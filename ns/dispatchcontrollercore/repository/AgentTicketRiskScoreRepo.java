package com.straviso.ns.dispatchcontrollercore.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.straviso.ns.dispatchcontrollercore.dto.AgentTicketRiskScore;

@Repository
public interface AgentTicketRiskScoreRepo extends MongoRepository <AgentTicketRiskScore, String> {
	

}
