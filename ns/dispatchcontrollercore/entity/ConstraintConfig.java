package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;

import javax.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="constraintconfigDCSolver")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintConfig {
	
	@Id
	private String _id;
	private String constraintId;
	private String constraintName;
	private String constraintType;
	private String constraintScore;
	private String isActive;
	private LocalDateTime createdDate; 
	private LocalDateTime updatedDate;
	private String createdBy;
	private String updatedBy;
	private String constraintTitle;
}
