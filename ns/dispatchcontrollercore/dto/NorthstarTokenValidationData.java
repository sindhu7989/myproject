package com.straviso.ns.dispatchcontrollercore.dto;

public class NorthstarTokenValidationData {

	private String message;
	private boolean validToken;
	private String businessId;
	private String emailId;
	private String firstName;
	private String lastName;

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isValidToken() {
		return validToken;
	}
	public void setValidToken(boolean validToken) {
		this.validToken = validToken;
	}
	public String getBusinessId() {
		return businessId;
	}
	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}
	public String getEmailId() {
		return emailId;
	}
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}



}
