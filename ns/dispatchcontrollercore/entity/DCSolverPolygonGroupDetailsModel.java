package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDate;
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
import lombok.NoArgsConstructor;

@Document(collection="dcSolverPolygonGroupDetails")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DCSolverPolygonGroupDetailsModel {
	
	@Id
    private String _id;
	private long polygonGroupId;
	private LocalDateTime timestamp; 
	private List<Long> polygonList = new ArrayList<>();
	private long totalTicketsCount;
	private long totalTechnicianCount;
	private LocalDate assignmentDate;
	private String globalStatus;
	private String dcSolverStatus;
	private String dcSolverStatusMessage;
	private String isActive = DispatchControllerConstants.FLAG_Y;
	private Map<String, String> additionalInfo = new LinkedHashMap<>();
}

