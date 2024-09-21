package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "technicianDCSolver")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentTS {


	private String technicianId;
	private List<TicketSequence> ticketList = new ArrayList<>();

	
}
