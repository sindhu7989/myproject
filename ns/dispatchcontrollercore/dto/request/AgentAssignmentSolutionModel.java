package com.straviso.ns.dispatchcontrollercore.dto.request;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.straviso.ns.dispatchcontrollercore.dto.response.TicketSequence;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;

import lombok.Data;
import lombok.ToString;

@Document(collection="technicianAssignmentSolution")
@Data
@ToString
public class AgentAssignmentSolutionModel {

	
	private String technicianId;
	private List<TicketSequence> ticketList = new ArrayList<>();


}
