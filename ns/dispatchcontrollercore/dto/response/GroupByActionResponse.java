package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class GroupByActionResponse {
	
	private Map<String, String> response = new LinkedHashMap<>();

}
