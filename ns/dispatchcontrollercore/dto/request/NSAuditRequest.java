package com.straviso.ns.dispatchcontrollercore.dto.request;



import javax.persistence.Id;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NSAuditRequest {
	
	@Id
	private String _id;
	private String conversationId;
	private String topic;    
	private String topicIdentifier;    
	private String transactionId;    
	private String process;  
	private String action;
	private String actionBySystem;  
	private String preAction;
	private String postAction;
	private String actionByUser;
	private double latitude;
	private double longitude;
	private String remarks;
	private String error;
	
}
