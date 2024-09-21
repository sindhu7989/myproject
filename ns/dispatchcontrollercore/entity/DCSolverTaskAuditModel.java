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

@Document(collection="dcSolverTaskAudit")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DCSolverTaskAuditModel {
	
	@Id
    private String _id;
	private long dcSolverProcessId;
	private long dcSolverTaskId;
	private LocalDateTime timestamp; 
	private LocalDateTime taskStartDateTime;
	private LocalDateTime taskCompletionDateTime;
	private List<String> polygonList;
	private long polygonGroupId;
	private String globalStatus;
	private String status;
	private String statusMessage;
	private long totalTechnicianCount; 
	private long unassignedTechnicianCount;
	private long totalTicketCount; 
	private long unassignedTicketCount;
	private Map<String, String> additionalInfo = new LinkedHashMap<>();
	private String supervisorId;


}
