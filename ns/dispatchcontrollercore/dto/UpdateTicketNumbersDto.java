package com.straviso.ns.dispatchcontrollercore.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class UpdateTicketNumbersDto {
		
	String technicianId;
	String ticketStatus;
	String ticketNumber;
	String actionBy;
	private long actualTimeSpent = 0;
	
	
}
