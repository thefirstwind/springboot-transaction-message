package com.tdw.transaction.model.message;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(value = "SendMessage")
public class SendMessage {

    @ApiModelProperty(value = "消息 ID", required = true)
    @NotBlank
    private String messageId;


    @ApiModelProperty(value = "消息", required = true)
    @NotNull
    private Object message;


    @Override
    public String toString() {
        return "SendMessage{" +
                ", messageId='" + messageId +
                ", message='" + message.toString() +
                '}';
    }


	public String getMessageId() {
		return messageId;
	}


	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}


	public Object getMessage() {
		return message;
	}


	public void setMessage(Object message) {
		this.message = message;
	}

    
}
