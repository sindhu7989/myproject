package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class DashboardRequest {

	private String startDate;
	private String endDate;
	private Integer pageId;
	private Integer size;
}
