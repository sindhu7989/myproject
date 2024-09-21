package com.straviso.ns.dispatchcontrollercore.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.DCSolverTaskAuditModel;

public interface DCSolverTaskAuditRepo extends MongoRepository<DCSolverTaskAuditModel, String> {

	DCSolverTaskAuditModel findByDcSolverProcessIdAndPolygonListIn(Long dcSolverProcessAuditId, List<String> asList);

	DCSolverTaskAuditModel findByDcSolverProcessIdAndSupervisorId(long dcSolverProcessId, String supervisorId);
	
}