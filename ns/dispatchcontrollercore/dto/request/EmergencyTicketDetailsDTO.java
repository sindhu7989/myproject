package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyTicketDetailsDTO {
	
	private String conversationId;
	private String ticketNumber;

}
