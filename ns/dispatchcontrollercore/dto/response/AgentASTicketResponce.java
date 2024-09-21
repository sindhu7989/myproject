package com.straviso.ns.dispatchcontrollercore.dto.response;


import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Document(collection = "technicianAssignmentSolution")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentASTicketResponce {

	
	private AgentTS agent;


}
