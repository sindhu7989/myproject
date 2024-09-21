package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"dateAndTime","availableTime","totalWorkHourGlobal"})
public class TechnianWorkLoadDto {
	
	private LocalDateTime dateAndTime;
	private long availableTime;
	private long totalWorkHourGlobal = 0;
	private String technicianId;
	private String technicianName;
	
}
