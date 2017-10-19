package com.tdw.transaction.controller;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tdw.transaction.client.producer.LocalTransactionState;
import com.tdw.transaction.client.send.SendMassage;
import com.tdw.transaction.model.request.MessageIdCreator;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/msg")
public class MsgClientController {

	private static final Logger logger = LoggerFactory.getLogger(MsgClientController.class);

	@Value("${massage.manage.url}")
	private String massageManageUrl;

	AtomicInteger transactionIndex = new AtomicInteger(1);

	/**
	 * 发送事务消息
	 * 
	 * @param name
	 * @return
	 */
	@ApiOperation(value = "測試", notes = "hello world")
	@ApiImplicitParams({ @ApiImplicitParam(name = "result", value = "名字", paramType = "query", dataType = "string") })
	@RequestMapping(value = "/producer", method = RequestMethod.GET)
	public HashMap<String, Object> Producer(@RequestParam String result) {
		SendMassage sendMassage = new SendMassage(massageManageUrl);

		logger.info("producer: {}", result);
		// 构建消息
		HashMap<String, Object> msg = new HashMap<String, Object>();
		msg.put("title", "hello world");
		msg.put("name", result);

		// 发送事务消息
		MessageIdCreator messageIdCreator = new MessageIdCreator();
		messageIdCreator.setExpectResult(result);
		messageIdCreator.setMessage(msg);
		messageIdCreator.setMessageTopic("ProducerTestTopic");
		messageIdCreator.setMessageType(0);
		messageIdCreator.setPresendBackUrl("http://127.0.0.1:8090/msg/messageid/check");
		messageIdCreator.setResultBackUrl("http://127.0.0.1:8090/msg/messageid/result");
		messageIdCreator.setServiceName("pro");

		LocalTransactionState lts = sendMassage.preSendMassage(messageIdCreator, new TransactionExecuterImpl(), null);
		
		msg.put("state", lts);
		return msg;
	}

	/**
	 * 事务消息回调
	 */
	@RequestMapping(value = "/messageid/check", method = RequestMethod.POST)
	public HashMap<String, Object> checkListener(@RequestBody String body) {
		HashMap<String, Object> msg = new HashMap<String, Object>();

		logger.info("=======================messageid/check===========================body: {}", body);
		int value = transactionIndex.getAndIncrement();
		if ((value % 3) == 0) {
			logger.info("TransactionExecuterImpl ROLLBACK_MESSAGE! =============>  " + body + "\n");
			msg.put("status", 204);
			msg.put("message", "rollback");
			return msg;
		} 

		logger.info("TransactionExecuterImpl COMMIT_MESSAGE! =============>  " + body + "\n");
		msg.put("status", 200);
		msg.put("message", "commit");
		return msg;
	}

	/**
	 * 消息结果通知
	 */
	@RequestMapping(value = "/messageid/result", method = RequestMethod.POST)
	public ResponseEntity<String> messageResult(@RequestBody String body) {
		// TODO 业务处理
		logger.info("===================messageid result======================= : " + body);
		return new ResponseEntity<String>("", HttpStatus.OK);
	}

}