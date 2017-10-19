package com.tdw.transaction.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tdw.transaction.service.AbnomalProcessMessageService;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { 
		"presend.back.threshods=8,8,8,8,8,8", 
		"result.back.threshods=30", 
		"send.threshods=0,0,0,0,0,0,0,0,0,0",
		"test.send.topic=test.send.topic" })
public class AbnomalProcessMessageServiceImplTest {
	
	@Autowired
	AbnomalProcessMessageService abnomalProcessMessageService;

	@Before
	public void before() {
		// 初始化数据；
	}
	

	/**
	 * [预发送回调]请求连接失败，检测执行结果
	 */
	@Test
	public void preSendCallbackByTaskHttpFailed() throws InterruptedException {
		abnomalProcessMessageService.preSendCallbackByTask();
	}


	/**
	 * [预发送回调]请求连接成功，检测执行结果
	 */
	@Test
	public void preSendCallbackByTaskHttpSuccess() throws InterruptedException {
		//TODO
	}
	


	/**
	 * [预发送回调]请求连接失败，检测 预发布 到 异常
	 */
	@Test
	public void preSendCallbackByTaskHttpFailedToAbnomal() throws InterruptedException {
		//TODO
	}
	


	/**
	 * [预发送回调]请求连接多线程并发；
	 */
	@Test
	public void preSendCallbackByTaskMoreThread() throws InterruptedException {
		//TODO
	}



	/**
	 * [预发送回调]多定时任务调用；
	 */
	@Test
	public void preSendCallbackByTaskMoreTask() throws InterruptedException {
		//TODO
	}
	
	

	


	/**
	 * [发送消息]消息发送，成功
	 */
	@Test
	public void SendMQByTaskSuccess() throws InterruptedException {
		//TODO
		abnomalProcessMessageService.sendToMQByTask();
	}


	/**
	 * [发送消息]消息发送，阀值减少
	 */
	@Test
	public void SendMQByTaskAssertThreshods() throws InterruptedException {
		//TODO
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
	}
	


	/**
	 * [发送消息]消息发送，阀值耗尽，状态变更：死亡
	 */
	@Test
	public void SendMQByTaskToDeid() throws InterruptedException {
		//TODO
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
		abnomalProcessMessageService.sendToMQByTask();
	}
	
	
	
	
	
	
	
}
