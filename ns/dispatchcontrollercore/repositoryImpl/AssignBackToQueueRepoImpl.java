package com.straviso.ns.dispatchcontrollercore.repositoryImpl;


import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.bson.Document;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.result.UpdateResult;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.RouteSolverPath;
import com.straviso.ns.dispatchcontrollercore.dto.TicketNumbersDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.Location;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.entity.TicketActionTrail;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianAssignmentSolutionRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianDataRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TicketDataRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.serviceImpl.CockpitServiceImpl;
import com.straviso.ns.dispatchcontrollercore.utils.DataConvertorUtils;
import com.straviso.ns.dispatchcontrollercore.utils.DispatchControllerSupportUtils;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;

import com.fasterxml.jackson.databind.deser.DataFormatReaders.Match;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;



@Repository
public class AssignBackToQueueRepoImpl {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	TechnicianAssignmentSolutionRepo assignmentRepository;
	
	@Autowired
	TicketDataRepo ticketDataRepo;
	
	@Autowired
	TechnicianDataRepo technicianDataRepo;
	
	@Autowired
	DispatchControllerSupportUtils dispatchControllerSupportUtils;
	
	@Autowired
	TicketRepository ticketRepository;
	
	@Autowired
	DataConvertorUtils dataConvertorUtils;
	
	@Autowired(required = true)
	@Qualifier(value = "taskExecutor")
	private ThreadPoolTaskExecutor executor;
	

	private static final Logger log = LoggerFactory.getLogger(Ticket.class);

