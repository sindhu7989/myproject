package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class AdvanceSearchDataColumn {

	
	private String columnName;
	private String operator;
	private Object value;
}
