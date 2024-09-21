package com.straviso.ns.dispatchcontrollercore.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.AfterHrsAgentModel;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAvailabilityLookUp;

public interface AgentAvailabilityLookUpRepo extends MongoRepository<AgentAvailabilityLookUp, String> {

}
