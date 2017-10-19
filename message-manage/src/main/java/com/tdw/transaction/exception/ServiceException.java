package com.tdw.transaction.exception;

import com.tdw.transaction.constant.BZStatusCode;

public class ServiceException extends RuntimeException {	
	
	private static final long serialVersionUID = -8930391991543496652L;

	private int code;
	
	private String message;


	public ServiceException() {
	}

	public ServiceException(int code,String message) {
		this.setCode(code);
		this.setMessage(message);
	}

	public ServiceException(BZStatusCode bzStatusCode) {
		this.setCode(bzStatusCode.code());
		this.setMessage(bzStatusCode.message());
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
	

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}