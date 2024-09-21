package com.straviso.ns.dispatchcontrollercore.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.Agent;

public interface TechnicianDataRepo extends MongoRepository<Agent, String> {
	
	Page<Agent> findByIsActive(String string, Pageable paging);

	boolean existsByTechnicianId(String technicianId);

	Agent findByTechnicianId(String toTechnicianId);

	List<Agent> findByIsActiveAndAgentType(String flagY, String agentTypeActual);

	Agent findByTechnicianIdAndTicketListTicketNumber(String technicianId, String ticketNumber);

	Agent findByTechnicianIdAndAgentType(String string, String agentTypeVirtual);

	List<Agent> findByIsActiveAndAgentTypeAndSupervisorId(String flagY, String agentTypeActual, String supervisorId);

	Agent findByTechnicianIdAndSupervisorId(String technicianId, String supervisorId);

	List<Agent> findByIsActiveAndAgentTypeAndSupervisorIdOrderByFirstNameAscLastNameAsc(String flagY,
			String agentTypeActual, String supervisorId);

	List<Agent> findBySupervisorIdAndIsActiveAndAgentType(String supervisorId, String flagY, String agentTypeActual);
	
	boolean existsByTechnicianIdAndIsActive(String technicianId, String string);

	boolean existsByTechnicianIdAndIsActiveAndSupervisorId(String technicianId, String string, String supervisorId);
	

}
