package com.straviso.ns.wholesale.core.dto.response;

import lombok.Data;

@Data
public class ExportMetaDataUpdateResponse {

	private Integer statusCode;
	private String statusMessage;
	private Integer id;
	private String componentName;
	private String reportName;
	private String userId;
}
