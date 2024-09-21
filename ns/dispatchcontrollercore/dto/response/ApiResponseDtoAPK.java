package com.straviso.ns.dispatchcontrollercore.dto.response;


import lombok.Data;

@Data
public class ApiResponseDtoAPK {
	
	private String status;
	private String message;
	private Object responseData;
	private String responseMessage;
	
}
