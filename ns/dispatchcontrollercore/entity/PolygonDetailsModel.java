package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection="polygonDetails")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolygonDetailsModel {
	
	@Id
    private String _id;
	private long polygonId;
	private LocalDateTime timestamp; 
	private List<Long> adjacentPolygonList;
	private String city;  // town
	private String county;
	private String state;
	private String country;
	private long zipCode;
	private Map<String, String> additionalInfo = new LinkedHashMap<>();
	private String createdBy;
	private LocalDateTime createdDateTime;
	private String updatedBy;
	private LocalDateTime updatedDateTime;
}