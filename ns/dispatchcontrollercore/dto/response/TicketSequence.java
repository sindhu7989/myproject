package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TicketSequence {


	public TicketSequence() {
		// TODO Auto-generated constructor stub
	}
	private String conversationId;
	private String ticketNumber;
	private String isFirstTicket = DispatchControllerConstants.NO;
	private String ticketType;
	private String globalStatus;  
	private LocalDateTime ticketDueDateAndTime;
	private LocalDateTime completionDateTime;
	
	
}
