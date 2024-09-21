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
public class TicketTrailsCollectionDTO {
	

	private String conversationId;
	private String ticketExternalId;      // For External Use , DC have value of ticketExternalId in ticketNumber
	private String ticketStage;  //UnAssigned / Assigned / Cancelled / Completed / MissingInfo
	private String action;
	private String actionBy;
	private LocalDateTime auditDateTime;
	private String preAction;
	private String postAction;
	private String remarks;
	private String error;
	
}
