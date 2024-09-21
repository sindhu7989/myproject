package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;

import lombok.AllArgsConstructor;
import lombok.Data;

@Document(collection="ticketDCSolver")
@Data
@AllArgsConstructor
public class Ticket {

	@Id
    private String _id;
	private String conversationId;
	private String ticketNumber;
	private String masterTicketExternalId;
	private String ticketNumber811 = "";
	private LocalDateTime createdDateTime;
	private String polygonId;
	private Location location;
	
	private long ticketETA ;  // in minutes
	private long actualTimeSpent = 0; // in minutes ticketETA + beak time + travel Time
	private long ticketPriority;
	private String ticketType;
	private long ticketScore;
	private LocalDateTime ticketDueDateAndTime;
	private List<String> certificationRequired = new ArrayList<>();
	private String emergencyFlag;
    private String afterHours;
    private String isAssistTicket;
	private String assistPresent=DispatchControllerConstants.NO; //if Yes then assistTicket is created at DC side for main ticket else assistTicket is not created
    private String multiTechnicianTicket;
    private String isFirstTicket = DispatchControllerConstants.NO;
	
	private String globalStatus;  //UnAssigned / Assigned / Cancelled / Completed / MissingInfo
	private String actionOnTicket; //Transfered / BackToQueue / Cancelled / UnAssigned / Assigned
	private String internalStatus; //failed at different level
	private String intialTicketStatus; //Active / MissingInfo
	private long transferCount;
	private LocalDateTime assignmentDateTime;
	private LocalDateTime completionDateTime;
	
	private String workAddress;
	private String workStreet;
	private String workType;
	private String workState;
	private String workCounty;
	private String workCity;
	private String workZip;
    private String skills;
	
	private String technicianId;
	private String technicianFirstName;
	private String technicianLastName;
	private String technicianEmailId;
	private String supervisorName;
	private String supervisorId;
	private String supervisorPolygonId;
	
	
	private List<TicketActionTrail> ticketActionTrails = new ArrayList<>();
	private Map<String, String> additionalInfo = new LinkedHashMap<>(); 

    public Ticket() {
	}
	
   
}
