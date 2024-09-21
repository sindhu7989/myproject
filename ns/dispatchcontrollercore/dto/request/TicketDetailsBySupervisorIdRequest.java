package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class TicketDetailsBySupervisorIdRequest {

	private String supervisorId;
	private Integer pageNo;
	private Integer pageSize;
	private String startDate;
	private String endDate;
	private String ticketStatus;
	
}
