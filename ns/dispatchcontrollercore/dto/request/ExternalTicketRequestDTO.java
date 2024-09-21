package com.straviso.ns.dispatchcontrollercore.dto.request;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.straviso.ns.dispatchcontrollercore.entity.Location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalTicketRequestDTO {
	
	private String conversationId;
	private String ticketNumber;
	private String ticketNumber811;
	private String masterTicketExternalId;
	private String emergencyFlag;
	private String afterHours;
	private String multiTechnicianTicket;
	private String isAssistTicket;
	private String isFirstTicket;
	
	private String polygonId;
	private long ticketETA ;  // in minutes
	private List<String> certificationRequired;
	private long ticketScore;
	private String ticketDueDateAndTime;
	
	private String supervisorId;
	private String supervisorName;
	private String supervisorPolygonId;
	
	private Location location;
	private String ticketType; 
	private long ticketPriority;
	private String createdDateTime;
	private String ticketStatus; //globalStatus
	
	private String workAddress;
	private String workType;
	private String workCity;
	private String workState;
	private String workCounty;
	private String workStreet;
	private String workZip;
	
	private String technicianId;
	private String technicianFirstName;
	private String technicianLastName;
	private String technicianEmailId;
	private String actionOnTicket; //Transfered / BackToQueue / Cancelled / UnAssigned
	
	private String assignmentType; // StickyRouting/upcoming ticketscenarios 
	
	/*private long customerZip; 
	private String workPlace;
	private String customerCity;
    private String skills;
	private String workBeginDateTime;
	private String workCompletionDateTime;
	private String globalStatus;  //UnAssigned / Assigned / Cancelled / Completed / MissingInfo
	private String actionOnTicket; //Transfered / BackToQueue / Cancelled / NoAction
	private String intialTicketStatus; //Active / MissingInfo
	private String damageControl;
	private String riskScore;
	private String weatherCode;*/
	
}
