package com.straviso.ns.dispatchcontrollercore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LumenStartColumn {
	
	@JsonProperty(value = "ColumnName")
	private String columnName;
}