	public ResponseEntity<ApiResponseDto> updateByTicketNumber(TicketNumbersDto ticketNumber) {
		ApiResponseDto response = new ApiResponseDto();
		try {
			 LocalDateTime now = LocalDateTime.now(); 
			 
			 Ticket ticket1 = ticketDataRepo.findByTicketNumber(ticketNumber.getTicketNumber());
			 
			 String pre=ticket1.getTechnicianId()+":"+ticket1.getTechnicianFirstName()+" "+ticket1.getTechnicianLastName();
			 
			 	String actionBy = StringUtils.isEmpty(ticketNumber.getActionBy()) ?ticket1.getSupervisorId()+":"+ticket1.getSupervisorName() : ticketNumber.getActionBy() ;
				String pattern = "\\d+:.*";
				Pattern regex = Pattern.compile(pattern);
				Matcher matcher = regex.matcher(actionBy);
				
				if (!matcher.matches()) {
					 log.info("Pattern not match for {}",ticketNumber.getTicketNumber());
					 actionBy=ticket1.getSupervisorId()+":"+ticket1.getSupervisorName();
			       } 
		
				
			// Find the tickets to transfer based on ticketNumber and fromtechnicianId
			
				// Handle case when the ticket is not found for the given fromTechnicianId return Bad Request
				
				
				if (ticket1 == null) {

					log.info("Unable to find the data for Ticket {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_FAILED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED ) ) {

					log.info("Ticket unassigned {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY +" "+DispatchControllerConstants.STATUS_UNASSIGNED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_PAST_DUE ) ) {

					log.info("Ticket PastDue {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY +" "+DispatchControllerConstants.STATUS_PAST_DUE);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_CANCELLED)) {

					log.info("Ticket cancelled {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY +" "+DispatchControllerConstants.STATUS_CANCELLED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_MISSING_INFO) ) {

					log.info(" Ticket missing info {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_MISSING_INFO);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_COMPLETED) ) {

					log.info("Ticket completed {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY +" "+DispatchControllerConstants.STATUS_COMPLETED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				
				AgentAssignmentSolutionModel fromTechnicianAssignment = assignmentRepository
						.findByAgentTechnicianIdAndAgentTicketListTicketNumber(ticketNumber.getTechnicianId(), ticketNumber.getTicketNumber()).findFirst().orElse(null);
		
				
			if (fromTechnicianAssignment != null)
			{		
			//Update agent fields in AgentAssignmentSolutionModel
			// minus minutes from totalWorkHourGlobal 
			Ticket ticketToTransfer = null;
			List<Ticket> ticketList = fromTechnicianAssignment.getAgent().getTicketList();
			Location specificTicketLocation = null;
			Location precedingTicketLocation = null;
			
			for (Ticket ticket : ticketList) {
				if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber.getTicketNumber())) {
					ticketToTransfer = ticket;
					break;
				}
			}


			for (int i = 0; i < ticketList.size(); i++) {
				Ticket ticket = ticketList.get(i);
				log.info(" ticket {} ", ticket);
				if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber.getTicketNumber())) {
					specificTicketLocation = ticket.getLocation();
					log.info(" specificTicketLocation {} ", specificTicketLocation);
					// Check if there is a preceding ticket
					if (i > 0) {
						precedingTicketLocation = ticketList.get(i - 1).getLocation();
						log.info(" precedingTicketLocation {} ", precedingTicketLocation);
					} else {
						precedingTicketLocation = fromTechnicianAssignment.getAgent().getLocation();
						log.info(" At 1 precedingTicketLocation {} ", precedingTicketLocation);
					}
					break; 
				}

			}
			
			// Remove ticket from the TechnicianSolution and Agent
			if (ticketToTransfer != null) {
				ticketList.remove(ticketToTransfer);

			}
			
			// Update TotalWorkHourGlobal and AssignmentStatus of toTechnicianAssignment
			RouteSolverPath routeSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(precedingTicketLocation,specificTicketLocation);
			long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath) ? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME : routeSolverPath.getTime();
			double evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
			long travelTime = travelTimeLocal / 60000;
				
						long workHourTime = fromTechnicianAssignment.getAgent().getTotalWorkHourGlobal() - (travelTime + ticketToTransfer.getTicketETA());
						
						if(workHourTime<0) {
							workHourTime=0;
						}
						log.info(" fromTechnicianAssignment.getAgent().getTicketList() {} ", fromTechnicianAssignment.getAgent().getTicketList());
						
						double evaluatedDistance=fromTechnicianAssignment.getAgent().getEvaluatedDistance()-evaluatedDistanceLocal;
						if(evaluatedDistance<0)
						{
							evaluatedDistance=0;
						}
						
						//Set TotalWorkHourGlobal to 0 if ticketlist is empty for Techniciansolution and agent
						if(fromTechnicianAssignment.getAgent().getTicketList() == null)
						{
							workHourTime=0;
							evaluatedDistance=0;
						}
						
						fromTechnicianAssignment.getAgent().setTotalWorkHourGlobal(workHourTime);
						fromTechnicianAssignment.getAgent().setEvaluatedDistance(evaluatedDistance);
						log.info(" total work Time of fromTechnicianAssignment  {} ", workHourTime);
						String assignmentStatus = fromTechnicianAssignment.getAgent().getAvailableTime() > fromTechnicianAssignment
								.getAgent().getTotalWorkHourGlobal()
								? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED
										: fromTechnicianAssignment.getAgent().getAvailableTime() == fromTechnicianAssignment
										.getAgent().getTotalWorkHourGlobal()
										? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED
												: DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;

						fromTechnicianAssignment.getAgent().setAssignmentStatus(assignmentStatus);						
						
						
					
						assignmentRepository.save(fromTechnicianAssignment);
					
						technicianDataRepo.save(fromTechnicianAssignment.getAgent());
						
							}
						
						//Add action trail 
						TicketActionTrail ticketActionTrail = ticketActionTrailForCancelAndBTQ(DispatchControllerConstants.STATUS_BACKTOQUEUE, actionBy,  pre, DispatchControllerConstants.STATUS_UNASSIGNED);
						log.info(" updated the details of ticketActionTrail 1 {} ", ticketActionTrail);
						//Update Ticket collection
						updateTicketdetailsForCancelAndBTQ(ticket1, ticketActionTrail,DispatchControllerConstants.STATUS_UNASSIGNED,DispatchControllerConstants.STATUS_BACKTOQUEUE);

						//ticketActionTrailForCancelAndBTQ  updateTicketdetailsForCancelAndBTQ
			
			log.info("Updated Ticktes by ticketnumber: {}",ticketNumber.getTicketNumber());
			
			response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
			response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.info("Unable to updateTicket for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			
			response.setStatus(DispatchControllerConstants.STATUS_FAILED);
			response.setMessage(DispatchControllerConstants.STATUS_FAILED);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
	}
	public ResponseEntity<ApiResponseDto> cancelByTicketNumber(TicketNumbersDto ticketNumber) {
		ApiResponseDto response = new ApiResponseDto();
		try {
			 LocalDateTime now = LocalDateTime.now(); 
		
			 Ticket ticket1 = ticketDataRepo.findByTicketNumber(ticketNumber.getTicketNumber());
			 String pre=ticket1.getTechnicianId()+":"+ticket1.getTechnicianFirstName()+" "+ticket1.getTechnicianLastName();
			 
			 String actionBy = StringUtils.isEmpty(ticketNumber.getActionBy()) ?ticket1.getSupervisorId()+":"+ticket1.getSupervisorName() : ticketNumber.getActionBy() ;
				
				String pattern = "\\d+:.*";
				Pattern regex = Pattern.compile(pattern);
				Matcher matcher = regex.matcher(actionBy);
				
				if (!matcher.matches()) {
					 log.info("Pattern not match for {}",ticketNumber.getTicketNumber());
					 actionBy=ticket1.getSupervisorId()+":"+ticket1.getSupervisorName();
			       } 
		
			// Handle case when the ticket is not found for the given fromTechnicianId return Bad Request
				
				
			 if (ticket1 == null) {

					log.info("Unable to find the data for Ticket {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_FAILED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_CANCELLED)) {

					log.info(" Ticket cancelled {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY +" "+DispatchControllerConstants.STATUS_CANCELLED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_MISSING_INFO) ) {

					log.info("Ticket Missing info {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_MISSING_INFO);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_COMPLETED) ) {

					log.info("Ticket completed {} ", ticketNumber.getTicketNumber());

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY +" "+DispatchControllerConstants.STATUS_COMPLETED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				
			  log.info(" fromTechnicianAssignment");
              AgentAssignmentSolutionModel fromTechnicianAssignment = assignmentRepository.findByAgentTechnicianIdAndAgentTicketListTicketNumber(ticketNumber.getTechnicianId(), ticketNumber.getTicketNumber()).findFirst().orElse(null);
		      log.info(" fromTechnicianAssignment {} ", fromTechnicianAssignment);
		
			
		if (fromTechnicianAssignment != null) {	
			//Update agent fields in AgentAssignmentSolutionModel
			// minus minutes from totalWorkHourGlobal 
			Ticket ticketToTransfer = null;
			List<Ticket> ticketList = fromTechnicianAssignment.getAgent().getTicketList();
			Location specificTicketLocation = null;
			Location precedingTicketLocation = null;
			
			for (Ticket ticket : ticketList) {
				if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber.getTicketNumber())) {
					ticketToTransfer = ticket;
					break;
				}
			}


			for (int i = 0; i < ticketList.size(); i++) {
				Ticket ticket = ticketList.get(i);
				log.info(" ticket {} ", ticket);
				if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber.getTicketNumber())) {
					specificTicketLocation = ticket.getLocation();
					log.info(" specificTicketLocation {} ", specificTicketLocation);
					// Check if there is a preceding ticket
					if (i > 0) {
						precedingTicketLocation = ticketList.get(i - 1).getLocation();
						log.info(" precedingTicketLocation {} ", precedingTicketLocation);
					} else {
						precedingTicketLocation = fromTechnicianAssignment.getAgent().getLocation();
						log.info(" At 1 precedingTicketLocation {} ", precedingTicketLocation);
					}
					break; 
				}

			}
			
			// Remove ticket from the fromTechnician
						if (ticketToTransfer != null) {
							ticketList.remove(ticketToTransfer);

						}
			
			// Update TotalWorkHourGlobal and AssignmentStatus of toTechnicianAssignment
						RouteSolverPath routeSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(precedingTicketLocation,specificTicketLocation);
						long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath) ? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME : routeSolverPath.getTime();
						double evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
						
						long travelTime = travelTimeLocal / 60000;
						
						long workHourTime = fromTechnicianAssignment.getAgent().getTotalWorkHourGlobal() - (travelTime + ticketToTransfer.getTicketETA());
						
						if(workHourTime<0) {
							workHourTime=0;
						}
						
						double evaluatedDistance=fromTechnicianAssignment.getAgent().getEvaluatedDistance()-evaluatedDistanceLocal;
						if(evaluatedDistance<0)
						{
							evaluatedDistance=0;
						}
						
						//Set TotalWorkHourGlobal to 0 if ticketlist is empty for fromTechnician
						if(fromTechnicianAssignment.getAgent().getTicketList() == null)
						{
							workHourTime=0;
							evaluatedDistance=0;
						}
					
						fromTechnicianAssignment.getAgent().setTotalWorkHourGlobal(workHourTime);
						fromTechnicianAssignment.getAgent().setEvaluatedDistance(evaluatedDistance);
						log.info(" total work Time of fromTechnicianAssignment  {} ", workHourTime);
						String assignmentStatus = fromTechnicianAssignment.getAgent().getAvailableTime() > fromTechnicianAssignment
								.getAgent().getTotalWorkHourGlobal()
								? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED
										: fromTechnicianAssignment.getAgent().getAvailableTime() == fromTechnicianAssignment
										.getAgent().getTotalWorkHourGlobal()
										? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED
												: DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;

						fromTechnicianAssignment.getAgent().setAssignmentStatus(assignmentStatus);						
						
					
						assignmentRepository.save(fromTechnicianAssignment);
					
						technicianDataRepo.save(fromTechnicianAssignment.getAgent());
					
			 
		 }
		TicketActionTrail ticketActionTrail = null;
			//Add action trail 
			if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED)) {
				 ticketActionTrail = ticketActionTrailForCancelAndBTQ(DispatchControllerConstants.STATUS_CANCELLED, actionBy,pre, DispatchControllerConstants.STATUS_CANCELLED);
				log.info(" updated the details of ticketActionTrail 1 {} ", ticketActionTrail);
				//Update Ticket collection
				updateTicketdetailsForCancelAndBTQ(ticket1, ticketActionTrail,DispatchControllerConstants.STATUS_CANCELLED,DispatchControllerConstants.STATUS_CANCELLED);

			}
			else if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED)) 
			{
						
				 ticketActionTrail = ticketActionTrailForCancelAndBTQ(DispatchControllerConstants.STATUS_CANCELLED, actionBy,  pre, DispatchControllerConstants.STATUS_CANCELLED);
				log.info(" updated the details of ticketActionTrail 1 {} ", ticketActionTrail);
				//Update Ticket collection
				updateTicketdetailsForCancelIfUnaAssigned(ticket1, ticketActionTrail,DispatchControllerConstants.STATUS_CANCELLED,DispatchControllerConstants.STATUS_CANCELLED);

			}
			else if (ticket1.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_PAST_DUE)) 
			{
						
				ticketActionTrail = ticketActionTrailForCancelAndBTQ(DispatchControllerConstants.STATUS_CANCELLED, actionBy,pre, DispatchControllerConstants.STATUS_CANCELLED);
				log.info(" updated the details of ticketActionTrail 1 {} ", ticketActionTrail);
				//Update Ticket collection
				updateTicketdetailsForCancelAndBTQ(ticket1, ticketActionTrail,DispatchControllerConstants.STATUS_CANCELLED,DispatchControllerConstants.STATUS_CANCELLED);

			}
			
