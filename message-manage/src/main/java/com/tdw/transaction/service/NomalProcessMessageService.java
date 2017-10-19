package com.tdw.transaction.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alibaba.fastjson.JSONObject;
import com.tdw.transaction.domain.TransactionMessage;
import com.tdw.transaction.exception.ServiceException;
import com.tdw.transaction.model.request.MessageIdCreator;

public interface NomalProcessMessageService {
	
	/**
	 * 创建消息
	 */
	String createMessage(MessageIdCreator messageIdCreator) throws ServiceException;


	/**
	 * 更新消息为发送状态
	 */
	String updateMessageToSend(String messageId) throws ServiceException;
	

	/**
	 * 更新消息为预发送状态
	 */
	String updateMessageToPreSend(String messageId) throws ServiceException;
	

	/**
	 * 更新消息为异常状态
	 */
	String updateMessageToAbnormal(String messageId) throws ServiceException;
	

	/**
	 * 更新消息为死亡状态
	 */
	String updateMessageToDied(String messageId) throws ServiceException;
	

	/**
	 * 更新消息为完成状态
	 */
	String updateMessageToDone(TransactionMessage transactionMessage) throws ServiceException;
	

	/**
	 * 消息确认完成
	 */
	void resultToDone(JSONObject msgJo) throws ServiceException;
	

	/**
	 * 更新消息为废弃状态
	 */
	String updateMessageToDiscard(String messageId) throws ServiceException;
	
	
	/**
	 * 根据ID获取消息信息
	 */
	TransactionMessage  getTransactionMessageById(String messageId) throws ServiceException;
	
	/**
	 * 根据状态获取消息列表
	 */
	Page<TransactionMessage>  queryTransactionMessageByState(int messageState,Pageable pageable) throws ServiceException;
	

}
