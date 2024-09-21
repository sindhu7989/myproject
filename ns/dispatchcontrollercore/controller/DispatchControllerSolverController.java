package com.straviso.ns.dispatchcontrollercore.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.entity.ConstraintConfig;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.service.ConstraintConfigService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@RestController
@CrossOrigin(origins = "*")
@Log4j2
@RequestMapping("/core")
public class DispatchControllerSolverController {

	@Autowired
	ConstraintConfigService constraintConfigService;

	@GetMapping("/testing")
	public Object testing() {
		log.info("Dispatch Controller Core Test API Request Received for business: {}", BusinessContext.getTenantId());
		return "Dispatch Controller Core is Up & Running!!!";
	}
	
	private double getObjectSize(Object input, String logKey) {
		try {
		Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
		 String inputString = gson.toJson(input);
		return input != null && !inputString.trim().isEmpty() ? (double) inputString.getBytes().length / 1000
				: 0;
		}catch (Exception e) {
			log.info("{} Unable to find size due to {} " ,logKey,e.getMessage());	
			return 0;
		}
	}

	@ApiOperation(value = "This API is used to get the records of ConstraintConfig  Details")
	@GetMapping("/getAllConstraintConfigDetails")
	public ResponseEntity<?> getAllConstraintConfigDetails() {
		String businessId = BusinessContext.getTenantId();
		log.info("received request to get all constraint config details for business: {}", businessId);
		return constraintConfigService.getAllConstraintConfigDetails();
	}
	
	@ApiOperation(value = "This API is used to update the details of ConstraintConfig  Based on   ConstraintName and display to UI")
	@PostMapping("/updateByConstraintName")
	public List<Document> updateByConstraintName(@RequestBody List<ConstraintConfig> configDetailsList,
			HttpServletRequest servletRequest) {
		String logKey = DispatchControllerConstants.FLAG_Y;
		String businessId = BusinessContext.getTenantId();
		String businessToken = BusinessTokenContext.getBusinessToken();
		log.info("{} received request to update constraint config details for business: {}", logKey, businessId,businessToken);
		
		long startTimes = System.currentTimeMillis();
		
		List<Document> updatedConfigs = constraintConfigService.updateByConstraintName(configDetailsList,businessId,businessToken);

		log.info("T-ID : {} , ****#### updateByConstraintName API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
				
		log.info("{} completed for business: {}", logKey, businessId,businessToken);
		return updatedConfigs;
	}

}
