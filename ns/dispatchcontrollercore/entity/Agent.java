package com.straviso.ns.dispatchcontrollercore.entity;

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

@Document(collection = "technicianDCSolver")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Agent {

	@Id
	private String _id;
	private String technicianId;
	private String firstName;
	private String lastName;
	private String emailId; 
	private String cuid;
	private String address;
	private String isActive;  // Y or N
	private String supervisorName;
	private String supervisorId;
	private long technicianExplevel; 
	private String technicianAvailability;
	private String polygonId;
	private String tenure;
	private String phoneNumber;
	private String jobTitle;
	private Location location;
	private String skills;
	private long defaultAvailableTime; // Total time in minutes that an agent is expected to work
	private long availableTime;  //Estimated time in minutes that an agent can work on a given day
	private long calculatedAvailableTime = 0;  //time in minutes calculated as Technician Availability Time will be increased by Technician Addition Availability Time Percentage
	private String city;
	private String county;
	private String state;
	private String vehicleType;
	private long totalWorkHourGlobal = 0; // Time in minutes that agent is assigned 
	private String agentType;
	private Map<String, String> additionalInfo = new LinkedHashMap<>();
	private String assignmentStatus; //UnderAssigned | OverAssigned | IdealAssignment
	private String availabilityStatus; // OnLeave, UnAvailable, OnTraining, HalfDayLeave, MultiDayTicket
	private long technicianScore;
	private List<String> certificationList = new ArrayList<>();

	private List<Ticket> ticketList = new ArrayList<>();

	private long remainingWorkTime = 600;  // in minutes
	private double evaluatedDistance = 0.0;  // in Meters
	private String autoAssignment = DispatchControllerConstants.YES;
}
