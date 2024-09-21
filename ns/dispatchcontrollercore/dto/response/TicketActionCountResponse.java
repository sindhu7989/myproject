package com.straviso.ns.dispatchcontrollercore.dto.response;

import lombok.Data;

@Data
public class TicketActionCountResponse {

	
	private Long totalCount;
	private Long transfered;
	private Long cancelled;
	private Long backToQueue;
	private Long assigned;
	private Long unAssigned;
	
}
