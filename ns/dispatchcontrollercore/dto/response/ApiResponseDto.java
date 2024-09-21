package com.straviso.ns.dispatchcontrollercore.dto.response;


import lombok.Data;

@Data
public class ApiResponseDto {
	
	private String status;
	private String message;
	private Object responseData;
}
