package com.straviso.ns.dispatchcontrollercore.repository;

import java.util.List;


import org.springframework.data.mongodb.repository.MongoRepository;


import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;

public interface SupervisorRepository extends MongoRepository<SupervisorPolygonMapping, String> {
	
	boolean existsBySupervisorId(String supervisorId);

	SupervisorPolygonMapping findBySupervisorId(String toSupervisorId);

	List<SupervisorPolygonMapping> findByIsActiveOrderByFirstNameAscLastNameAsc(String flagY);

	<SupervisorPolygonMapping> findAll(SupervisorPolygonMapping supervisoListrPolygonMapping);

	

}
