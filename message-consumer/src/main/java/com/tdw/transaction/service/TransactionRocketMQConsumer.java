package com.tdw.transaction.service;

import java.util.Random;

import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.qianmi.ms.starter.rocketmq.annotation.RocketMQMessageListener;
import com.qianmi.ms.starter.rocketmq.core.RocketMQListener;

@Component
@RocketMQMessageListener(topic = "ProducerTestTopic", consumerGroup = "transaction-group")
public class TransactionRocketMQConsumer implements RocketMQListener<MessageExt>{
	private static final Logger logger = LoggerFactory.getLogger(TransactionRocketMQConsumer.class);
	
	private Random random = new Random();

    public void onMessage(MessageExt messageExt) {
    	int value = random.nextInt(300);

        if ((value % 3) == 0) {
        	logger.info("TransactionRocketMQConsumer RuntimeException! ============= \n {}" , messageExt );
            throw new RuntimeException("Could not find db");
        }

    	logger.info("==========================received message========================= \n {} \n {}", messageExt.toString(),new String(messageExt.getBody()));
    }

}