package com.tdw.transaction.domain;

import java.util.Date;

import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.mapping.Field;

public class OptLog {
	
	@Field("b")
    @Size(max = 128)
	private int beginState;
	@Field("e")
    @Size(max = 128)
	private int endState;
	@Field("log")
	private String optionLog;
	@Field("m")
	private String optionMethod;
	@Field("t")
	private Date optionTime;
	
	
	public int getBeginState() {
		return beginState;
	}
	public void setBeginState(int beginState) {
		this.beginState = beginState;
	}
	public int getEndState() {
		return endState;
	}
	public void setEndState(int endState) {
		this.endState = endState;
	}
	public String getOptionLog() {
		return optionLog;
	}
	public void setOptionLog(String optionLog) {
		this.optionLog = optionLog;
	}
	public String getOptionMethod() {
		return optionMethod;
	}
	public void setOptionMethod(String optionMethod) {
		this.optionMethod = optionMethod;
	}
	public Date getOptionTime() {
		return optionTime;
	}
	public void setOptionTime(Date optionTime) {
		this.optionTime = optionTime;
	}
	
	
}