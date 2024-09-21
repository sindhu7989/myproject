package com.straviso.ns.dispatchcontrollercore.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.straviso.ns.dispatchcontrollercore.entity.ConstraintConfig;

@Repository
public interface ConstraintConfigRepo extends MongoRepository<ConstraintConfig, String> {
	
	
}
