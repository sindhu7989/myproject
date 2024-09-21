package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class CallTechnicianDto {
	
	private String ticketNumber;
	private String technicianName;
	private String phoneNumber;
	private String message;

}
