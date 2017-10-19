package com.tdw.transaction.service.impl;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tdw.transaction.component.ThreshodsTimeManage;
import com.tdw.transaction.constant.MessageState;
import com.tdw.transaction.constant.Threshods;
import com.tdw.transaction.domain.TransactionMessage;
import com.tdw.transaction.model.request.MessageIdCreator;
import com.tdw.transaction.service.NomalProcessMessageService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "presend.back.threshods=1200,600,60,30,10,8", "result.back.threshods=30",
		"test.send.topic=test.send.topic" })
public class NomalProcessMessageServiceTest {
	private static final Logger logger = LoggerFactory.getLogger(NomalProcessMessageServiceTest.class);

	@Autowired
	private NomalProcessMessageService preSendMessageService;

	@Autowired
	private ThreshodsTimeManage threshodsTimeManage;

	private MessageIdCreator mc = new MessageIdCreator();

	private String messageId = "";

	@Value("${test.send.topic}")
	private String testSendTopic;

	@Before
	public void before() {
		// 初始化数据；
		mc.setMessageTopic(testSendTopic);
		mc.setMessageType(0);
		mc.setPresendBackUrl("http://www.sina.net");
		mc.setResultBackUrl("http://www.sina.net");
		mc.setServiceName("1234");
		mc.setExpectResult("a");
		mc.setMessage("{'a':'a','b':'b'}");

	}
	

	/**
	 * 创建 NullPointerException
	 * 
	 */
	@Test(expected = NullPointerException.class)
	public void createMessageNullException() {
		//创建
		preSendMessageService.createMessage(null);
	}

	/**
	 * 创建消息 检查创建是否添加成功
	 * @Test(expected = IllegalArgumentException.class)
	 */
	@Test
	public void createMessage() {
		// 正常创建
		messageId = preSendMessageService.createMessage(mc);
		logger.info("TransactionMessage ID: {}", messageId);
		Assertions.assertThat(messageId).isNotEmpty();
		Assertions.assertThat(messageId.length()).isLessThan(37);

		TransactionMessage transactionMessage = preSendMessageService.getTransactionMessageById(messageId);

		// 预发送，期望结果是否正确
		Assertions.assertThat(transactionMessage.getMessageState()).isEqualTo(MessageState.PRESEND.code());
		Assertions.assertThat(transactionMessage.getCreateTime()).isNotNull();

		Assertions.assertThat(transactionMessage.getMessage()).isEqualTo(mc.getMessage());
		Assertions.assertThat(transactionMessage.getResult()).isEqualTo("");
		Assertions.assertThat(transactionMessage.getMessageTopic()).isEqualTo(mc.getMessageTopic());
		Assertions.assertThat(transactionMessage.getMessageType()).isEqualTo(mc.getMessageType());
		Assertions.assertThat(transactionMessage.getExpectResult()).isEqualTo(mc.getExpectResult());

		// 回调接口是否正确，回调阀值是否正确，回调时间是否设置，
		Assertions.assertThat(transactionMessage.getPresendBackMethod()).isEqualTo(HttpMethod.POST);
		Assertions.assertThat(transactionMessage.getPresendBackSendTimes()).isEqualTo(0);
		Assertions.assertThat(transactionMessage.getPresendBackThreshold()).isEqualTo(Threshods.MAX_PRESENDBACK.code());
		Assertions.assertThat(transactionMessage.getPresendBackNextSendTime())
				.isBefore((threshodsTimeManage.createPreSendBackTime(Threshods.MAX_PRESENDBACK.code())));
		Assertions.assertThat(transactionMessage.getPresendBackUrl()).isEqualTo(mc.getPresendBackUrl());

		Assertions.assertThat(transactionMessage.getResultBackMethod()).isEqualTo(HttpMethod.POST);
		Assertions.assertThat(transactionMessage.getResultBackThreshod()).isEqualTo(Threshods.MAX_RESULTBACK.code());
		Assertions.assertThat(transactionMessage.getResultBackUrl()).isEqualTo(mc.getResultBackUrl());

		// 创建日志是否添加成功；
		Assertions.assertThat(transactionMessage.getLogs().size()).isEqualTo(1);

	}

