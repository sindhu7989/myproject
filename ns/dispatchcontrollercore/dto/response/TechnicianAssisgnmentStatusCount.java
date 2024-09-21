package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.Data;

@Data
public class TechnicianAssisgnmentStatusCount {
	
	private Long totalCount;
	private Long underAssigned;
	private Long overAssigned;
	private Long idealAssignment; 

}
