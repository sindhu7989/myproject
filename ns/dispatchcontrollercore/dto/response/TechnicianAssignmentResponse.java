package com.straviso.ns.dispatchcontrollercore.dto.response;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Data;

@Data
public class TechnicianAssignmentResponse {
	
	private String technicianId;
	private String ticketNumber;
	private String ticketType;
	private String workType;
	private long estTicketDuration;
	private String emergency;
	private LocalDateTime dueDate;
	private LocalDateTime closedDate;
	private String technicianName;
	private String userId;
	private String employeeId;
	private String supervisorName;
	private String state;
	private LocalDateTime receivedDate;
	private String city;
	private Integer index;
	private Double latitude;
	private Double longitude;
	private Double distance;
	private long availableCapacity;
	private long assignmentETA;
	private long technicianScore;
	private String ticketNumber811;
	private LocalDateTime assignmentDateTime;
	private long ticketScore;
	

}
