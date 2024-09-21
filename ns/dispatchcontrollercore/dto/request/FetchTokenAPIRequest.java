package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class FetchTokenAPIRequest {
	
	private String emailId;
	private String password;
	private String businessId;
	public FetchTokenAPIRequest(String emailId, String password, String businessId) {
		super();
		this.emailId = emailId;
		this.password = password;
		this.businessId = businessId;
	}
	public FetchTokenAPIRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

}