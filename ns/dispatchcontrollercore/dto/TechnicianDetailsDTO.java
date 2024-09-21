package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class TechnicianDetailsDTO {
	private String startDate;
	private String endDate;
	private Integer pageNo ;
	private Integer size ;
	private String isActive = "Y";
	private String globalStatus ;
	

}