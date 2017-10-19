package com.tdw.transaction.service.impl;

import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.tdw.transaction.component.RocketMQHelper;
import com.tdw.transaction.component.ThreshodsTimeManage;
import com.tdw.transaction.component.TransactionMessageMongodbRepository;
import com.tdw.transaction.constant.BZStatusCode;
import com.tdw.transaction.constant.MessageState;
import com.tdw.transaction.constant.Threshods;
import com.tdw.transaction.domain.TransactionMessage;
import com.tdw.transaction.exception.ServiceException;
import com.tdw.transaction.model.message.SendMessage;
import com.tdw.transaction.model.request.MessageIdCreator;
import com.tdw.transaction.service.NomalProcessMessageService;
import com.tdw.transaction.service.util.OptLogsService;
import com.tdw.transaction.util.IdGenerator;
import com.tdw.transaction.util.SortString;

@Service
public class NomalProcessMessageServiceImpl implements NomalProcessMessageService {
	private static final Logger logger = LoggerFactory.getLogger(NomalProcessMessageServiceImpl.class);

	@Autowired
	private TransactionMessageMongodbRepository transactionMessageMongodbRepository;

	@Autowired
	private ThreshodsTimeManage threshodsTimeManage;

	@Autowired
	private RocketMQHelper rocketMQHelper;

	@Autowired
	private RestTemplate restTemplate;

	private static HttpHeaders header = new HttpHeaders();

	private static HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();

