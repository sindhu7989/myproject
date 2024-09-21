package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePageDto {
	private Integer statusCode;
	private String statusMessage;
	private Object responseData;
	private long totalPages;
	private long totalElements;

}
