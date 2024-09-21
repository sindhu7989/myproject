package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.List;

@lombok.Data
public class StateCountData {
	
	private String name;
	private long value;
	private String drilldown;
	private List<Data> data;

}
