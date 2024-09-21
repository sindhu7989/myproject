package com.straviso.ns.dispatchcontrollercore.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.DCSolverProcessAuditModel;

public interface DCSolverProcessAuditRepo extends MongoRepository<DCSolverProcessAuditModel, String> {

	List<DCSolverProcessAuditModel> findByTimestampBetweenOrderByDcSolverProcessIdDesc(LocalDateTime startDate,
			LocalDateTime latestDate);
	
	
}