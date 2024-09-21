package com.straviso.ns.dispatchcontrollercore.repository;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;

public interface TechnicianAssignmentSolutionRepo extends MongoRepository <AgentAssignmentSolutionModel, String> {
	
	@Query("{" +
            "'agent.agentType': ?0, " +
            "'agent.isActive': ?1, " +
            "'agent.assignmentStatus': ?2, " +
            "'timestamp': { $gte: ?3, $lte: ?4 }" +
            "}")
    Page<AgentAssignmentSolutionModel> findByAgentTypeAndIsActiveAndAssignmentStatusAndTimestampBetween(
            String agentType, String isActive, String assignmentStatus, LocalDateTime startTime, LocalDateTime endTime,
            Pageable pageable
    );

	Page<AgentAssignmentSolutionModel> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate,
			Pageable pageable);
	
	@Query("{" +
            "'agent.technicianId': ?0, " +
            "'agent.ticketList.ticketNumber': ?1 " +
            "}")
	AgentAssignmentSolutionModel findByTechnicianIdAndTicketNumber(String TechnicianId , String TicketNumber);

	@Query(value="{'agent.technicianId': ?0, 'agent.ticketList.ticketNumber': ?1}",
	        sort="{'timestamp': -1}")
	Stream<AgentAssignmentSolutionModel> findByAgentTechnicianIdAndAgentTicketListTicketNumber(String fromTechnicianId,
			String ticketNumber);

	Page<AgentAssignmentSolutionModel> findByAgentAgentTypeAndTimestampBetween(String string, LocalDateTime startDate,
			LocalDateTime endDate, Pageable pageable);

	Page<AgentAssignmentSolutionModel> findByAgentAgentTypeAndAgentIsActiveAndTimestampBetween(String string, String string2,
			LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

	Stream<AgentAssignmentSolutionModel> findByAgentSupervisorIdAndAgentTicketListTicketNumber(String fromSupervisorId, String ticketNumber);

	@Query(value="{'agent.ticketList.conversationId': ?0, 'agent.ticketList.ticketNumber': ?1}",
	        sort="{'timestamp': -1}")
	Stream<AgentAssignmentSolutionModel> findByAgentTicketListConversationIdAndAgentTicketListTicketNumber(String conversationId,
			String ticketNumber);

	Page<AgentAssignmentSolutionModel> findByDcSolverProcessIdBetween(long dcSolverProcessIdStart,
			long dcSolverProcessIdEnd, Pageable processPage);

	Page<AgentAssignmentSolutionModel> findByAgentTechnicianIdAndDcSolverProcessIdBetween(String technicianId,
			long dcSolverProcessIdStart, long dcSolverProcessIdEnd, Pageable processPage);
	
	boolean existsByAgentTechnicianId(String technicianId);

	Stream<AgentAssignmentSolutionModel> findByAgentTechnicianIdAndTimestampBetween(String toTechnicianId, LocalDateTime startOfDay,
			LocalDateTime endOfDay, Sort sort);

}
