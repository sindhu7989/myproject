package com.straviso.ns.dispatchcontrollercore.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "updatedTicketScoreData")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentTicketRiskScore {
	
	@Id
    private String id;
    private LocalDateTime createdDate;
    private List<UpdatedTicketScoreData> updatedTicketScoreData;
    
}
