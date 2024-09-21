package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class NSUserRoleRequest {

	private String userEmailId;
	private String userBusinessId;
	private String apiUrl;
	private String jwtToken;
	private String logKey;
	
}
