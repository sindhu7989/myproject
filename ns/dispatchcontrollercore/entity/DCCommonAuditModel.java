package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="dcCommonAuditModel")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DCCommonAuditModel {
	
	@Id
    private String _id;
	private String component;
	private String logEvent;
	private LocalDateTime logEventTime;
	private String action;
	private String actionBy;
	private Map<String, String> additionalInfo = new LinkedHashMap<>();
	private List<String> perviousData = new ArrayList<>();
	private List<String> updatedData = new ArrayList<>();

}