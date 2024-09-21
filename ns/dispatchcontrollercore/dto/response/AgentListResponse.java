package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class AgentListResponse {
	
	List<Object> responseList;
	Integer count;

}
