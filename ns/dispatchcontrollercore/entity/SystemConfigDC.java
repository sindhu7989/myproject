package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="systemConfigDC")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigDC {
	
	@Id
	private String _id;
	private String configId;
	private String configTitle;
	private String configProperty;
	private String configValue;
	private String configRole;
	private String isActive;
	private String configDescription;
	private LocalDateTime createdDate; 
	private LocalDateTime updatedDate;
	private String createdBy;
	private String updatedBy;
	
}

