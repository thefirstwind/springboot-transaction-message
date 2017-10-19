package com.tdw.transaction.service;

import com.tdw.transaction.exception.ServiceException;

public interface AbnomalProcessMessageService {
	
	/**
	 * 预发送回调任务
	 * @throws ServiceException
	 */
	void preSendCallbackByTask()  throws ServiceException;

	/**
	 * 发送消息任务
	 * @throws ServiceException
	 */
	void sendToMQByTask()  throws ServiceException;

	/**
	 * 结果回调任务
	 * @throws ServiceException
	 */
	void resultCallbackByTask()  throws ServiceException;
	

}
