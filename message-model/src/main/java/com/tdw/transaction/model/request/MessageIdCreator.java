package com.tdw.transaction.model.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value = "MessageIdCreator")
public class MessageIdCreator {

    @ApiModelProperty(value = "服务名")
    @Size(max = 4)
    private String serviceName;

    @ApiModelProperty(value = "消息 Topic", required = true)
    @NotBlank
    @Size(max = 40)
    private String messageTopic;


    @ApiModelProperty(value = "消息", required = true)
    @NotNull
    private Object message;

    @ApiModelProperty(value = "消息类型", required = true)
    private int messageType;

    @ApiModelProperty(value = "期望结果", required = true)
    @NotBlank
    @Size(max = 10)
    private String expectResult;

    @ApiModelProperty(value = "预发送确认回调URL", required = true)
    @NotBlank
    @Size(max = 256)
    private String presendBackUrl;

    @ApiModelProperty(value = "结果反馈回调URL", required = true)
    @NotBlank
    @Size(max = 256)
    private String resultBackUrl;

    @Override
    public String toString() {
        return "MessageIdCreator{" +
                ", messageTopic='" + messageTopic +
                ", message='" + message.toString() +
                ", messageType='" + messageType +
                ", expectResult='" + expectResult +
                ", presendBackUrl='" + presendBackUrl +
                ", resultBackUrl='" + resultBackUrl +
                '}';
    }

    
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getExpectResult() {
		return expectResult;
	}

	public void setExpectResult(String expectResult) {
		this.expectResult = expectResult;
	}

	public String getPresendBackUrl() {
		return presendBackUrl;
	}

	public void setPresendBackUrl(String presendBackUrl) {
		this.presendBackUrl = presendBackUrl;
	}

	public String getResultBackUrl() {
		return resultBackUrl;
	}

	public void setResultBackUrl(String resultBackUrl) {
		this.resultBackUrl = resultBackUrl;
	}
    
}
