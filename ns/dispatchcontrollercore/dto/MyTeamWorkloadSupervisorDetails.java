package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTeamWorkloadSupervisorDetails {
	
	private String supervisorId;
	private String supervisorName;
	private long open = 0;
	private long complete = 0;
	private long availableMin = 0;

}
