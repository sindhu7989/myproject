package com.straviso.ns.dispatchcontrollercore.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CalenderViewDto {
	
	private String startDate;
	private String endDate;
	private int pageNo;
	private int pageSize;
	String technicianId;
	List<String> ticketNumbers;
	//private String dcSolverProcessIdStart;
	//private String dcSolverProcessIdEnd;
	//private String isActive;


}
