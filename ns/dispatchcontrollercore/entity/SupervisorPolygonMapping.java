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

@Document(collection = "supervisorPolygonMappingDCSolver")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class SupervisorPolygonMapping {

	@Id
	private String _id;
	private String supervisorId;
	private String firstName;
	private String lastName;
	private String emailId; 
	private String isActive; 
	private String managerName;
	private String managerId;
	private long supervisorExplevel; 
	private String supervisorAvailability;
	private String supervisorPolygonId;
	private List<String> polygonList;
	private String phoneNumber;
	private String jobTitle;
	private Location location;
	private String city;
	private String county;
	private String state;
	private Map<String, String> additionalInfo = new LinkedHashMap<>();
	private String availabilityStatus; // OnLeave, UnAvailable, OnTraining, HalfDayLeave, MultiDayTicket
	private List<Ticket> ticketList = new ArrayList<>();
	private String autoAssignment = DispatchControllerConstants.YES;
	
	}




