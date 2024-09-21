package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAppResponse {
	
	private String error;
	private String errorMessage;
	private Object data ;

}
