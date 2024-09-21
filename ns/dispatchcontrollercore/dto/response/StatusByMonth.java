package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class StatusByMonth {
	
	private LocalDateTime createdDateTime;
    private String globalStatus;
    private String ticketNumber;

}
