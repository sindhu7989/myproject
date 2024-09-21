package com.straviso.ns.dispatchcontrollercore.dto;

import java.util.List;

import lombok.Data;

@Data
public class TransferTicketDTO {
	private String actionBy;

	private String fromTechnicianId;

	private String toTechnicianId;

	private String ticketNumbers;
	
	private String fromSupervisorId;

	private String toSupervisorId;

}