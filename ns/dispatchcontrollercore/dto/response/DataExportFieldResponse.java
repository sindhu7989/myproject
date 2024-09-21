package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class DataExportFieldResponse {
	
	@JsonProperty(value = "Result")
	private String result;
	private List<LumenStartColumn> lstdata;
}
