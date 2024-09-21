package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.ToString;

@Document(collection="technicianAssignmentSolution")
@Data
@ToString
public class AgentAssignmentSolutionModel {

	@Id
    private String _id;
	private long dcSolverProcessId;
	private long dcSolverTaskId;
	private LocalDateTime timestamp;
	private Agent agent;
	private Map<String, String> additionalInfo = new LinkedHashMap<>(); 
}
