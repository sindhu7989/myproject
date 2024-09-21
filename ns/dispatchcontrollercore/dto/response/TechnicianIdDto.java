package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.List;

import lombok.Data;
@Data
public class TechnicianIdDto {
	
	List<String> technicianIds;
	private String startDate;
	private String endDate;

}
