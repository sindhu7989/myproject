package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTeamWorkloadTechnicianDetails {
	
	private String techId;
	private String supervisorId;
	private String techName;
	private long open;
	private long complete;
	private long availableMins;

}
