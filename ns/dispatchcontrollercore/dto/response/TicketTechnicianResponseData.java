package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.time.LocalDateTime;
import lombok.Data;


@Data
public class TicketTechnicianResponseData {

	private String ticketId;
	private String ticketType;
	private String workType;
	private Long ticketScore;
	private Long technicianScore;
	private String emergency;
	private String status;
	private Long estTicketDuration;
	private LocalDateTime assignedDate;
	private LocalDateTime dueDate;
	private LocalDateTime createdDateTime;
	private LocalDateTime assignmentDateTime;
	private LocalDateTime receivedDate;
	private LocalDateTime closedDate;
	private String technicianId;
	private String technicianName;
	private String supervisorId;
	private String supervisorName;
	private String userId;
	private String employeeId;
	private String subGroup;
	private String district;
	private String workCity;
	private String workState;
	private String workZip;
	private String workAddress;
	private Integer routeIndex;
	private String startType;
	private Double startLat;
	private Double startLon;
	private Double evalMiles;
	private Long availableCapacity;
	private Long assignmentEta;
	private String ticketNumber811;
	private String skill;
	private Long ticketPriority;
	private Long driveTime;
	private Long totalDriveTime;
	private Double distance;
	
}
