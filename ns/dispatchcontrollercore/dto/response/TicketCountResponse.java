package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.Data;

@Data
public class TicketCountResponse {
	
	private Long totalCount;
	private Long completed;
	private Long unassigned;
	private Long missingInfo;
	private Long assigned;
	private Long cancelled;

}
