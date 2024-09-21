package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.Data;

@Data
public class CockpitBubbleCountStatRequest {

	private String startDate;
	private String endDate;
	private String supervisorId;
}
