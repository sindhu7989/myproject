package com.straviso.ns.dispatchcontrollercore.entity;


import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="technicianAvailability")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianAvailability {
	
		@Id
		private String _id;
		private String technicianId; 
		private String technicianName; 
		private String supervisorId;
	    private LocalDateTime timestamp;  // internal CST localdatetime
	    private LocalDateTime calenderDate;   // date to consider for availability
	    private String jobTitle;  //default value : "Technician"
	    private double availableTime;  // In Minutes  600
		private double projectTime;  // In Minutes  600
		private LocalDateTime onCallStartDateTime;
		private LocalDateTime onCallEndDateTime;
		private String isOnCall;
		private String isWeekend;
		private String availabilityStatus; // OnLeave, Available, UnAvailable, OnTraining, HalfDayLeave
		private Map<String, String> additionalInfo = new LinkedHashMap<>();  // Internal For now

}
