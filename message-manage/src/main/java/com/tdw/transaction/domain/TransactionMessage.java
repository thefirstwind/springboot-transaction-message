package com.tdw.transaction.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.http.HttpMethod;

@Document(collection = "transaction_message")
public class TransactionMessage {

	@Id
    @NotNull
	private String id;
	
	@Field("nm")
    @NotBlank
	private String serviceName;
	
	@Field("ct")
    @NotNull
	private Date createTime;
		
	@Field("mt")
	private int messageType;
	
	@Field("mtp")
    @NotBlank
	private String messageTopic;
	
	@Field("msg")
	private Object message;
	
	@Field("msgs")
    @NotNull
	private int messageState;
	
	@Field("mst")
	private int messageSendThreshold;
	
	@Field("msts")
	private int messageSendTimes;
	
	@Field("mnst")
	private Date messageNextSendTime;
	
	@Field("pbu")
	private String presendBackUrl;
	
	@Field("pbm")
	private HttpMethod presendBackMethod;
	
	@Field("pbt")
	private int presendBackThreshold;
	
	@Field("pbst")
	private int presendBackSendTimes;
	
	@Field("pbnst")
	private Date presendBackNextSendTime;
	
	@Field("rbu")
	private String resultBackUrl;
	
	@Field("rbm")
	private HttpMethod resultBackMethod;
	
	@Field("rbt")
	private int resultBackThreshod;
	
	@Field("rbnst")
	private Date resultBackNextSendTime;
	
	@Field("rst")
    @NotNull
	private String result;
	
	@Field("erst")
	private String expectResult;
	
	@Field("logs")
	private List<OptLog> logs;

	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getMessageTopic() {
		return messageTopic;
	}

	public void setMessageTopic(String messageTopic) {
		this.messageTopic = messageTopic;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public int getMessageState() {
		return messageState;
	}

	public void setMessageState(int messageState) {
		this.messageState = messageState;
	}

	public int getMessageSendThreshold() {
		return messageSendThreshold;
	}

	public void setMessageSendThreshold(int messageSendThreshold) {
		this.messageSendThreshold = messageSendThreshold;
	}

	public int getMessageSendTimes() {
		return messageSendTimes;
	}

	public void setMessageSendTimes(int messageSendTimes) {
		this.messageSendTimes = messageSendTimes;
	}

	public Date getMessageNextSendTime() {
		return messageNextSendTime;
	}

	public void setMessageNextSendTime(Date messageNextSendTime) {
		this.messageNextSendTime = messageNextSendTime;
	}

	public String getPresendBackUrl() {
		return presendBackUrl;
	}

	public void setPresendBackUrl(String presendBackUrl) {
		this.presendBackUrl = presendBackUrl;
	}

	public HttpMethod getPresendBackMethod() {
		return presendBackMethod;
	}

	public void setPresendBackMethod(HttpMethod presendBackMethod) {
		this.presendBackMethod = presendBackMethod;
	}

	public int getPresendBackThreshold() {
		return presendBackThreshold;
	}

	public void setPresendBackThreshold(int presendBackThreshold) {
		this.presendBackThreshold = presendBackThreshold;
	}

	public int getPresendBackSendTimes() {
		return presendBackSendTimes;
	}

	public void setPresendBackSendTimes(int presendBackSendTimes) {
		this.presendBackSendTimes = presendBackSendTimes;
	}

	public Date getPresendBackNextSendTime() {
		return presendBackNextSendTime;
	}

	public void setPresendBackNextSendTime(Date presendBackNextSendTime) {
		this.presendBackNextSendTime = presendBackNextSendTime;
	}

	public String getResultBackUrl() {
		return resultBackUrl;
	}

	public void setResultBackUrl(String resultBackUrl) {
		this.resultBackUrl = resultBackUrl;
	}

	public HttpMethod getResultBackMethod() {
		return resultBackMethod;
	}

	public void setResultBackMethod(HttpMethod resultBackMethod) {
		this.resultBackMethod = resultBackMethod;
	}

	public int getResultBackThreshod() {
		return resultBackThreshod;
	}

	public void setResultBackThreshod(int resultBackThreshod) {
		this.resultBackThreshod = resultBackThreshod;
	}

	public Date getResultBackNextSendTime() {
		return resultBackNextSendTime;
	}

	public void setResultBackNextSendTime(Date resultBackNextSendTime) {
		this.resultBackNextSendTime = resultBackNextSendTime;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getExpectResult() {
		return expectResult;
	}

	public void setExpectResult(String expectResult) {
		this.expectResult = expectResult;
	}

	public List<OptLog> getLogs() {
		return logs;
	}

	public void setLogs(List<OptLog> logs) {
		this.logs = logs;
	}

	public void addLog(OptLog log) {
		if(null == this.logs)
		{
			this.logs = new ArrayList<OptLog>();
		}
		this.logs.add(log);
	}

}