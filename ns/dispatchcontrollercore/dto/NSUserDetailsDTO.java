package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class NSUserDetailsDTO {

	private String userType;
	private String partnerId;
	private Integer userId;
	private Integer statusCode;
	private String statusMessage;
	
}
