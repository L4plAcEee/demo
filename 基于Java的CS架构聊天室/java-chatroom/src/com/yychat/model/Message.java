package com.yychat.model;
import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable,MessageType{
	User user;
	String sender;
	String recevier;
	String content;
	String messageType;
	Date sendTime;
	private static final long serialVersionUID = 1L;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user=user;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime() {
		this.sendTime=new Date();
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender=sender;
	}
	public String getRecevier() {
		return recevier;
	}
	public void setRecevier(String recevier) {
		this.recevier=recevier;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content=content;
	}
}
