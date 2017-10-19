package com.tdw.transaction.constant;

/**
 * 消息状态定义
 * @author DELL
 *
 */
public enum MessageState {

	PRESEND(10, "预发送"), ABNORMAL(11, "异常"), SEND(20, "发送"), DIED(21, "死亡"), DONE(30, "完成"), DISCARD(100, "废弃");

	private final int code;

	private final String message;

	MessageState(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int code() {
		return code;
	}

	public String message() {
		return message;
	}
	
	public MessageState isValidCode(int _code)
	{
		return MessageState.valueOf("");
	}
	
}
