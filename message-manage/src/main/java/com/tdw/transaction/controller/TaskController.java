package com.tdw.transaction.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.tdw.transaction.service.AbnomalProcessMessageService;
import com.tdw.transaction.util.Result;

import io.swagger.annotations.ApiOperation;

@RestController
public class TaskController {

	private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
	
	@Autowired
	AbnomalProcessMessageService abnomalProcessMessageService;

	@ApiOperation(value = "任务触发预发送状态回调确认", notes = "任务触发预发送状态回调确认【预发送、发送、废弃】")
	@RequestMapping(value = "/message/task/presend", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> presendTask() {
		abnomalProcessMessageService.preSendCallbackByTask();
		logger.debug("触发预发送状态回调确认 。。。");
		return new ResponseEntity<Result<String>>(new Result<String>(""), HttpStatus.OK);
	}


	@ApiOperation(value = "任务触发发送消息", notes = "任务触发发送消息【预发送、发送、死亡】")
	@RequestMapping(value = "/message/task/send", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> sendTask() {
		abnomalProcessMessageService.sendToMQByTask();
		logger.debug("任务触发发送消息 。。。");
		return new ResponseEntity<Result<String>>(new Result<String>(""), HttpStatus.OK);
	}
	


	@ApiOperation(value = "任务触发发送完成回调", notes = "任务触发发送完成回调【完成】")
	@RequestMapping(value = "/message/task/done", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> doneTask() {
		abnomalProcessMessageService.resultCallbackByTask();
		logger.debug("任务触发发送完成回调。。。");
		return new ResponseEntity<Result<String>>(new Result<String>(""), HttpStatus.OK);
	}
	
}