package com.straviso.ns.dispatchcontrollercore.service;

import java.util.List;

import org.bson.Document;
import org.springframework.http.ResponseEntity;

import com.straviso.ns.dispatchcontrollercore.entity.ConstraintConfig;

public interface ConstraintConfigService {

	ResponseEntity<?> getAllConstraintConfigDetails();

	public List<Document> updateByConstraintName(List<ConstraintConfig> configDetailsList,String businessId,String businessToken);

}
