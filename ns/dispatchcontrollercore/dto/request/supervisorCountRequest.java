package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class supervisorCountRequest {

	private String supervisorId;
	private String startDate;
	private String endDate;

}
