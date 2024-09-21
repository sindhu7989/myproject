package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class ColumnFilters {

	private String column;
	private String 	image;
	private String 	value;
	private String operator;
}
