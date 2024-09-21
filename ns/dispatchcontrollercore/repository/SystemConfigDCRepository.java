package com.straviso.ns.dispatchcontrollercore.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.SystemConfigDC;


public interface SystemConfigDCRepository extends MongoRepository<SystemConfigDC, String> {

	List<SystemConfigDC> findByConfigRoleAndIsActive(String roleDcSolver, String flagY);

}
