package com.straviso.ns.dispatchcontrollercore.dto;

public class NorthstarTokenValidationStatus {
	
	private int statusCode;
	private String statusMsg;
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getStatusMsg() {
		return statusMsg;
	}
	public void setStatusMsg(String statusMsg) {
		this.statusMsg = statusMsg;
	}
}
