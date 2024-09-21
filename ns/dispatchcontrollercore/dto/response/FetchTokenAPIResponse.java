package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.Data;

@Data
public class FetchTokenAPIResponse {

	private String status;
	private String message;
	private String token;
}
