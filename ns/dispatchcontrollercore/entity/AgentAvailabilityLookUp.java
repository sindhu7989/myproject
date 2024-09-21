package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="technicianUnAvailabilityLookUp")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentAvailabilityLookUp {
	
	@Id
    private String _id;
    private String technicianId; 
    private long polygonId;
    private Location location;
    private LocalDateTime timestamp;
    private LocalDate calenderDate;
    private long technicianExplevel;
    private String jobTitle;
    private long timeoff;  // In Minutes  600
    private String city;
	private String state;
	private String county;
	private String vehicleType;
	private String availabilityStatus; // OnLeave,Available, UnAvailable, OnTraining, HalfDayLeave, MultiDayTicket, SpecialAssignment
	private Map<String, String> additionalInfo = new LinkedHashMap<>();
	private Ticket ticket;

}
