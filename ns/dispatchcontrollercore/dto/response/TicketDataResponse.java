package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TicketDataResponse {

	private String ticketId;
	private String workType;
	private String workZip;
	private String workAddress;
	private Long ticketScore;
	private Long timeEstimate;
	private String street;
	private String workCity;
	private String workState;
	private String ticketType;
	private String status;
	private String technicianName;
	private String technicianId;
	private String supervisorName;
	private String supervisorId;
	private LocalDateTime assignedDate;
	private LocalDateTime dueDate;
	private LocalDateTime createdDateTime;
	private String ticketNumber811;
	private LocalDateTime assignmentDateTime;
}
