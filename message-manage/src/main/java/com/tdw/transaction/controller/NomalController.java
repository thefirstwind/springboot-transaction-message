package com.tdw.transaction.controller;

import java.util.regex.Pattern;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tdw.transaction.constant.BZStatusCode;
import com.tdw.transaction.constant.Constants;
import com.tdw.transaction.constant.MessageState;
import com.tdw.transaction.domain.TransactionMessage;
import com.tdw.transaction.exception.ServiceException;
import com.tdw.transaction.model.request.MessageIdCreator;
import com.tdw.transaction.service.NomalProcessMessageService;
import com.tdw.transaction.util.Result;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
public class NomalController {

	private static final Logger logger = LoggerFactory.getLogger(NomalController.class);

	@Autowired
	private NomalProcessMessageService preSendMessageService;

	// @ApiOperation(value = "測試", notes = "hello world")
	// @ApiImplicitParams({ @ApiImplicitParam(name = "name", value = "名字",
	// paramType = "query", dataType = "string") })
	// @RequestMapping(value = "/get", method = RequestMethod.GET)
	// public HashMap<String, Object> get(@RequestParam String name) {
	//
	// logger.info("get: {}", name);
	// HashMap<String, Object> map = new HashMap<String, Object>();
	// map.put("title", "hello world");
	// map.put("name", name);
	//
	// return map;
	// }

	@ApiOperation(value = "消息ID获取", notes = "消息ID获取")
	@ApiImplicitParam(name = "messageIdCreator", value = "创建消息ID基础信息", paramType = "body", required = true, dataType = "MessageIdCreator")
	@RequestMapping(value = "/message/creator", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> createMessage(@Valid @RequestBody MessageIdCreator messageIdCreator) {

		logger.debug("MessageIdCreator: {}", messageIdCreator.toString());

		String resltStr = preSendMessageService.createMessage(messageIdCreator);

		return new ResponseEntity<Result<String>>(new Result<String>(resltStr), HttpStatus.OK);
	}

	@ApiOperation(value = "预发送消息确认发送状态", notes = "预发送消息确认发送状态【发送、废弃】")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "messageId", value = "消息ID", paramType = "path", required = true, dataType = "string"),
			@ApiImplicitParam(name = "state", value = "确认状态", paramType = "path", required = true, dataType = "boolean") })
	@RequestMapping(value = "/message/send/{messageId}/{state}", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> putMessageToSend(@PathVariable String messageId,
			@PathVariable boolean state) {
		if (messageId.isEmpty() || messageId.length() > Constants.MESSAGE_ID_MAX_LENGTH
				|| !Pattern.matches("[0-9A-Za-z_-]+$", messageId)) {
			logger.error("invalid  parameter, messageid: {}", messageId);
			throw new ServiceException(BZStatusCode.INVALID_MODEL_FIELDS);
		}

		logger.debug("messageidSend: {} , {}", messageId, state);

		if (state) {
			preSendMessageService.updateMessageToSend(messageId);
		} else {
			preSendMessageService.updateMessageToDiscard(messageId);
		}

		return new ResponseEntity<Result<String>>(new Result<String>(""), HttpStatus.OK);
	}

	@ApiOperation(value = "消息列表查询", notes = "按照消息状态查询消息列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "state", value = "消息状态", paramType = "path", required = true, dataType = "int"),
			@ApiImplicitParam(name = "page", value = "页码", paramType = "query", required = true, dataType = "int"),
			@ApiImplicitParam(name = "size", value = "页条数", paramType = "query", required = true, dataType = "int") })
	@RequestMapping(value = "/message/query/{state}", method = RequestMethod.POST)
	public ResponseEntity<Result<Page<TransactionMessage>>> queryMessageListByState(@PathVariable int state,
			@RequestParam int page, @RequestParam int size) {

		logger.debug("queryMessageListByState: {} ", state);

		Pageable pageable = new PageRequest(page, size, new Sort(new Order(Direction.DESC, "createTime")));

		Page<TransactionMessage> pts = preSendMessageService.queryTransactionMessageByState(state, pageable);

		return new ResponseEntity<Result<Page<TransactionMessage>>>(new Result<Page<TransactionMessage>>(pts),
				HttpStatus.OK);
	}

	@ApiOperation(value = "人工干预-异常消息", notes = "人工干预-异常消息【预发送、废弃】")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "messageIds", value = "消息ID集【id1,id2,id3,...】", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "state", value = "确认状态", paramType = "path", required = true, dataType = "int") })
	@RequestMapping(value = "/message/change/abnormal/{state}", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> changeAbnormal(@RequestBody String messageIds, @PathVariable int state) {
		if (messageIds.isEmpty() || !Pattern.matches("[0-9A-Za-z_,-]+$", messageIds)) {
			logger.error("invalid  parameter, messageIds: {}", messageIds);
			throw new ServiceException(BZStatusCode.INVALID_MODEL_FIELDS);
		}

		logger.debug("changeAbnormal: {} , {}", messageIds, state);

		String[] idArray = messageIds.split(",");
		StringBuilder sb = new StringBuilder();
		for (String id : idArray) {
			try {
				if (state == MessageState.PRESEND.code()) {
					preSendMessageService.updateMessageToPreSend(id);
				} else if (state == MessageState.DISCARD.code()) {
					preSendMessageService.updateMessageToDiscard(id);
				} else {
					sb.append(",");
					sb.append(id);
				}
			} catch (RuntimeException e) {
				logger.error("changeAbnormal: id = {} , err = {}", id, e.getMessage());
				sb.append(",");
				sb.append(id);
			}
		}

		return new ResponseEntity<Result<String>>(new Result<String>(sb.toString()), HttpStatus.OK);
	}

	@ApiOperation(value = "人工干预-死亡消息", notes = "人工干预-死亡消息【发送、废弃】")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "messageIds", value = "消息ID集【id1,id2,id3,...】", paramType = "body", required = true, dataType = "string"),
			@ApiImplicitParam(name = "state", value = "确认状态", paramType = "path", required = true, dataType = "int") })
	@RequestMapping(value = "/message/change/died/{state}", method = RequestMethod.POST)
	public ResponseEntity<Result<String>> changeDied(@RequestBody String messageIds, @PathVariable int state) {
		if (messageIds.isEmpty() || !Pattern.matches("[0-9A-Za-z_,-]+$", messageIds)) {
			logger.error("invalid  parameter, messageIds: {}", messageIds);
			throw new ServiceException(BZStatusCode.INVALID_MODEL_FIELDS);
		}

		logger.debug("changeDied: {} , {}", messageIds, state);

		String[] idArray = messageIds.split(",");
		StringBuilder sb = new StringBuilder();
		for (String id : idArray) {
			try {
				if (state == MessageState.SEND.code()) {
					preSendMessageService.updateMessageToSend(id);
				} else if (state == MessageState.DISCARD.code()) {
					preSendMessageService.updateMessageToDiscard(id);
				} else {
					sb.append(",");
					sb.append(id);
				}

			} catch (RuntimeException e) {
				logger.error("changeDied: id = {} , err = {}", id, e.getMessage());
				sb.append(",");
				sb.append(id);
			}
		}

		return new ResponseEntity<Result<String>>(new Result<String>(sb.toString()), HttpStatus.OK);
	}

}