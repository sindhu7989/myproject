package com.straviso.ns.dispatchcontrollercore.dto.request;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@Data
public class TechnicianAvailabilityRequest {
	
	List<TechnicianAvailabilityDTO> technicianAvailability = new ArrayList<>();

}
