package com.straviso.ns.dispatchcontrollercore.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.AfterHrsAgentModel;

public interface AfterHrsAgentRepository extends MongoRepository<AfterHrsAgentModel, String> {

	List<AfterHrsAgentModel> findByIsActiveAndSupervisorId(String flagY, String supervisorId);

}
