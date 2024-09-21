package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ActionByMonth {
	
	private LocalDateTime createdDateTime;
    private String actionOnTicket;
    private String ticketNumber;


}
