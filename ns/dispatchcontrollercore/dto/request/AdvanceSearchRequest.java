package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class AdvanceSearchRequest {

	private AdvanceSearchDataColumn[] searchData;
	private String searchFor;
	private Integer pageNumber;
	private Integer pageSize;
}
