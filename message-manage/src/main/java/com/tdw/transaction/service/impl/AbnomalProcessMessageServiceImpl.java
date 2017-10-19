package com.tdw.transaction.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.tdw.transaction.component.RocketMQHelper;
import com.tdw.transaction.component.ThreshodsTimeManage;
import com.tdw.transaction.component.TransactionMessageMongodbTemplate;
import com.tdw.transaction.constant.Constants;
import com.tdw.transaction.constant.MessageState;
import com.tdw.transaction.constant.Threshods;
import com.tdw.transaction.domain.TransactionMessage;
import com.tdw.transaction.exception.ServiceException;
import com.tdw.transaction.service.AbnomalProcessMessageService;
import com.tdw.transaction.service.util.OptLogsService;

@Service
public class AbnomalProcessMessageServiceImpl implements AbnomalProcessMessageService {

	private static final Logger logger = LoggerFactory.getLogger(AbnomalProcessMessageServiceImpl.class);

	@Autowired
	private TransactionMessageMongodbTemplate transactionMessageMongodbTemplate;

	@Autowired
	private ThreshodsTimeManage threshodsTimeManage;

	@Autowired
	private RocketMQHelper rocketMQHelper;
	
	private static Date PRESEND_CALLBACK_BYTASK_TIME = null;

	private static HttpHeaders header = new HttpHeaders();

	private static HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();

