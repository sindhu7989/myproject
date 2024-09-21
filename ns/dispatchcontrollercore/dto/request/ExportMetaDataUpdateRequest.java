package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class ExportMetaDataUpdateRequest {

	private Integer id;
	private Integer serviceCode;
	private String serviceName;
	private String componentName;
	private String reportName;
	private String userId;
	private String triggerDate;
	private String fileName;
	private String filePath;
	private String validTill;
	private Long size;
	private String status;
}
