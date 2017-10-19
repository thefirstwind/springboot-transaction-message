package com.tdw.transaction.util;

import com.tdw.transaction.constant.BZStatusCode;

public class Result<T> {

	private int status;
	
    private T data;

    private String message;

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public Result(int status, String message,T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Result(T data) {
        this.status = BZStatusCode.OK.code();
        this.message = BZStatusCode.OK.message();
        this.data = data;
    }

    public Result(int status,T data) {
        this.status = status;
        this.message = "OK";
        this.data = data;
    }

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