			log.info("Updated Ticktes by ticketnumber: {}",ticketNumber.getTicketNumber());
			
			response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
			response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			log.info("{} Unable to cancelTicket for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			
			response.setStatus(DispatchControllerConstants.STATUS_FAILED);
			response.setMessage(DispatchControllerConstants.STATUS_FAILED);
			log.info("Unable to cancelTicket");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
	}
	
	public Ticket updateTicketdetailsForCancelAndBTQ(Ticket ticketDetails, TicketActionTrail ticketActionTrail,String globalStatus,
			String actionStatus) {

		// Updating Ticket DC Solver
		ticketDetails.getTicketActionTrails().add(ticketActionTrail);
		ticketDetails.setGlobalStatus(globalStatus);
		ticketDetails.setActionOnTicket(actionStatus);
		ticketDetails.setTechnicianId("");
		ticketDetails.setTechnicianFirstName("");
		ticketDetails.setTechnicianLastName("");
		ticketDetails.setTechnicianEmailId("");
				
		ticketDataRepo.save(ticketDetails);
		
	if(!StringUtils.equalsIgnoreCase(ticketDetails.getIsAssistTicket(), DispatchControllerConstants.YES) ) {
		// Updating master ticket collection
		LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();

		lumenCollectionUpdateDTO.setGlobalStatus(globalStatus);
		lumenCollectionUpdateDTO.setActionOnTicket(actionStatus);
		lumenCollectionUpdateDTO.setTechnicianId("");
		lumenCollectionUpdateDTO.setTechnicianFirstName("");
		lumenCollectionUpdateDTO.setTechnicianLastName("");
		lumenCollectionUpdateDTO.setTechnicianEmailId("");
		
		lumenCollectionUpdateDTO.setAssignmentDateTime(LocalDateTime.now());
		lumenCollectionUpdateDTO.setSupervisorName(ticketDetails.getSupervisorName());
		lumenCollectionUpdateDTO.setSupervisorId(ticketDetails.getSupervisorId());
		lumenCollectionUpdateDTO.setAssignmentDateTime(ticketDetails.getAssignmentDateTime());
		lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
		lumenCollectionUpdateDTO.setTicketNumber(ticketDetails.getTicketNumber());
		lumenCollectionUpdateDTO.setConversationId(ticketDetails.getConversationId());
		

		if(StringUtils.equalsIgnoreCase(DispatchControllerConstants.STATUS_CANCELLED, actionStatus)) {
			
			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_CANCELLED + ticketDetails.getSupervisorId() + " : " + ticketDetails.getSupervisorName());
		
		}else if(StringUtils.equalsIgnoreCase(DispatchControllerConstants.STATUS_BACKTOQUEUE, actionStatus)) {
			
			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_BACKTOQUEUE + ticketDetails.getSupervisorId() + " : " + ticketDetails.getSupervisorName());
			
		}else {
			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_TRANSFERRED + ticketDetails.getSupervisorId() + " : " + ticketDetails.getSupervisorName());
			
		}

		ticketRepository.updateLumenTicketCollection(lumenCollectionUpdateDTO);
		try {
			String businessId = BusinessContext.getTenantId();
	        String businessToken = BusinessTokenContext.getBusinessToken();
		executor.execute(()->{
			BusinessContext.setTenantId(businessId);
			BusinessTokenContext.setBusinessToken(businessToken);
			dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO,businessId,businessToken);
		});
		}
		catch(Exception e)
		{
			log.info("Unable to save Audit due to {} ", e.getMessage());
		}
		

	}



		log.info(" updateTicketdetails 1 {} ", ticketDetails);
		return ticketDetails;
	}
	public Ticket updateTicketdetailsForCancelIfUnaAssigned(Ticket ticketDetails, TicketActionTrail ticketActionTrail,String globalStatus,
			String actionStatus) {

		ticketDetails.getTicketActionTrails().add(ticketActionTrail);
		ticketDetails.setGlobalStatus(globalStatus);
		ticketDetails.setActionOnTicket(actionStatus);
		ticketDataRepo.save(ticketDetails);

		if(!StringUtils.equalsIgnoreCase(ticketDetails.getIsAssistTicket(), DispatchControllerConstants.YES) ) {
		// Updating master ticket collection
		LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();

		lumenCollectionUpdateDTO.setGlobalStatus(globalStatus);
		lumenCollectionUpdateDTO.setActionOnTicket(actionStatus);
		lumenCollectionUpdateDTO.setTechnicianId("");
		lumenCollectionUpdateDTO.setTechnicianFirstName("");
		lumenCollectionUpdateDTO.setTechnicianLastName("");
		lumenCollectionUpdateDTO.setTechnicianEmailId("");
		
		lumenCollectionUpdateDTO.setAssignmentDateTime(LocalDateTime.now());
		lumenCollectionUpdateDTO.setSupervisorName(ticketDetails.getSupervisorName());
		lumenCollectionUpdateDTO.setSupervisorId(ticketDetails.getSupervisorId());
		lumenCollectionUpdateDTO.setAssignmentDateTime(ticketDetails.getAssignmentDateTime());
		lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
		lumenCollectionUpdateDTO.setTicketNumber(ticketDetails.getTicketNumber());
		lumenCollectionUpdateDTO.setConversationId(ticketDetails.getConversationId());

		if(StringUtils.equalsIgnoreCase(DispatchControllerConstants.STATUS_CANCELLED, actionStatus)) {

			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_CANCELLED + ticketDetails.getSupervisorId() + " : " + ticketDetails.getSupervisorName());

		}else if(StringUtils.equalsIgnoreCase(DispatchControllerConstants.STATUS_BACKTOQUEUE, actionStatus)) {

			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_BACKTOQUEUE + ticketDetails.getSupervisorId() + " : " + ticketDetails.getSupervisorName());

		}else {
			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_TRANSFERRED + ticketDetails.getSupervisorId() + " : " + ticketDetails.getSupervisorName());

		}
		
		ticketRepository.updateLumenTicketCollection(lumenCollectionUpdateDTO);
		try {
			String businessId = BusinessContext.getTenantId();
	        String businessToken = BusinessTokenContext.getBusinessToken();
			executor.execute(()->{
				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(businessToken);
				dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO,businessId,businessToken);
			});
			}
			catch(Exception e)
			{
				log.info("Unable to save Audit due to {} ", e.getMessage());
			}
		}
		log.info(" updateTicketdetails 1 {} ", ticketDetails);
		return ticketDetails;
	}
	
	public TicketActionTrail ticketActionTrailForCancelAndBTQ(String action, String actionBy, String preAction,
			String postAction) {
		TicketActionTrail ticketActionTrail = new TicketActionTrail();

		log.info(" update TicketActionTrail details 1 {} ", ticketActionTrail);
		ticketActionTrail.setAction(action);
		ticketActionTrail.setActionBy(actionBy);
		ticketActionTrail.setActionOn(LocalDateTime.now());
		ticketActionTrail.setPreAction(preAction);
		ticketActionTrail.setPostAction(postAction);
		log.info(" ticketActionTrail 1 {} ", ticketActionTrail);
		return ticketActionTrail;
	}

//	public ConstraintConfig getByConstraintName(String constraintName) {
//		try {
//			Query query = Query.query(Criteria.where("constraintName").is(constraintName));
//			ConstraintConfig constraintConfig = mongoTemplate.findOne(query, ConstraintConfig.class);
//			return constraintConfig;
//		} catch (Exception e) {
//			log.info("{} Unable to get the updated result for businessId : {} , due to {}",
//					BusinessContext.getTenantId(), e.getMessage());
//			return null;
//		}
//	}

}
