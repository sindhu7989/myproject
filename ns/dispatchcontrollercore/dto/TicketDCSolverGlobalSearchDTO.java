package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class TicketDCSolverGlobalSearchDTO {
	private String startDate;
	private String endDate;
	private int pageNo;
	private int pageSize;
	private String supervisorId ;
	private String globalStatus ;
	private String searchText ;
	

}
