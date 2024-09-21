package com.straviso.ns.dispatchcontrollercore.dto.request;

import java.util.ArrayList;
import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyTicketRequest {
	
	private List<EmergencyTicketDetailsDTO> emergencyTicketList = new ArrayList<>();

}
