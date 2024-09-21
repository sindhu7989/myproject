package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.straviso.ns.dispatchcontrollercore.dto.MyTeamWorkloadSupervisorDetails;
import com.straviso.ns.dispatchcontrollercore.dto.MyTeamWorkloadTechnicianDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTeamWorkloadResponse {
	
	private MyTeamWorkloadSupervisorDetails supervisorDetail;
	private long totalAvailableMins;
	private long totalOpenTickets;
	private long completedTickets;
	private List<MyTeamWorkloadTechnicianDetails> techList = new ArrayList<>();

}