	/**
	 * 创建消息 检查预发送更新为发送是否成功
	 */
	@Test
	public void updateMessageToSend() {
		createMessage();

		// 检测消息存储状态
		String uStr = preSendMessageService.updateMessageToSend(messageId);
		Assertions.assertThat(uStr).isEqualTo(MessageState.SEND.message());

		TransactionMessage transactionMessage = preSendMessageService.getTransactionMessageById(messageId);
		Assertions.assertThat(transactionMessage.getMessageState()).isEqualTo(MessageState.SEND.code());
		Assertions.assertThat(transactionMessage.getMessageNextSendTime()).isNotNull();
		Assertions.assertThat(transactionMessage.getMessageSendThreshold()).isGreaterThan(0);
		Assertions.assertThat(transactionMessage.getLogs().size()).isGreaterThan(1);

	}

	/**
	 * 创建消息 检查预发送更新为废弃是否成功
	 */
	@Test
	public void updateMessageToDiscard() {
		createMessage();

		String uStr = preSendMessageService.updateMessageToDiscard(messageId);
		Assertions.assertThat(uStr).isEqualTo(MessageState.DISCARD.message());

		TransactionMessage transactionMessage = preSendMessageService.getTransactionMessageById(messageId);
		Assertions.assertThat(transactionMessage.getMessageState()).isEqualTo(MessageState.DISCARD.code());

	}

	/**
	 * 获取消息列表
	 */
	@Test
	public void queryTransactionMessageByState() {
		createMessage();
		createMessage();
		createMessage();
		createMessage();
		createMessage();

		Pageable pageable = new PageRequest(2, 3, new Sort(new Order(Direction.DESC, "id")));
		Page<TransactionMessage> ptm = preSendMessageService.queryTransactionMessageByState(MessageState.PRESEND.code(),
				pageable);

		Assertions.assertThat(ptm.getTotalElements()).isGreaterThan(4);
		Assertions.assertThat(ptm.getTotalPages()).isGreaterThan(1);

	}
	


	/**
	 * 预发送 -》 异常
	 */
	@Test
	public void updateMessageToAbnormal() {
		createMessage();

		String uStr = preSendMessageService.updateMessageToAbnormal(messageId);
		Assertions.assertThat(uStr).isEqualTo(MessageState.ABNORMAL.message());

		TransactionMessage transactionMessage = preSendMessageService.getTransactionMessageById(messageId);
		Assertions.assertThat(transactionMessage.getMessageState()).isEqualTo(MessageState.ABNORMAL.code());
		
	}


	/**
	 * 异常 -》 预发送 
	 */
	@Test
	public void updateMessageToPreSend() {
		updateMessageToAbnormal();

		String uStr = preSendMessageService.updateMessageToPreSend(messageId);
		Assertions.assertThat(uStr).isEqualTo(MessageState.PRESEND.message());

		TransactionMessage transactionMessage = preSendMessageService.getTransactionMessageById(messageId);
		Assertions.assertThat(transactionMessage.getMessageState()).isEqualTo(MessageState.PRESEND.code());
		
	}
	

	/**
	 * 发送 -》 死亡
	 */
	@Test
	public void updateMessageToDied() {
		updateMessageToSend();

		String uStr = preSendMessageService.updateMessageToDied(messageId);
		Assertions.assertThat(uStr).isEqualTo(MessageState.DIED.message());

		TransactionMessage transactionMessage = preSendMessageService.getTransactionMessageById(messageId);
		Assertions.assertThat(transactionMessage.getMessageState()).isEqualTo(MessageState.DIED.code());

	}

	/**
	 * 发送 -》 完成
	 */
	@Test
	public void updateMessageToDone() {
		updateMessageToSend();

		TransactionMessage transactionMessage = preSendMessageService.getTransactionMessageById(messageId);
		String uStr = preSendMessageService.updateMessageToDone(transactionMessage);
		Assertions.assertThat(uStr).isEqualTo(MessageState.DONE.message());
		Assertions.assertThat(transactionMessage.getResultBackThreshod()).isLessThan(4);

	}
	
}