	static {
		header.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON_UTF8));
		header.setContentType(MediaType.APPLICATION_JSON_UTF8);

		httpRequestFactory.setConnectionRequestTimeout(5000);
		httpRequestFactory.setConnectTimeout(5000);
		httpRequestFactory.setReadTimeout(5000);
	}

	/**
	 * 预发送回调确认
	 */
	@Override
	public void preSendCallbackByTask() throws ServiceException {
		// 任务执行中不可重复调用
		if (isPreSendCallbackRuning()) {
			return;
		}
		Calendar calendar = Calendar.getInstance();
		List<TransactionMessage> transactionMessageList = transactionMessageMongodbTemplate
				.findForPresendBack(calendar.getTime(), MessageState.PRESEND.code());
		// 操作数据，直接返回
		if (transactionMessageList.size() == 0) {
			logger.debug("PresendCallback list is null....");
			return;
		}

		logger.debug("PresendCallback running....");
		ExecutorService poll = Executors.newFixedThreadPool(transactionMessageList.size());

		for (TransactionMessage transactionMessage : transactionMessageList) {
			Future<Boolean> future = poll.submit(presendCallback(transactionMessage));
			try {
				// 设置超时
				future.get(Constants.RESTFUL_MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("PresendCallback 任务异常中止 : {}", e.getMessage());
			} catch (ExecutionException e) {
				logger.error("PresendCallback 计算出现异常: {}", e.getMessage());
			} catch (TimeoutException e) {
				logger.error("PresendCallback 超时异常: {}", e.getMessage());
				// 超时后取消任务
				future.cancel(true);
			}
		}

		poll.shutdown();
	}

	@Override
	public void sendToMQByTask() throws ServiceException {
		Calendar calendar = Calendar.getInstance();
		List<TransactionMessage> transactionMessageList = transactionMessageMongodbTemplate
				.findForSendMQ(calendar.getTime(), MessageState.SEND.code());
		// 操作数据，直接返回
		if (transactionMessageList.size() == 0) {
			logger.debug("sendToMQByTask list is null....");
			return;
		}

		logger.debug("sendToMQByTask running....");

		for (TransactionMessage transactionMessage : transactionMessageList) {

			try {
				// 消息发送			
				rocketMQHelper.sendTranTopicMsg(transactionMessage);
				
				transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
						MessageState.DONE.code(), null));
				transactionMessage.setMessageState(MessageState.DONE.code());
				transactionMessageMongodbTemplate.save(transactionMessage);
			} catch (Exception e) {
				logger.error("=================send exception============= {}", e.getMessage());

				int timesTmp = transactionMessage.getMessageSendTimes();
				transactionMessage.setMessageSendTimes(timesTmp + 1);

				int thresholdTmp = transactionMessage.getMessageSendThreshold();
				if (thresholdTmp <= 0) {
					// 操作信息
					transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
							MessageState.DIED.code(), "SendToMQ  Died"));
					transactionMessage.setMessageState(MessageState.DIED.code());
				} else {
					transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
							transactionMessage.getMessageState(), "SendToMQ  ..."));
					transactionMessage.setMessageNextSendTime(threshodsTimeManage.createSendNextTime(thresholdTmp));
					transactionMessage.setMessageSendThreshold(thresholdTmp - 1);
				}

				transactionMessageMongodbTemplate.save(transactionMessage);
			}
		}

	}

	@Override
	public void resultCallbackByTask() throws ServiceException {
		// 任务执行中不可重复调用
		if (isPreSendCallbackRuning()) {
			return;
		}
		Calendar calendar = Calendar.getInstance();
		List<TransactionMessage> transactionMessageList = transactionMessageMongodbTemplate
				.findForDoneBack(calendar.getTime(), MessageState.DONE.code());
		// 操作数据，直接返回
		if (transactionMessageList.size() == 0) {
			logger.debug("resultCallbackByTask list is null....");
			return;
		}

		logger.debug("resultCallbackByTask running....");
		ExecutorService poll = Executors.newFixedThreadPool(transactionMessageList.size());

		for (TransactionMessage transactionMessage : transactionMessageList) {
			Future<Boolean> future = poll.submit(resultCallback(transactionMessage));
			try {
				// 设置超时
				future.get(Constants.RESTFUL_MAX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("DoneCallback 任务异常中止 : {}", e.getMessage());
			} catch (ExecutionException e) {
				logger.error("DoneCallback 计算出现异常: {}", e.getMessage());
			} catch (TimeoutException e) {
				logger.error("DoneCallback 超时异常: {}", e.getMessage());
				// 超时后取消任务
				future.cancel(true);
			}
		}

		poll.shutdown();

	}

	private Callable<Boolean> presendCallback(TransactionMessage transactionMessage) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				HashMap<String, Object> httpbodyMap = new HashMap<String, Object>();
				httpbodyMap.put("messageId", transactionMessage.getId());
				RestTemplate thRestTemplate = new RestTemplate(httpRequestFactory);

				try {
					final ResponseEntity<String> response = thRestTemplate.exchange(
							transactionMessage.getPresendBackUrl(), transactionMessage.getPresendBackMethod(),
							new HttpEntity<HashMap<String, Object>>(httpbodyMap, header), String.class);

					logger.debug("PreSend Callback response: {} ", response.toString());

					if (response.getStatusCode().equals(HttpStatus.OK)) {
						JSONObject jo = JSONObject.parseObject(response.getBody());
						int status = jo.getIntValue("status");
						if (200 == status) {
							logger.debug("{} PreSend Callback success!", transactionMessage.getId());
							presendCallbackSuccess(transactionMessage);
							return true;
						}
						if (204 == status) {
							logger.debug("{} PreSend Callback discard!", transactionMessage.getId());
							changeToDiscard(transactionMessage);
							return true;
						}
					}
				} catch (Exception e) {
					logger.error("PreSend Callback Exception : {}", e.getMessage());
				}

				logger.debug("{} PreSend Callback failed!", transactionMessage.getId());
				presendCallbackFailed(transactionMessage);
				return true;

			}
		};
	}

	private void presendCallbackSuccess(TransactionMessage transactionMessage) {
		// 操作信息
		transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
				MessageState.SEND.code(), "Presend callback success!"));

		transactionMessage.setMessageState(MessageState.SEND.code());
		transactionMessage.setMessageSendThreshold(Threshods.MAX_SEND.code());
		int sendTimes = transactionMessage.getMessageSendTimes() + 1;
		transactionMessage.setMessageSendTimes(sendTimes);
		transactionMessage.setMessageNextSendTime(threshodsTimeManage.createSendNextTime(Threshods.MAX_SEND.code()));

		try {

			// 消息发送			
			rocketMQHelper.sendTranTopicMsg(transactionMessage);

			transactionMessage.addLog(
					OptLogsService.createOptLogs(transactionMessage.getMessageState(), MessageState.DONE.code(), null));
			transactionMessage.setMessageState(MessageState.DONE.code());
			transactionMessageMongodbTemplate.save(transactionMessage);
		} catch (Exception e) {
			logger.error("=================send exception============= {}", e.getMessage());
			transactionMessageMongodbTemplate.save(transactionMessage);
		}

	}

	private void presendCallbackFailed(TransactionMessage transactionMessage) {
		// 操作信息
		transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
				transactionMessage.getMessageState(), "Presend callback failed!"));

		int timesTmp = transactionMessage.getPresendBackSendTimes();
		transactionMessage.setPresendBackSendTimes(timesTmp + 1);

		int thresholdTmp = transactionMessage.getPresendBackThreshold();
		if (thresholdTmp <= 0) {
			// 异常
			transactionMessage.setMessageState(MessageState.ABNORMAL.code());
		} else {
			transactionMessage.setPresendBackNextSendTime(threshodsTimeManage.createPreSendBackTime(thresholdTmp));
			transactionMessage.setPresendBackThreshold(thresholdTmp - 1);
		}
		transactionMessageMongodbTemplate.save(transactionMessage);
	}

	private void changeToDiscard(TransactionMessage transactionMessage) {
		// 操作信息
		transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
				MessageState.DISCARD.code(), "Change to discard!"));
		transactionMessage.setMessageState(MessageState.DISCARD.code());
		transactionMessageMongodbTemplate.save(transactionMessage);
	}

	private boolean isPreSendCallbackRuning() {
		if (null == PRESEND_CALLBACK_BYTASK_TIME) {
			PRESEND_CALLBACK_BYTASK_TIME = new Date();
			return false;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -1 * (Constants.RESTFUL_MAX_TIMEOUT_SECONDS + 2));
		if (calendar.getTime().before(PRESEND_CALLBACK_BYTASK_TIME)) {
			logger.info("PresendCallback 正在执行中，任务不可重复调用 ......!");
			return true;
		}
		return false;
	}

	private Callable<Boolean> resultCallback(TransactionMessage transactionMessage) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				HashMap<String, Object> httpbodyMap = new HashMap<String, Object>();
				httpbodyMap.put("messageId", transactionMessage.getId());
				RestTemplate thRestTemplate = new RestTemplate(httpRequestFactory);

				try {
					final ResponseEntity<String> response = thRestTemplate.exchange(
							transactionMessage.getResultBackUrl(), transactionMessage.getResultBackMethod(),
							new HttpEntity<HashMap<String, Object>>(httpbodyMap, header), String.class);
					if (response.getStatusCode().equals(HttpStatus.OK)) {
						logger.debug("{} Result Callback success!", transactionMessage.getId());
						resultCallbackSuccess(transactionMessage);
						return true;
					}
				} catch (Exception e) {
					logger.error("Result Callback Exception : {}", e.getMessage());
				}

				logger.debug("{} Result Callback failed!", transactionMessage.getId());
				resultCallbackFailed(transactionMessage);
				return true;

			}
		};
	}

	private void resultCallbackSuccess(TransactionMessage transactionMessage) {
		// 操作信息
		transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
				MessageState.DONE.code(), "result callback success!"));

		transactionMessage.setResultBackThreshod(Threshods.OVER.code());
		transactionMessageMongodbTemplate.save(transactionMessage);

		// TODO 完成信息转移
	}

	private void resultCallbackFailed(TransactionMessage transactionMessage) {
		// 操作信息
		transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
				transactionMessage.getMessageState(), "result callback failed!"));

		int thresholdTmp = transactionMessage.getResultBackThreshod();
		transactionMessage.setPresendBackNextSendTime(threshodsTimeManage.createResultBackTime());
		transactionMessage.setPresendBackThreshold(thresholdTmp - 1);
		transactionMessageMongodbTemplate.save(transactionMessage);
	}

}