	static {
		header.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON_UTF8));
		header.setContentType(MediaType.APPLICATION_JSON_UTF8);

		httpRequestFactory.setConnectionRequestTimeout(5000);
		httpRequestFactory.setConnectTimeout(5000);
		httpRequestFactory.setReadTimeout(5000);
	}

	@Override
	public String createMessage(MessageIdCreator mc) throws ServiceException {

		Preconditions.checkNotNull(mc);
		// define transaction message
		TransactionMessage transactionMessage = new TransactionMessage();
		// 操作信息
		transactionMessage.addLog(OptLogsService.createOptLogs(0, MessageState.PRESEND.code(), ""));

		// 初始化messageId
		transactionMessage.setId(IdGenerator.uuid36());
		transactionMessage.setMessageState(MessageState.PRESEND.code());
		transactionMessage.setCreateTime(new Date());

		transactionMessage.setMessage(mc.getMessage());
		transactionMessage.setExpectResult(mc.getExpectResult());
		transactionMessage.setResult("");
		transactionMessage.setMessageTopic(mc.getMessageTopic());
		transactionMessage.setMessageType(mc.getMessageType());
		transactionMessage.setExpectResult(mc.getExpectResult());

		transactionMessage.setPresendBackMethod(HttpMethod.POST);
		transactionMessage.setPresendBackSendTimes(0);
		transactionMessage.setPresendBackThreshold(Threshods.MAX_PRESENDBACK.code());
		transactionMessage.setPresendBackNextSendTime(
				threshodsTimeManage.createPreSendBackTime(Threshods.MAX_PRESENDBACK.code()));
		transactionMessage.setPresendBackUrl(mc.getPresendBackUrl());

		transactionMessage.setResultBackMethod(HttpMethod.POST);
		transactionMessage.setResultBackThreshod(Threshods.MAX_RESULTBACK.code());
		transactionMessage.setResultBackUrl(mc.getResultBackUrl());

		try {
			transactionMessageMongodbRepository.insert(transactionMessage);
		} catch (DuplicateKeyException e) {
			logger.error("DuplicateKeyException: {},{}", transactionMessage.getId(), e.getMessage());
			transactionMessage.setId(IdGenerator.uuid36());
			transactionMessageMongodbRepository.insert(transactionMessage);
		}

		return transactionMessage.getId();
	}

	@Override
	public String updateMessageToSend(String messageId) throws ServiceException {

		TransactionMessage transactionMessage = transactionMessageMongodbRepository.findById(messageId);

		Preconditions.checkNotNull(transactionMessage);

		if (MessageState.PRESEND.code() != transactionMessage.getMessageState()
				&& MessageState.DIED.code() != transactionMessage.getMessageState()) {
			throw new ServiceException(BZStatusCode.INVALID_STATE_OPTION);
		}

		// 操作信息
		transactionMessage.addLog(
				OptLogsService.createOptLogs(transactionMessage.getMessageState(), MessageState.SEND.code(), null));

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
			transactionMessageMongodbRepository.save(transactionMessage);
			return MessageState.DONE.message();
		} catch (Exception e) {
			logger.error("=================send exception============= {}",e.getMessage());
			transactionMessageMongodbRepository.save(transactionMessage);
			return MessageState.SEND.message();
		}

	}

	@Override
	public String updateMessageToDiscard(String messageId) throws ServiceException {

		TransactionMessage transactionMessage = transactionMessageMongodbRepository.findById(messageId);

		Preconditions.checkNotNull(transactionMessage);

		if (MessageState.DISCARD.code() == transactionMessage.getMessageState()) {
			throw new ServiceException(BZStatusCode.INVALID_STATE_OPTION);
		}

		// 操作信息
		transactionMessage.addLog(
				OptLogsService.createOptLogs(transactionMessage.getMessageState(), MessageState.DISCARD.code(), null));

		transactionMessage.setMessageState(MessageState.DISCARD.code());

		transactionMessageMongodbRepository.save(transactionMessage);

		return MessageState.DISCARD.message();
	}

	@Override
	public TransactionMessage getTransactionMessageById(String messageId) throws ServiceException {
		TransactionMessage transactionMessage = transactionMessageMongodbRepository.findById(messageId);

		Preconditions.checkNotNull(transactionMessage);

		return transactionMessage;
	}

	@Override
	public Page<TransactionMessage> queryTransactionMessageByState(int messageState, Pageable pageable)
			throws ServiceException {

		Page<TransactionMessage> result = transactionMessageMongodbRepository.findByMessageState(messageState,
				pageable);

		return result;
	}

	@Override
	public String updateMessageToPreSend(String messageId) throws ServiceException {

		TransactionMessage transactionMessage = transactionMessageMongodbRepository.findById(messageId);

		Preconditions.checkNotNull(transactionMessage);

		if (MessageState.ABNORMAL.code() != transactionMessage.getMessageState()) {
			throw new ServiceException(BZStatusCode.INVALID_STATE_OPTION);
		}

		// 操作日志
		transactionMessage.addLog(
				OptLogsService.createOptLogs(transactionMessage.getMessageState(), MessageState.PRESEND.code(), null));

		transactionMessage.setMessageState(MessageState.PRESEND.code());
		transactionMessage.setPresendBackThreshold(Threshods.MAX_PRESENDBACK.code());
		transactionMessage.setPresendBackNextSendTime(
				threshodsTimeManage.createPreSendBackTime(Threshods.MAX_PRESENDBACK.code()));

		transactionMessageMongodbRepository.save(transactionMessage);

		return MessageState.PRESEND.message();
	}

	@Override
	public String updateMessageToAbnormal(String messageId) throws ServiceException {

		TransactionMessage transactionMessage = transactionMessageMongodbRepository.findById(messageId);

		Preconditions.checkNotNull(transactionMessage);

		if (MessageState.PRESEND.code() != transactionMessage.getMessageState()) {
			throw new ServiceException(BZStatusCode.INVALID_STATE_OPTION);
		}

		// 操作日志
		transactionMessage.addLog(
				OptLogsService.createOptLogs(transactionMessage.getMessageState(), MessageState.ABNORMAL.code(), null));
		transactionMessage.setMessageState(MessageState.ABNORMAL.code());

		transactionMessageMongodbRepository.save(transactionMessage);

		return MessageState.ABNORMAL.message();
	}

	@Override
	public String updateMessageToDied(String messageId) throws ServiceException {

		TransactionMessage transactionMessage = transactionMessageMongodbRepository.findById(messageId);

		Preconditions.checkNotNull(transactionMessage);

		if (MessageState.SEND.code() != transactionMessage.getMessageState()) {
			throw new ServiceException(BZStatusCode.INVALID_STATE_OPTION);
		}

		// 操作日志
		transactionMessage.addLog(
				OptLogsService.createOptLogs(transactionMessage.getMessageState(), MessageState.DIED.code(), null));

		transactionMessage.setMessageState(MessageState.DIED.code());

		transactionMessageMongodbRepository.save(transactionMessage);

		return MessageState.DIED.message();
	}

	/**
	 * 按照多结果匹配回调，适应广播消息
	 */
	@Override
	public void resultToDone(JSONObject msgJo) throws ServiceException {
		SendMessage sendMessage = new SendMessage();
		try {
			sendMessage = JSONObject.toJavaObject(msgJo, SendMessage.class);
		} catch (Exception e) {
			// TODO 接入告警
			logger.error("Result message : {};   Exception: {}", msgJo, e.getMessage());
			return;
		}

		String messageId = sendMessage.getMessageId();
		String messageResult = sendMessage.getMessage().toString();

		// 检测ID有效性
		TransactionMessage transactionMessage = transactionMessageMongodbRepository.findById(messageId);
		Preconditions.checkNotNull(transactionMessage);
		// 结果合法性
		if (messageResult.length() != 1 || !transactionMessage.getExpectResult().contains(messageResult)) {
			logger.error("===========Result message error ==========: {} ; expect: {} ", msgJo,
					transactionMessage.getExpectResult());
			return;
		}
		// 幂等
		if (transactionMessage.getResult().contains(messageResult)) {
			logger.error("==========幂等===========: {} ", messageId);
			return;
		}

		logger.info("============Process=============== : {} ", messageId);

		StringBuilder sbuild = new StringBuilder();
		sbuild.append(transactionMessage.getResult());
		sbuild.append(messageResult);
		String newResult = SortString.sortString(sbuild.toString());

		if (newResult.equals(transactionMessage.getExpectResult())) {
			transactionMessage.setResult(newResult);
			updateMessageToDone(transactionMessage);
		} else {
			// 操作日志
			transactionMessage.addLog(OptLogsService.createOptLogs(transactionMessage.getMessageState(),
					transactionMessage.getMessageState(), messageResult));
			transactionMessage.setResult(newResult);
			transactionMessageMongodbRepository.save(transactionMessage);
		}
	}

	/**
	 * 消息结果回调
	 */
	@Override
	public String updateMessageToDone(TransactionMessage transactionMessage) throws ServiceException {

		if (MessageState.SEND.code() != transactionMessage.getMessageState()) {
			throw new ServiceException(BZStatusCode.INVALID_STATE_OPTION);
		}
		// 操作日志
		transactionMessage.addLog(
				OptLogsService.createOptLogs(transactionMessage.getMessageState(), MessageState.DONE.code(), "Done"));
		transactionMessage.setMessageState(MessageState.DONE.code());
		transactionMessage.setResultBackThreshod(Threshods.MAX_RESULTBACK.code());
		try {
			HashMap<String, Object> httpbodyMap = new HashMap<String, Object>();
			httpbodyMap.put("messageId", transactionMessage.getId());
			// 回调结果接口
			final ResponseEntity<String> response = restTemplate.exchange(transactionMessage.getResultBackUrl(),
					transactionMessage.getResultBackMethod(),
					new HttpEntity<HashMap<String, Object>>(httpbodyMap, header), String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				transactionMessage.setResultBackThreshod(Threshods.OVER.code());
			}
		} catch (Exception e) {
			logger.error("{} result back method exception: {}", transactionMessage.getId(), e.getMessage());
		}

		transactionMessageMongodbRepository.save(transactionMessage);

		return MessageState.DONE.message();
	}

}
