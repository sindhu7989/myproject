package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianResponseDto {
	
	private Integer statusCode;
	private String statusMessage;
	private Object responseData;

}
