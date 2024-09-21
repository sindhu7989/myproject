package com.straviso.ns.dispatchcontrollercore.dto;



import lombok.Data;

@Data
public class firstTicketNumberDto {
		
	String ticketExternalId;
	String conversationId;
	private String isFirstTicket;
	String actionBy;
	String technicianId;
	
	
}
