package com.straviso.ns.dispatchcontrollercore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LumenAdditionalInfo {

	@JsonProperty(value = "ParameterName")
	private String ParameterName;
	
	@JsonProperty(value = "ParameterValue")
	private String ParameterValue;
}
