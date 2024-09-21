package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTeamWorkloadRequest {

	private String supervisorId;

	private String startDateTime;

	private String endDateTime;

}
