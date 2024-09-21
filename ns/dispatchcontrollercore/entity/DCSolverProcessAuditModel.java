package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="dcSolverProcessAudit")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DCSolverProcessAuditModel {
	
	@Id
    private String _id;
	private long dcSolverProcessId;
	private LocalDateTime timestamp; 
	private LocalDateTime processStartDateTime;
	private LocalDateTime processCompletionDateTime;
	private long totalTechnicianCount; 
	private long unassignedTechnicianCount;
	private long totalTicketCount; 
	private long unassignedTicketCount;
	private long totalTaskCount;
	private long completedTaskCount;
	private long inProgressTaskCount;
	private long failedTaskCount;
	private String processTriggerType;
	private Map<String,List<String>> polygonGroupListMap = new LinkedHashMap<>();
	private String globalStatus;
	private String globalStatusMessage;
	private Map<String, String> additionalInfo = new LinkedHashMap<>();

}