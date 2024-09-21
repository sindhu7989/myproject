package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.Data;

@Data
public class FileWriterResponse {

	private Integer statusCode;
	private String statusMessage;
	private String filePath;
	private Long fileSize;
}
