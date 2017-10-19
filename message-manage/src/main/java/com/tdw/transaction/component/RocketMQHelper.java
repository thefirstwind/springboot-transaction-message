package com.tdw.transaction.component;

import javax.annotation.Resource;

import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.qianmi.ms.starter.rocketmq.core.RocketMQTemplate;
import com.tdw.transaction.domain.TransactionMessage;


@Component
public class RocketMQHelper {

	@Resource
	private RocketMQTemplate rocketMQTemplate;

	/**
	 * 发送消息中带事物Id的事物消息
	 * @param transactionMessage
	 */
	public void sendNomalMsg(TransactionMessage transactionMessage) {
		// 消息发送
		JSONObject sendMsg = new JSONObject();
		sendMsg.put("messageId", transactionMessage.getId());
		sendMsg.put("message", transactionMessage.getMessage());
		rocketMQTemplate.convertAndSend(transactionMessage.getMessageTopic(), sendMsg);
	}

	/**
	 * 发送带key[事物id]的事物消息
	 * @param transactionMessage
	 */
	public void sendTranTopicMsg(TransactionMessage transactionMessage) {
		Message<?> message = MessageBuilder.withPayload(transactionMessage.getMessage())
				.setHeader(MessageConst.PROPERTY_KEYS, transactionMessage.getId()).build();
		rocketMQTemplate.send(transactionMessage.getMessageTopic(), message);
	}

}
