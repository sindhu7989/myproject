package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class UpdatedTicketScoreData {
	
	private String technicianId;
    private double employeeQualityScore;
    private String continuousServiceDate;
    private String mostRecentHireDate;
    private double preTicketRiskScore;
    private double postTicketRiskScore;

}
