package com.straviso.ns.dispatchcontrollercore.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import com.straviso.ns.dispatchcontrollercore.entity.TicketActionTrail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LumenCollectionUpdateDTO {

	@Transient
	private String ticketNumber;
	@Transient
	private String conversationId;
	private String technicianId;
	private String technicianFirstName;
	private String technicianLastName;
	private String technicianEmailId;
	private String supervisorName;
	private String supervisorId;
	private LocalDateTime assignmentDateTime;
	private String globalStatus;  //UnAssigned / Assigned / Cancelled / Completed / MissingInfo
	private String actionOnTicket; //Transfered / BackToQueue / Cancelled / NoAction / Assigned
	private TicketActionTrail ticketActionTrails ;
//	private String ticketExternalId;      // For External Use , DC have value of ticketExternalId in ticketNumber
	private String remarks;
	private String error;
}
