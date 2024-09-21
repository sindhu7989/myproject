package com.straviso.ns.dispatchcontrollercore.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.catalina.connector.Request;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.CalenderViewDto;
import com.straviso.ns.dispatchcontrollercore.dto.DataDTO;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.RouteSolverPath;
import com.straviso.ns.dispatchcontrollercore.dto.TicketDCSolverGlobalSearchDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TechnicianDetailsDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketCountDTO;

import com.straviso.ns.dispatchcontrollercore.dto.TicketNumbersDto;
import com.straviso.ns.dispatchcontrollercore.dto.TransferTicketDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchFieldRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.BuzzSendNotificationRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.CockpitBubbleCountStatRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.FetchCountById;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechnicianStatusCount;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketDetailsByMasterExternalIdRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketDetailsBySupervisorIdRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.supervisorCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.AgentListResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.CountResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.GroupByActionResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponsePageDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnianWorkLoadDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianAssisgnmentStatusCount;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianIdDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketActionCountResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketCountResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.UserHierarchySupervisorDTO;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.DCSolverProcessAuditModel;
import com.straviso.ns.dispatchcontrollercore.entity.DCSolverTaskAuditModel;
import com.straviso.ns.dispatchcontrollercore.entity.Location;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.entity.TicketActionTrail;
import com.straviso.ns.dispatchcontrollercore.entity.UserHierarchyModel;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.repository.AgentRepository;
import com.straviso.ns.dispatchcontrollercore.repository.DCSolverProcessAuditRepo;
import com.straviso.ns.dispatchcontrollercore.repository.DCSolverTaskAuditRepo;
import com.straviso.ns.dispatchcontrollercore.repository.SupervisorRepository;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianAssignmentSolutionRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianDataRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TicketDataRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.repository.UserHierarchyRepo;
import com.straviso.ns.dispatchcontrollercore.repositoryImpl.AgentRepositoryImpl;
import com.straviso.ns.dispatchcontrollercore.repositoryImpl.AssignBackToQueueRepoImpl;
import com.straviso.ns.dispatchcontrollercore.service.CockpitService;
import com.straviso.ns.dispatchcontrollercore.utils.DataConvertorUtils;
import com.straviso.ns.dispatchcontrollercore.utils.DispatchControllerSupportUtils;
import java.util.regex.*;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CockpitServiceImpl implements CockpitService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	TechnicianDataRepo technicianDataRepo;

	@Autowired
	TicketDataRepo ticketdetails;

	@Autowired
	TechnicianAssignmentSolutionRepo assignmentRepository;

	@Autowired
	TicketRepository ticketRepository;

	@Autowired
	AssignBackToQueueRepoImpl assignBackToQueueRepoImpl;

	@Autowired
	UserHierarchyRepo userHierarchyRepo;

	// @Autowired(required = true)
	// @Qualifier(value = "taskExecutor")
	// private ThreadPoolTaskExecutor executor;

	@Autowired
	DispatchControllerSupportUtils dispatchControllerSupportUtils;

	@Autowired
	DCSolverProcessAuditRepo dcSolverProcessAuditRepo;

	@Autowired
	DCSolverTaskAuditRepo dcSolverTaskAuditRepo;

	@Autowired
	SupervisorRepository supervisorRepo;

	@Autowired
	DataConvertorUtils dataConvertorUtils;

	@Autowired(required = true)
	@Qualifier(value = "taskExecutor")
	private ThreadPoolTaskExecutor executor;
	
	@Autowired
	AgentRepository agentRepository;
	
	 

	@Override
	public ResponseEntity<ApiResponseDto> getTicketStatusBubbleCount(TicketCountDTO ticketDetails) {
		TicketActionCountResponse countResponse = new TicketActionCountResponse();
		ApiResponseDto apiResponseDto = new ApiResponseDto();

		log.info("{} Request body of getTicketStatusBubbleCount {}", ticketDetails);

		try {
			if (ticketDetails != null) {

				String fieldName = "actionOnTicket";
				long transferedCount = 0;
				long cancelledCount = 0;
				long backToQueueCount = 0;
				long assignedCount = 0;
				long unAssignedCount = 0;
				if (StringUtils.isNotEmpty(ticketDetails.getStatus())) {
					if ("Transfered".equalsIgnoreCase(ticketDetails.getStatus())) {
						transferedCount = countByStatus("Transfered", ticketDetails, fieldName);
						log.info("transferedCount : {} ", transferedCount);
					} else if ("Cancelled".equalsIgnoreCase(ticketDetails.getStatus())) {
						cancelledCount = countByStatus("Cancelled", ticketDetails, fieldName);
						log.info("cancelledCount : {} ", cancelledCount);
					} else if ("BackToQueue".equalsIgnoreCase(ticketDetails.getStatus())) {
						backToQueueCount = countByStatus("BackToQueue", ticketDetails, fieldName);
						log.info("backToQueueCount : {} ", backToQueueCount);
					} else if ("UnAssigned".equalsIgnoreCase(ticketDetails.getStatus())) {
						unAssignedCount = countByStatus("UnAssigned", ticketDetails, fieldName);
						log.info("unAssignedCount : {} ", unAssignedCount);
					} else if ("NoAction".equalsIgnoreCase(ticketDetails.getStatus())) {
						assignedCount = countByStatus("Assigned", ticketDetails, fieldName);
						log.info("Assigned : {} ", assignedCount);
					}
				} else {
					transferedCount = countByStatus("Transfered", ticketDetails, fieldName);
					cancelledCount = countByStatus("Cancelled", ticketDetails, fieldName);
					backToQueueCount = countByStatus("BackToQueue", ticketDetails, fieldName);
					assignedCount = countByStatus("Assigned", ticketDetails, fieldName);
					unAssignedCount = countByStatus("UnAssigned", ticketDetails, fieldName);
					log.info("backToQueue : {} ", backToQueueCount);

				}

				countResponse.setTransfered(transferedCount);
				countResponse.setCancelled(cancelledCount);
				countResponse.setBackToQueue(backToQueueCount);
				countResponse.setAssigned(assignedCount);
				countResponse.setUnAssigned(unAssignedCount);
				log.info("countResponse : {} ", countResponse);

				long totalCount = transferedCount + cancelledCount + backToQueueCount + assignedCount + unAssignedCount;
				countResponse.setTotalCount(totalCount);

				apiResponseDto.setStatus("true");
				apiResponseDto.setMessage("Success");
				apiResponseDto.setResponseData(countResponse);

				log.info("apiResponseDto : {} ", apiResponseDto);

			} else {
				log.info("Unable to execute Query");

				apiResponseDto.setStatus("false");
				apiResponseDto.setMessage("Unable to execute Query");

			}

		} catch (Exception e) {
			log.info("Unable to execute query due to {} ",e.getMessage());
			apiResponseDto.setStatus("false");
			apiResponseDto.setMessage("Unable to execute Query");
		}

		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);

	}

	private long countByStatus(String globalStatus, TicketCountDTO ticketCountDTO, String fieldName) {

		Query query = new Query();

		if (StringUtils.isNotEmpty(ticketCountDTO.getStartDate())
				&& StringUtils.isNotEmpty(ticketCountDTO.getEndDate())) {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime startDateTime = LocalDateTime.parse(ticketCountDTO.getStartDate(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(ticketCountDTO.getEndDate(), formatter);

			// old :
			// query.addCriteria(Criteria.where("createdDateTime").gte(startDateTime).lte(endDateTime));
			// new filter ticketduedateand time
//			query.addCriteria(Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime));
			query.addCriteria(
					Criteria.where(DispatchControllerConstants.CREATEDDATETIME).gte(startDateTime).lte(endDateTime));
		}

		query.addCriteria(Criteria.where(fieldName).is(globalStatus));

		return mongoTemplate.count(query, DispatchControllerConstants.TICKET_COLLECTION);
	}

	@Override
	public ResponseEntity<CountResponseDTO> getAllTechnicianDetials(TechnicianDetailsDTO technicianDetailsDTO) {
		String businessId = BusinessContext.getTenantId();
		CountResponseDTO responseDTO = new CountResponseDTO();
		int pageNo = 0;
		int pageSize=0;
		try {
			
			if (technicianDetailsDTO.getPageNo() > 0) {
				pageNo = technicianDetailsDTO.getPageNo() - 1;
			}
			log.info("pageNo : {} ", pageNo);
			
			Pageable pageable = PageRequest.of(pageNo, technicianDetailsDTO.getSize());
			
			AgentListResponse pageOfAgent = null;

				 pageOfAgent = agentRepository.findAllData(pageable,technicianDetailsDTO.getIsActive());
				log.info("pageOfAgent : {} ", pageOfAgent);
			
			
			
			if(pageOfAgent.getCount() %technicianDetailsDTO.getSize() !=0) {
				pageSize = (pageOfAgent.getCount() /technicianDetailsDTO.getSize()) + 1;
			}else {
				pageSize = (pageOfAgent.getCount() /technicianDetailsDTO.getSize());
			}
			
		
			
			responseDTO.setTotalPages(pageSize);
			responseDTO.setTotalElements(Long.valueOf(pageOfAgent.getCount()));
			responseDTO.setResponseData(pageOfAgent.getResponseList());
			responseDTO.setResponseText(HttpStatus.OK.getReasonPhrase());

			log.info("getAllTechnicianDetails request completed with statusCode: {}", HttpStatus.OK);
			return new ResponseEntity<>(responseDTO, HttpStatus.OK);
		} catch (Exception e) {
			log.info(businessId + " Unable to find the data while getting getAllTechnicianDetails due to : {}", e.getMessage());
			return new ResponseEntity<>(new CountResponseDTO("Unable to find the data", null, null, null),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<CountResponseDTO> getTicketDetialsByglobalStatus(TechnicianDetailsDTO technicianDetailsDTO) {
		String businessId = BusinessContext.getTenantId();
		CountResponseDTO responseDTO = new CountResponseDTO();
		String globalStatus = technicianDetailsDTO.getGlobalStatus();
		int pageSize = technicianDetailsDTO.getSize();
		int pageNo = 0;

		log.info(" pageNo : {} ", pageNo);

		try {
			Page<Ticket> ticketPage = null;
			if (technicianDetailsDTO.getPageNo() > 0) {
				pageNo = technicianDetailsDTO.getPageNo() - 1;
			}

			log.info("pageNo : {} ", pageNo);

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDate;
			LocalDateTime endDate;

			Pageable pageable;
			pageable = PageRequest.of(pageNo, pageSize).withSort(Direction.DESC,
					DispatchControllerConstants.TICKETDUEDATEANDTIME);
			LocalDateTime ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
			if (StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && StringUtils.isEmpty(globalStatus)) {
		
				ticketPage = ticketdetails
						.findByGlobalStatusInOrGlobalStatusAndCompletionDateTimeGreaterThan(
								new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
										DispatchControllerConstants.STATUS_UNASSIGNED,
										DispatchControllerConstants.STATUS_MISSING_INFO,
										DispatchControllerConstants.STATUS_RESCHEDULE,
										DispatchControllerConstants.STATUS_CANCELLED},
								DispatchControllerConstants.STATUS_COMPLETE, ldt_start, pageable);

			} else if (StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && !StringUtils.isEmpty(globalStatus)) {
				if (globalStatus.equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketPage = ticketdetails.findByGlobalStatusInAndIsAssistTicket(
							new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
									DispatchControllerConstants.STATUS_RESCHEDULE },
							DispatchControllerConstants.NO, pageable);
				} else if (StringUtils.equalsIgnoreCase(globalStatus, DispatchControllerConstants.STATUS_COMPLETE)) {
					ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

					ticketPage = ticketdetails.findByGlobalStatusAndIsAssistTicketAndCompletionDateTimeGreaterThan(
							globalStatus, DispatchControllerConstants.NO, ldt_start, pageable);
				} else {
					ticketPage = ticketdetails.findByGlobalStatusAndIsAssistTicket(globalStatus,
							DispatchControllerConstants.NO, pageable);
				}
			} else if (!StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& !StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && StringUtils.isEmpty(globalStatus)) {
				startDate = LocalDateTime.parse(technicianDetailsDTO.getStartDate(), formatter);
				endDate = LocalDateTime.parse(technicianDetailsDTO.getEndDate(), formatter);

				
				ticketPage = ticketdetails
						.findByGlobalStatusInOrGlobalStatusAndCompletionDateTimeGreaterThanAndTicketDueDateBetween(
								new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
										DispatchControllerConstants.STATUS_UNASSIGNED,
										DispatchControllerConstants.STATUS_MISSING_INFO,
										DispatchControllerConstants.STATUS_RESCHEDULE,
										DispatchControllerConstants.STATUS_CANCELLED},
								DispatchControllerConstants.STATUS_COMPLETE, ldt_start, startDate, endDate, pageable);

			} else if (!StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& !StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && !StringUtils.isEmpty(globalStatus)) {
				startDate = LocalDateTime.parse(technicianDetailsDTO.getStartDate(), formatter);
				endDate = LocalDateTime.parse(technicianDetailsDTO.getEndDate(), formatter);

				if (globalStatus.equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketPage = ticketdetails
							.findByGlobalStatusInAndDueDateBetween(
									new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
											DispatchControllerConstants.STATUS_RESCHEDULE },
									startDate, endDate, pageable);
				} else if (StringUtils.equalsIgnoreCase(globalStatus, DispatchControllerConstants.STATUS_COMPLETE)) {
					ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

					ticketPage = ticketdetails.findByGlobalStatusAndIsAssistTicketAndCompletionDateTime(globalStatus,
							DispatchControllerConstants.NO, ldt_start, startDate, endDate, pageable);
				} else {
					ticketPage = ticketdetails.findByGlobalStatusAndDueDateBetween(globalStatus, startDate, endDate,
							pageable);
				}

			}

			log.info("tickets : {} ", ticketPage);

			log.info("ticketPageByGlobalStatusByDateRange : {} ", ticketPage);
			responseDTO.setResponseText("OK");
			responseDTO.setResponseData(ticketPage.getContent());
			responseDTO.setTotalPages(ticketPage.getTotalPages());
			responseDTO.setTotalElements(ticketPage.getTotalElements());
			log.info("ticketPageByGlobalStatusByDateRange request completed with statusCode: {}", HttpStatus.OK);
			return new ResponseEntity<>(responseDTO, HttpStatus.OK);
		} catch (Exception e) {
			// Handle exceptions
			log.info(businessId + " Unable to find the data while fetching ticketPageByGlobalStatusByDateRange due to : {}",
					e.getMessage());
			return new ResponseEntity<>(new CountResponseDTO("Unable to find the data", null, null, null),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<ApiResponseDto> getTechnicianAssismentStatusCounts(TicketCountDTO ticketDetails) {
		TechnicianAssisgnmentStatusCount countResponse = new TechnicianAssisgnmentStatusCount();
		ApiResponseDto apiResponseDto = new ApiResponseDto();

		log.info("{} Request body of getTechnicianAssismentStatusCounts {}", ticketDetails);

		try {
			if (ticketDetails != null) {

				long underAssignedCount = 0;
				long overAssignedCount = 0;
				long idealAssignmentCount = 0;
				if (StringUtils.isNotEmpty(ticketDetails.getStatus())) {
					if ("UnderAssigned".equalsIgnoreCase(ticketDetails.getStatus())) {
						underAssignedCount = techniciancountByStatus("UnderAssigned", ticketDetails);
						log.info("underAssignedCount : {} ", underAssignedCount);
					} else if ("OverAssigned".equalsIgnoreCase(ticketDetails.getStatus())) {
						overAssignedCount = techniciancountByStatus("OverAssigned", ticketDetails);
						log.info("OverAssignedCount : {} ", overAssignedCount);
					} else if ("IdealAssignment".equalsIgnoreCase(ticketDetails.getStatus())) {
						idealAssignmentCount = techniciancountByStatus("IdealAssignment", ticketDetails);

						log.info("idealAssignmentCount : {} ", idealAssignmentCount);
					}
				} else {
					underAssignedCount = techniciancountByStatus("UnderAssigned", ticketDetails);
					overAssignedCount = techniciancountByStatus("OverAssigned", ticketDetails);
					idealAssignmentCount = techniciancountByStatus("IdealAssignment", ticketDetails);

					log.info("idealAssignmentCount : {} ", idealAssignmentCount);

				}

				countResponse.setUnderAssigned(underAssignedCount);
				countResponse.setOverAssigned(overAssignedCount);
				countResponse.setIdealAssignment(idealAssignmentCount);

				log.info("countResponse : {} ", countResponse);

				long totalCount = underAssignedCount + overAssignedCount + idealAssignmentCount;
				countResponse.setTotalCount(totalCount);

				apiResponseDto.setStatus("true");
				apiResponseDto.setMessage("Success");
				apiResponseDto.setResponseData(countResponse);

				log.info("apiResponseDto : {} ", apiResponseDto);

			} else {
				log.info("Unable to execute Query");

				apiResponseDto.setStatus("false");
				apiResponseDto.setMessage("Unable to execute Query");

			}

		} catch (Exception e) {
			log.info("Unable to execute query due to {} ",e.getMessage());
			apiResponseDto.setStatus("false");
			apiResponseDto.setMessage("Unable to execute Query");
		}

		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

	private long techniciancountByStatus(String assignmentStatus, TicketCountDTO ticketCountDTO) {

		Query query = new Query();

		if (StringUtils.isNotEmpty(ticketCountDTO.getStartDate())
				&& StringUtils.isNotEmpty(ticketCountDTO.getEndDate())) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime startDateTime = LocalDateTime.parse(ticketCountDTO.getStartDate(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(ticketCountDTO.getEndDate(), formatter);

			query.addCriteria(Criteria.where("timestamp").gte(startDateTime).lte(endDateTime));
		}
		query.addCriteria(Criteria.where("agent.agentType").is(DispatchControllerConstants.AGENT_TYPE_ACTUAL));
		query.addCriteria(Criteria.where("agent.isActive").is(DispatchControllerConstants.FLAG_Y));
		query.addCriteria(Criteria.where("agent.assignmentStatus").is(assignmentStatus));

		log.info("DTO : {} ", mongoTemplate.count(query, "technicianAssignmentSolution"));

		return mongoTemplate.count(query, "technicianAssignmentSolution");
	}

	@Override
	public ResponseEntity<CountResponseDTO> getTechnicianAssismentStatusDetails(TicketCountDTO ticketDetails) {

		String businessId = BusinessContext.getTenantId();
		CountResponseDTO responseDTO = new CountResponseDTO();
		String status = ticketDetails.getStatus();
		int pageSize = ticketDetails.getSize();
		int pageNo = 0;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		log.info(" pageNo : {} ", pageNo);
		try {
			Page<AgentAssignmentSolutionModel> solutionPage;
			if (ticketDetails.getPageNo() > 0) {
				pageNo = ticketDetails.getPageNo() - 1;
			}

			LocalDateTime startDate = LocalDateTime.parse(ticketDetails.getStartDate(), formatter);
			LocalDateTime endDate = LocalDateTime.parse(ticketDetails.getEndDate(), formatter);
			Pageable pageable = PageRequest.of(pageNo, pageSize).withSort(Direction.DESC, "timestamp");
			if (StringUtils.equalsIgnoreCase(status, "All") || status == null || StringUtils.isEmpty(status)) {
				solutionPage = assignmentRepository.findByAgentAgentTypeAndAgentIsActiveAndTimestampBetween("Actual",
						"Y", startDate, endDate, pageable);
				log.info("ticketPage : {} ", solutionPage);
			} else {
				solutionPage = assignmentRepository.findByAgentTypeAndIsActiveAndAssignmentStatusAndTimestampBetween(
						"Actual", "Y", status, startDate, endDate, pageable);
			}
			log.info("AgentAssignmentSolutionModel : {} ", solutionPage);
			responseDTO.setResponseText("OK");
			responseDTO.setResponseData(solutionPage.getContent());
			responseDTO.setTotalPages(solutionPage.getTotalPages());
			responseDTO.setTotalElements(solutionPage.getTotalElements());
			log.info("ticketPageByGlobalStatusByDateRange request completed with statusCode: {}", HttpStatus.OK);
			return new ResponseEntity<>(responseDTO, HttpStatus.OK);
		} catch (Exception e) {
			// Handle exceptions
			log.info(businessId + " Unable to find the data while getting ticketPageByGlobalStatusByDateRange due to :{}",
					e.getMessage());
			return new ResponseEntity<>(new CountResponseDTO("Unable to find the data", null, null, null),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<ResponsePageDto> getCalenderViewDetailsService(CalenderViewDto calenderViewDto) {

		ResponsePageDto apiResponseDto = new ResponsePageDto();
		LocalDateTime startDateTime = null;
		LocalDateTime endDateTime = null;
		try {
			if (StringUtils.isNotEmpty(calenderViewDto.getStartDate())
					&& StringUtils.isNotEmpty(calenderViewDto.getEndDate())) {

				DateTimeFormatter formatter = DateTimeFormatter
						.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
				try {
					startDateTime = LocalDateTime.parse(calenderViewDto.getStartDate(), formatter);
					endDateTime = LocalDateTime.parse(calenderViewDto.getEndDate(), formatter);
				} catch (DateTimeParseException e) {
					log.info("Invalid Date Time format for getTechnicianAssismentSolutionByTechnicianId: {}",
							e.getMessage());
					apiResponseDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
					apiResponseDto.setStatusMessage("Invalid Date Time format");
					apiResponseDto.setResponseData(new ArrayList<>());
					return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
				}
			} else {
				apiResponseDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
				apiResponseDto.setStatusMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
				apiResponseDto.setResponseData(new ArrayList<>());
				return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
			}

			try {
				LocalDateTime ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
				int pageNo = calenderViewDto.getPageNo() <= 0 ? 0 : calenderViewDto.getPageNo() - 1;
				int pageSize = calenderViewDto.getPageSize() <= 0 ? 20 : calenderViewDto.getPageSize();

				Pageable pageable = PageRequest.of(pageNo, pageSize,
						Sort.by(DispatchControllerConstants.TICKETDUEDATEANDTIME).descending());
				Page<Ticket> dbResponse = ticketdetails.findByTechnicianIdAndTimestampBetween(
						calenderViewDto.getTechnicianId(), startDateTime, endDateTime,new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
								DispatchControllerConstants.STATUS_UNASSIGNED,
								DispatchControllerConstants.STATUS_MISSING_INFO,
								DispatchControllerConstants.STATUS_RESCHEDULE,
								DispatchControllerConstants.STATUS_CANCELLED},DispatchControllerConstants.STATUS_COMPLETE, ldt_start, pageable);
				if (dbResponse != null && !dbResponse.isEmpty()) {

					apiResponseDto.setStatusCode(HttpStatus.OK.value());
					apiResponseDto.setStatusMessage(DispatchControllerConstants.STATUS_SUCCESS);
					apiResponseDto.setResponseData(dbResponse.getContent());
					apiResponseDto.setTotalPages(dbResponse.getTotalPages());
					apiResponseDto.setTotalElements(dbResponse.getTotalElements());
				} else {
					apiResponseDto.setStatusCode(HttpStatus.OK.value());
					apiResponseDto.setStatusMessage(DispatchControllerConstants.NO_DATA_AVAILABLE);
					apiResponseDto.setResponseData(new ArrayList<>());
				}
			} catch (Exception e) {
				log.info("Unable to process the request for getTechnicianAssismentSolutionByTechnicianId: {}"
						+ e.getMessage());
				apiResponseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				apiResponseDto.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
				apiResponseDto.setResponseData(new ArrayList<>());
			}
		} catch (Exception e) {
			log.info("Unable to process the request for getTechnicianAssismentSolutionByTechnicianId: {}"
					+ e.getMessage());
			apiResponseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			apiResponseDto.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(new ArrayList<>());
		}
		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<TechnicianResponseDto> getTechnicianWorkloadByTechnicianId(CalenderViewDto technicianIds) {

		TechnicianResponseDto apiResponseDto = new TechnicianResponseDto();
		LocalDateTime startDateTime;
		LocalDateTime endDateTime;

		try {
			if (technicianIds == null || technicianIds.getTechnicianId() == null || technicianIds.getStartDate() == null
					|| technicianIds.getEndDate() == null) {
				return new ResponseEntity<>(new TechnicianResponseDto(HttpStatus.BAD_REQUEST.value(),
						DispatchControllerConstants.RESPONSE_BAD_REQUEST, new ArrayList<>()), HttpStatus.OK);
			} else {
				DateTimeFormatter formatter = DateTimeFormatter
						.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);

				try {
					startDateTime = LocalDateTime.parse(technicianIds.getStartDate(), formatter);
					endDateTime = LocalDateTime.parse(technicianIds.getEndDate(), formatter);
				} catch (DateTimeParseException e) {
					log.info("Invalid Date Time format for getTechnicianWorkloadByTechnicianId: {}", e.getMessage());
					return new ResponseEntity<>(new TechnicianResponseDto(HttpStatus.BAD_REQUEST.value(),
							"Invalid Date Time Format", new ArrayList<>()), HttpStatus.OK);
				}

				List<TechnianWorkLoadDto> response = new ArrayList<>();

				TypedAggregation<AgentAssignmentSolutionModel> aggregation = Aggregation.newAggregation(
						AgentAssignmentSolutionModel.class,
						Aggregation.match(Criteria.where(DispatchControllerConstants.FIELD_AGENTTECHNICIANID)
								.is(technicianIds.getTechnicianId()).and(DispatchControllerConstants.TIMESTAMP)
								.gte(startDateTime).lte(endDateTime)));

				List<AgentAssignmentSolutionModel> dbResponse = mongoTemplate
						.aggregate(aggregation, AgentAssignmentSolutionModel.class).getMappedResults();

				if (dbResponse != null && !dbResponse.isEmpty()) {
					dbResponse.forEach(x -> {
						Agent agent = x.getAgent();
						if (agent != null) {
							response.add(new TechnianWorkLoadDto(x.getTimestamp(), agent.getAvailableTime(),
									agent.getTotalWorkHourGlobal(), agent.getTechnicianId(),
									agent.getFirstName() + " " + agent.getLastName()));
						}
					});
					apiResponseDto.setStatusCode(HttpStatus.OK.value());
					apiResponseDto.setStatusMessage(DispatchControllerConstants.STATUS_SUCCESS);
					apiResponseDto.setResponseData(response);
					return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
				} else {
					apiResponseDto.setStatusCode(HttpStatus.OK.value());
					apiResponseDto.setStatusMessage(DispatchControllerConstants.NO_DATA_AVAILABLE);
					apiResponseDto.setResponseData(new ArrayList<>());
					return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
				}
			}
		} catch (Exception e) {
			log.info("Unable to process the request for getTechnicianWorkloadByTechnicianId: {}", e.getMessage());
			apiResponseDto.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			apiResponseDto.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(new ArrayList<>());
		}
		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ApiResponseDto> getAllTicketsDetailsService(CalenderViewDto calenderViewDto) {
		ApiResponseDto apiResponseDto = new ApiResponseDto();

		try {
			List<String> ticketNumbers = Arrays.asList(calenderViewDto.getTicketNumbers()).get(0);
			List<Long> ticketNumbersLong = ticketNumbers.stream().map(e -> Long.parseLong(e))
					.collect(Collectors.toList());
			Criteria criteria = new Criteria().andOperator(Criteria.where("ticketNumber").in(ticketNumbersLong));

			Query q = new Query();
			q.addCriteria(criteria);
			q.with(Sort.by(Sort.Direction.ASC, "ticketNumber"));

			List<Ticket> list = mongoTemplate.find(q, Ticket.class);
			try {

				if (!list.get(0).getTicketNumber().isEmpty()) {
					apiResponseDto.setStatus(DispatchControllerConstants.STATUS_TRUE);
					apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
					apiResponseDto.setResponseData(list);
				}
			} catch (Exception e) {
				log.info("Unable to get tickets : {} ", e.getMessage());
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
				apiResponseDto.setMessage(DispatchControllerConstants.TICKET_NOT_AVAILABLE);
			}

		} catch (Exception e) {
			
			log.info("Unable to execute query due to {} ",e.getMessage());
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FALSE);
			apiResponseDto.setMessage(DispatchControllerConstants.FAILED_EXECUTE_QUERY);

		}
		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ApiResponseDto> getTechnicianDetailsById(TechnicianIdDto technicianIds) {
		ApiResponseDto apiResponseDto = new ApiResponseDto();

		log.info("{} Request body of getAgentByTechnicianID {}", technicianIds);

		try {
			if (technicianIds != null) {
				List<String> technicianId = Arrays.asList(technicianIds.getTechnicianIds()).get(0);
				Criteria criteria = new Criteria().andOperator(Criteria.where("technicianId").in(technicianId)
						.and("isActive").is("Y").and("agentType").is("Actual"));
				Query q = new Query();
				q.addCriteria(criteria);
				List<Agent> list = mongoTemplate.find(q, Agent.class);
				if (!list.isEmpty()) {
					apiResponseDto.setStatus("true");
					apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
					apiResponseDto.setResponseData(list);

					log.info("apiResponseDto : {} ", apiResponseDto);
				} else {
					apiResponseDto.setStatus("true");
					apiResponseDto.setMessage(DispatchControllerConstants.STATUS_TECHNICIAN_NOT_AVAILABLE);
					apiResponseDto.setResponseData(null);
					log.info("apiResponseDto : {} ", apiResponseDto);
				}
			}
		} catch (Exception e) {
			log.info(technicianIds + " Unable to find the data while getting getAgentByTechnicianID:{}",
					e.getMessage());
			apiResponseDto.setStatus("false");
			apiResponseDto.setMessage("Unable to find the Technician data" + e.getMessage());
		}

		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<ApiResponseDto> getTicketGlobalStatusCount(TicketCountDTO ticketDetails) {
		TicketCountResponse countResponse = new TicketCountResponse();
		ApiResponseDto apiResponseDto = new ApiResponseDto();

		log.info("{} Request body of getTicketStatusBubbleCount {}", ticketDetails);

		try {// UnAssigned / Assigned / Cancelled / Completed / MissingInfo
			if (ticketDetails != null) {
				String fieldName = DispatchControllerConstants.FIELD_GLOBAL_STATUS;
				String fromDate = ticketDetails.getStartDate();
				String toDate = ticketDetails.getEndDate();
				long completedCount = 0;
				long unassignedCount = 0;
				long missingInfoCount = 0;
				long assignedCount = 0;
				long cancelledCount = 0;
				if (StringUtils.isNotEmpty(ticketDetails.getStatus())) {
					if (DispatchControllerConstants.STATUS_COMPLETE.equalsIgnoreCase(ticketDetails.getStatus())) {
						completedCount = countByStatus(DispatchControllerConstants.STATUS_COMPLETE, ticketDetails,
								fieldName);
					} else if (DispatchControllerConstants.STATUS_UNASSIGNED
							.equalsIgnoreCase(ticketDetails.getStatus())) {
						unassignedCount = countByStatus(DispatchControllerConstants.STATUS_UNASSIGNED, ticketDetails,
								fieldName);
					} else if (DispatchControllerConstants.STATUS_ASSIGNED
							.equalsIgnoreCase(ticketDetails.getStatus())) {
						assignedCount = countByStatus(DispatchControllerConstants.STATUS_ASSIGNED, ticketDetails,
								fieldName);
					} else if (DispatchControllerConstants.STATUS_CANCELLED
							.equalsIgnoreCase(ticketDetails.getStatus())) {
						cancelledCount = countByStatus(DispatchControllerConstants.STATUS_CANCELLED, ticketDetails,
								fieldName);
					}
				} else {
					completedCount = countByStatus(DispatchControllerConstants.STATUS_COMPLETE, ticketDetails,
							fieldName);
					unassignedCount = countByStatus(DispatchControllerConstants.STATUS_UNASSIGNED, ticketDetails,
							fieldName);
					missingInfoCount = countByStatus(DispatchControllerConstants.STATUS_MISSING_INFO, ticketDetails,
							fieldName);
					assignedCount = countByStatus(DispatchControllerConstants.STATUS_ASSIGNED, ticketDetails,
							fieldName);
					cancelledCount = countByStatus(DispatchControllerConstants.STATUS_CANCELLED, ticketDetails,
							fieldName);

				}

				countResponse.setCompleted(completedCount);
				countResponse.setUnassigned(unassignedCount);
				countResponse.setMissingInfo(missingInfoCount);
				countResponse.setAssigned(assignedCount);
				countResponse.setCancelled(cancelledCount);

				long totalCount = completedCount + unassignedCount + assignedCount + cancelledCount + missingInfoCount;
				countResponse.setTotalCount(totalCount);

				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setResponseData(countResponse);

			} else {
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
				apiResponseDto.setMessage("Unable to execute Query");

			}

		} catch (Exception e) {
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
			apiResponseDto.setMessage("Unable to execute Query");
		}

		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

	public ResponseEntity<CountResponseDTO> advanceSearch(AdvanceSearchRequest request) {
		CountResponseDTO response = new CountResponseDTO();
		try {

			if (request == null || StringUtils.isAllEmpty(request.getSearchFor())) {
				log.info(" Bad Request received for advanceSearch where request is {} , for businessId : {}", request,
						BusinessContext.getTenantId());
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			CountResponseDTO responseData = ticketRepository.getAdvanceSearchData(request);

			responseData.setResponseText(DispatchControllerConstants.STATUS_SUCCESS);

			return new ResponseEntity<>(responseData, HttpStatus.OK);
		} catch (Exception e) {
			log.info("Unable to find the data while getting advanceSearch:{}", e.getMessage());
			response.setResponseText(DispatchControllerConstants.STATUS_FAILED);
			response.setResponseData(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public ResponseEntity<ApiResponseDto> updateByTicketNumber(List<TicketNumbersDto> ticketNumbersDto) {

		GroupByActionResponse groupByActionResponse = new GroupByActionResponse();
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		try {
			if (!CollectionUtils.isEmpty(ticketNumbersDto)) {
				for (TicketNumbersDto ticketNumber : ticketNumbersDto) {

					ResponseEntity<ApiResponseDto> responseEntity = assignBackToQueueRepoImpl
							.updateByTicketNumber(ticketNumber);
					String responseValue = responseEntity.getBody().getMessage();
					String responseKey = String.valueOf(ticketNumber.getTicketNumber());
					groupByActionResponse.getResponse().put(responseKey, responseValue);

				}
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			} else {
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
			}
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);

		} catch (Exception e) {
			log.info("Unable to BACKTOQUEUE");
			groupByActionResponse.getResponse().put(DispatchControllerConstants.STATUS_FAILED,
					DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@Override
	public ResponseEntity<CountResponseDTO> getAdvanceSearchFields(AdvanceSearchFieldRequest request) {
		CountResponseDTO response = new CountResponseDTO();
		try {
			Object responseData = ticketRepository.getAdvancedSearchFields(request);

			response.setResponseText(DispatchControllerConstants.STATUS_SUCCESS);
			response.setResponseData(responseData);

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			log.info("Unable to find the data while getting getAdvanceSearchFields:{}", e.getMessage());
			response.setResponseText(DispatchControllerConstants.STATUS_FAILED);
			response.setResponseData(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public ResponseEntity<ApiResponseDto> cancelByTicketNumber(List<TicketNumbersDto> ticketNumbersDto) {
		GroupByActionResponse groupByActionResponse = new GroupByActionResponse();
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		try {
			if (!CollectionUtils.isEmpty(ticketNumbersDto)) {
				for (TicketNumbersDto ticketNumber : ticketNumbersDto) {

					ResponseEntity<ApiResponseDto> responseEntity = assignBackToQueueRepoImpl
							.cancelByTicketNumber(ticketNumber);
					String responseValue = responseEntity.getBody().getMessage();
					String responseKey = String.valueOf(ticketNumber.getTicketNumber());
					groupByActionResponse.getResponse().put(responseKey, responseValue);
				}
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			} else {
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
			}
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		} catch (Exception e) {
			log.info("Unable to cancel ticket");
			groupByActionResponse.getResponse().put(DispatchControllerConstants.STATUS_FAILED,
					DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public ResponseEntity<ApiResponseDto> transferTicketByIds(TransferTicketDTO transferTicketByIds) {
		ApiResponseDto response = new ApiResponseDto();
		String businessId = BusinessContext.getTenantId();
		String businessToken = BusinessTokenContext.getBusinessToken();

		try {
			String globalStatus = DispatchControllerConstants.STATUS_ASSIGNED;
			String actionStatus = DispatchControllerConstants.STATUS_TRANSFERED;
			String action = DispatchControllerConstants.STATUS_TRANSFERED;
			String actionBy = transferTicketByIds.getActionBy();
			String fromTechnicianId = transferTicketByIds.getFromTechnicianId();
			String toTechnicianId = transferTicketByIds.getToTechnicianId();
			String ticketNumber = transferTicketByIds.getTicketNumbers();

			log.info("Inside from technicianId {} , to technicianId {} transfer", fromTechnicianId, toTechnicianId);

			log.info(" ticketNumbers {} ", ticketNumber);

			Ticket ticketId = ticketdetails.findByTicketNumber(ticketNumber);
			if (ticketId == null) {
				log.info("Unable to find the Ticket data {} ", ticketNumber);

				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_FAILED);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_CANCELLED)) {
				log.info("Ticket ID {} is alerady been cancelled ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
						+ DispatchControllerConstants.STATUS_CANCELLED);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_MISSING_INFO)) {
				log.info("Ticket ID {} is having MissingInfo ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_MISSING_INFO);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_COMPLETED)) {
				log.info("Ticket ID {} is completed ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
						+ DispatchControllerConstants.STATUS_COMPLETED);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_RESCHEDULE)) {
				log.info("Ticket ID {} is rescheduled ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
						+ DispatchControllerConstants.STATUS_RESCHEDULE);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getTechnicianId() == fromTechnicianId) {
				if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_TRANSFERED)) {
					log.info("Ticket ID {} is alerady been transfered ", ticketNumber);
					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
							+ DispatchControllerConstants.STATUS_TRANSFERED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
			}

			actionBy = StringUtils.isEmpty(transferTicketByIds.getActionBy()) ?ticketId.getSupervisorId()+":"+ticketId.getSupervisorName() : transferTicketByIds.getActionBy() ;
			
			String pattern = "\\d+:.*";
			Pattern regex = Pattern.compile(pattern);
			Matcher matcher = regex.matcher(actionBy);

			if (!matcher.matches()) {
				log.info("Pattern not match for {}", ticketId.getTicketNumber());
				actionBy = ticketId.getSupervisorId() + ":" + ticketId.getSupervisorName();
			}

			// Find the tickets to transfer based on ticketNumber and fromtechnicianId
			AgentAssignmentSolutionModel fromTechnicianAssignment = assignmentRepository
					.findByAgentTechnicianIdAndAgentTicketListTicketNumber(fromTechnicianId, ticketNumber).findFirst()
					.orElse(null);

			log.info(" fromTechnicianId {} ", fromTechnicianAssignment);

			// Handle case when the ticket is not found for the given fromTechnicianId
			// return Bad Request
			if (fromTechnicianAssignment == null
					&& ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED)) {

				log.info("Unable to find the data fromTechnicianId {} ", fromTechnicianId);

				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(fromTechnicianId + " " + DispatchControllerConstants.STATUS_FAILED);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}

			if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED)
					|| ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_PAST_DUE)) {

				ResponseEntity<ApiResponseDto> responseEntity = UpdatetoTechnicianTransferTicketDetails(action,
						actionBy, fromTechnicianId, toTechnicianId, ticketId, ticketNumber);
				log.info("UnAssigned Ticket  responce dto ", responseEntity);

				return responseEntity;

			}

			// Find the ticket to be transferred in the fromTechnician's ticket list
			Ticket ticketToTransfer = null;
			List<Ticket> ticketList = fromTechnicianAssignment.getAgent().getTicketList();

			for (Ticket ticket : ticketList) {
				if (ticket.getTicketNumber().equalsIgnoreCase(ticketNumber)) {
					ticketToTransfer = ticket;
					break;
				}
			}

			Location specificTicketLocation = null;
			Location precedingTicketLocation = null;

			for (int i = 0; i < ticketList.size(); i++) {
				Ticket ticket = ticketList.get(i);
				log.info(" ticket {} ", ticket);
				if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber)) {
					specificTicketLocation = ticket.getLocation();
					log.info(" specificTicketLocation {} ", specificTicketLocation);
					// Check if there is a preceding ticket
					if (i > 0) {
						precedingTicketLocation = ticketList.get(i - 1).getLocation();
						log.info(" precedingTicketLocation {} ", precedingTicketLocation);
					} else {
						precedingTicketLocation = fromTechnicianAssignment.getAgent().getLocation();
						log.info(" Agent Location {} ", precedingTicketLocation);
					}
					break;
				}

			}

			// Remove ticket from the fromTechnician
			if (ticketToTransfer != null) {
				ticketList.remove(ticketToTransfer);

			}

			// Update TotalWorkHourGlobal and AssignmentStatus of fromTechnicianAssignment
			/*
			 * long travelTime =
			 * dispatchControllerSupportUtils.calculateDistance(precedingTicketLocation,
			 * specificTicketLocation) / 60000;
			 */
			RouteSolverPath routeSolverPath = dispatchControllerSupportUtils
					.getLocationDistanceDetails(precedingTicketLocation, specificTicketLocation);
			long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath)
					? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME
					: routeSolverPath.getTime();
			double evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
			long travelTime = travelTimeLocal / 60000;

			/*
			 * long workHourTime =
			 * fromTechnicianAssignment.getAgent().getTotalWorkHourGlobal() - (travelTime +
			 * MINUTES .between(ticketToTransfer.getWorkBeginDateTime(),
			 * ticketToTransfer.getWorkCompletionDateTime()));
			 */

			// WorkBegintime and WorkCompletion Time for Contractor not for technician.

			long workHourTime = fromTechnicianAssignment.getAgent().getTotalWorkHourGlobal()
					- (travelTime + ticketToTransfer.getTicketETA());

			double evaluatedDistance = fromTechnicianAssignment.getAgent().getEvaluatedDistance()
					- evaluatedDistanceLocal;

			if (workHourTime < 0) {
				workHourTime = 0;
			}

			if (evaluatedDistance < 0) {
				evaluatedDistance = 0;
			}

			// Set TotalWorkHourGlobal to 0 if ticketlist is empty for fromTechnician
			if (fromTechnicianAssignment.getAgent().getTicketList() == null) {
				workHourTime = 0;
				evaluatedDistance = 0;
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

			// Find the toTechnicianId based on currentDate and toTechnicianId from
			// technicianAssignmentSolution
			LocalDate currentDate = LocalDate.now();
			LocalDateTime startOfDay = currentDate.atStartOfDay();
			LocalDateTime endOfDay = currentDate.atTime(LocalTime.MAX);

			Sort sort = Sort.by(Sort.Order.desc(DispatchControllerConstants.TIMESTAMP));
			AgentAssignmentSolutionModel toTechnicianAssignment = assignmentRepository
					.findByAgentTechnicianIdAndTimestampBetween(toTechnicianId, startOfDay, endOfDay, sort).findFirst()
					.orElse(null);

			log.info(" currentDate {} startOfDay {} endOfDay {} ", currentDate, startOfDay, endOfDay);
			log.info(" toTechnicianAssignment {} ", toTechnicianAssignment);

			// Handle case when the toTechnicianId is not found form the given
			// technicianAssignmentSolution Document , find it from technicianDCSolver
			// Document
			if (toTechnicianAssignment == null) {
				log.info(
						"Unable to find the data of ToTechnicianId {} in technicianAssignmentSolution Document ,now fetching it from technicianDCSolver Document",
						fromTechnicianId);
				Agent toTechnician = technicianDataRepo.findByTechnicianId(toTechnicianId);
				log.info(" ToTechnicianId 1 {} ", toTechnician);
				// Handle case when the toTechnicianId is not found in TechnicanDCSolver
				// Document-return Bad Request
				if (toTechnician == null) {

					log.info("Unable to find the data ToTechnicianId in {} technicianDCSolver Document",
							fromTechnicianId);
					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_FAILED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

				// Set the required fields toTechnicianId if not found in TechnicanDCSolver
				// Document
				toTechnicianAssignment = new AgentAssignmentSolutionModel();
				toTechnicianAssignment.setDcSolverProcessId(fromTechnicianAssignment.getDcSolverProcessId());
				toTechnicianAssignment.setDcSolverTaskId(fromTechnicianAssignment.getDcSolverTaskId());

				toTechnicianAssignment.setTimestamp(LocalDateTime.now());
				toTechnicianAssignment.setAgent(toTechnician);
				log.info(" Creating new Document 1 {} ", toTechnicianAssignment);

				TicketActionTrail ticketActionTrail = ticketActionTrail(action, actionBy,
						fromTechnicianId + ":" + fromTechnicianAssignment.getAgent().getFirstName() + " "
								+ fromTechnicianAssignment.getAgent().getLastName(),
						toTechnicianId + ":" + toTechnician.getFirstName() + " " + toTechnician.getLastName());
				log.info(" updated the details of ticketActionTrail  {} ", ticketActionTrail);
				// Update the required fields of ticket in TechnicanDCSolver and ticketDCSolver
				// and TechnicianAssignmentSolution Document
				updateTicketdetails(ticketToTransfer, toTechnician, ticketActionTrail, globalStatus, actionStatus);

				log.info(" ticketToTransfer  {} ", ticketToTransfer);
				log.info(" toTechnician 1 {} ", toTechnician);
				log.info(" fromTechnicianAssignment  {} ", fromTechnicianAssignment);

				// Add the ticket to the toTechnician
				List<Ticket> toTechnicianTicketList = toTechnicianAssignment.getAgent().getTicketList();
				toTechnicianTicketList.add(ticketToTransfer);

				log.info(" totechnicianLocation {} ", toTechnicianAssignment.getAgent().getLocation());
				// long toTravelTime =
				// dispatchControllerSupportUtils.calculateDistance(toTechnicianAssignment.getAgent().getLocation(),
				// specificTicketLocation) / 60000;

				RouteSolverPath toRouteSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(
						toTechnicianAssignment.getAgent().getLocation(), specificTicketLocation);
				long toTravelTimeLocal = ObjectUtils.isEmpty(toRouteSolverPath)
						? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME
						: toRouteSolverPath.getTime();
				double toEvaluatedDistanceLocal = ObjectUtils.isEmpty(toRouteSolverPath) ? 0.0
						: toRouteSolverPath.getDistance();
				long toTravelTime = toTravelTimeLocal / 60000;

				long toWorkHourTime = toTechnicianAssignment.getAgent().getTotalWorkHourGlobal() + toTravelTime
						+ ticketToTransfer.getTicketETA();
				double toEvaluatedDistance = toTechnicianAssignment.getAgent().getEvaluatedDistance()
						+ toEvaluatedDistanceLocal;

				if (toWorkHourTime < 0) {
					toWorkHourTime = 0;
				}
				toTechnicianAssignment.getAgent().setTotalWorkHourGlobal(toWorkHourTime);
				toTechnicianAssignment.getAgent().setEvaluatedDistance(toEvaluatedDistance);

				log.info(" total work Time of toTechnicianAssignment  {} ", workHourTime);
				String toAssignmentStatus = toTechnicianAssignment.getAgent()
						.getAvailableTime() > toTechnicianAssignment.getAgent().getTotalWorkHourGlobal()
								? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED
								: toTechnicianAssignment.getAgent().getAvailableTime() == toTechnicianAssignment
										.getAgent().getTotalWorkHourGlobal()
												? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED
												: DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;

				toTechnicianAssignment.getAgent().setAssignmentStatus(toAssignmentStatus);
				log.info(" AssignmentStatus of toTechnicianAssignment  {} ", assignmentStatus);

				// Save the updated documents
				technicianDataRepo.save(toTechnicianAssignment.getAgent());
				technicianDataRepo.save(fromTechnicianAssignment.getAgent());
				assignmentRepository.save(fromTechnicianAssignment);
				assignmentRepository.save(toTechnicianAssignment);

				if (ticketToTransfer.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES)) {
					try {
						Ticket ticketLocal = ticketToTransfer;
						Agent assignAgent = toTechnicianAssignment.getAgent();
						executor.execute(() -> {
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(businessToken);
							dispatchControllerSupportUtils.notifySupervisorAndTech(assignAgent, ticketLocal,
									DispatchControllerConstants.STATUS_ASSIGNED, businessId, businessToken);
						});
					} catch (Exception e) {
						log.info("Unable to notifySupervisorAndTech due to {} ", e.getMessage());
					}

				}

				log.info(" toTechnicianAssignment 1 : {} ", toTechnicianAssignment);
				log.info(" fromTechnicianAssignment 1 : {} ", fromTechnicianAssignment);

				response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
				response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);

				return new ResponseEntity<>(response, HttpStatus.OK);

			}
			Agent toTechnicianFNLN = technicianDataRepo.findByTechnicianId(toTechnicianId);

			TicketActionTrail ticketActionTrail = ticketActionTrail(action, actionBy,
					fromTechnicianId + ":" + fromTechnicianAssignment.getAgent().getFirstName() + " "
							+ fromTechnicianAssignment.getAgent().getLastName(),
					toTechnicianId + ":" + toTechnicianFNLN.getFirstName() + " " + toTechnicianFNLN.getLastName());

			log.info(" updated the details of ticketActionTrail  {} ", ticketActionTrail);

			updateTicketdetails(ticketToTransfer, toTechnicianAssignment.getAgent(), ticketActionTrail, globalStatus,
					actionStatus);

			// Add the ticket to the toTechnician
			List<Ticket> toTechnicianTicketList = toTechnicianAssignment.getAgent().getTicketList();
			toTechnicianTicketList.add(ticketToTransfer);

			List<Ticket> toTicketList = toTechnicianAssignment.getAgent().getTicketList();

			Location toTicketLocation = ticketToTransfer.getLocation();
			Location toPrecedingTicketLocation = null;

			for (int i = 0; i < toTicketList.size(); i++) {
				Ticket ticket = toTicketList.get(i);
				log.info(" ticket {} ", ticket);
				if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber)) {
					toTicketLocation = ticket.getLocation();
					log.info(" toTicketLocation {} ", specificTicketLocation);
					// Check if there is a preceding ticket
					if (i > 0) {
						toPrecedingTicketLocation = toTicketList.get(i - 1).getLocation();
						log.info(" toPrecedingTicketLocation {} ", precedingTicketLocation);
					} else {
						toPrecedingTicketLocation = toTechnicianAssignment.getAgent().getLocation();
						log.info(" At 1 toPrecedingTicketLocation {} ", toPrecedingTicketLocation);
					}
					break;
				}

			}

			// Update TotalWorkHourGlobal and AssignmentStatus of toTechnicianAssignment
			log.info(" totechnicianLocation 1 {} ", toTechnicianAssignment.getAgent().getLocation());

			// long toTravelTime =
			// dispatchControllerSupportUtils.calculateDistance(toPrecedingTicketLocation,
			// toTicketLocation) / 60000;

			RouteSolverPath toRouteSolverPath = dispatchControllerSupportUtils
					.getLocationDistanceDetails(toPrecedingTicketLocation, toTicketLocation);
			long toTravelTimeLocal = ObjectUtils.isEmpty(toRouteSolverPath)
					? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME
					: toRouteSolverPath.getTime();
			double toEvaluatedDistanceLocal = ObjectUtils.isEmpty(toRouteSolverPath) ? 0.0
					: toRouteSolverPath.getDistance();
			long toTravelTime = toTravelTimeLocal / 60000;

			long toWorkHourTime = toTechnicianAssignment.getAgent().getTotalWorkHourGlobal() + toTravelTime
					+ ticketToTransfer.getTicketETA();
			double toEvaluatedDistance = toTechnicianAssignment.getAgent().getEvaluatedDistance()
					+ toEvaluatedDistanceLocal;

			if (toWorkHourTime < 0) {
				toWorkHourTime = 0;
			}

			toTechnicianAssignment.getAgent().setTotalWorkHourGlobal(toWorkHourTime);
			toTechnicianAssignment.getAgent().setEvaluatedDistance(toEvaluatedDistance);

			log.info(" total work Time of fromTechnicianAssignment  {} ", workHourTime);
			String toAssignmentStatus = toTechnicianAssignment.getAgent().getAvailableTime() > toTechnicianAssignment
					.getAgent().getTotalWorkHourGlobal()
							? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED
							: toTechnicianAssignment.getAgent().getAvailableTime() == toTechnicianAssignment.getAgent()
									.getTotalWorkHourGlobal()
											? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED
											: DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;

			toTechnicianAssignment.getAgent().setAssignmentStatus(toAssignmentStatus);

			log.info(" AssignmentStatus of fromTechnicianAssignment  {} ", assignmentStatus);
			// Save the updated documents
			technicianDataRepo.save(toTechnicianAssignment.getAgent());
			technicianDataRepo.save(fromTechnicianAssignment.getAgent());
			assignmentRepository.save(fromTechnicianAssignment);
			assignmentRepository.save(toTechnicianAssignment);

			if (ticketToTransfer.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES)) {
				try {
					Ticket ticketLocal = ticketToTransfer;
					Agent assignAgent = toTechnicianAssignment.getAgent();
					executor.execute(() -> {
						BusinessContext.setTenantId(businessId);
						BusinessTokenContext.setBusinessToken(businessToken);
						dispatchControllerSupportUtils.notifySupervisorAndTech(assignAgent, ticketLocal,
								DispatchControllerConstants.STATUS_ASSIGNED, businessId, businessToken);
					});
				} catch (Exception e) {
					log.info("Unable to notifySupervisorAndTech due to {} ", e.getMessage());
				}

			}

			log.info(" toTechnicianAssignment in solution Document {} ", toTechnicianAssignment);
			log.info(" fromTechnicianAssignment in solution Document  {} ", toTechnicianAssignment);

			response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
			response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			log.info(" Unable to find the data while getting getAgentByTechnicianID", e);
			response.setStatus(DispatchControllerConstants.STATUS_FAILED);
			response.setMessage("Unable to find the Technician data" + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<ApiResponseDto> transferTicketBySupervisorIds(TransferTicketDTO transferTicketByIds) {
		ApiResponseDto response = new ApiResponseDto();

		String businessId = BusinessContext.getTenantId();
		String businessToken = BusinessTokenContext.getBusinessToken();

		try {
			String globalStatus = DispatchControllerConstants.STATUS_UNASSIGNED;
			String actionStatus = DispatchControllerConstants.STATUS_TRANSFERED;
			String action = DispatchControllerConstants.STATUS_TRANSFERED;
			String actionBy = transferTicketByIds.getActionBy();
			String fromSupervisorId = transferTicketByIds.getFromSupervisorId();
			String toSupervisorId = transferTicketByIds.getToSupervisorId();
			String ticketNumber = transferTicketByIds.getTicketNumbers();

			String fromSupervisorName = "";

			String toSupervisorName = "";

			log.info("Inside from Supervisor {} , to Supervisor {} transfer", fromSupervisorId, toSupervisorId);

			log.info(" ticketNumbers {} ", ticketNumber);
			
			log.debug("ticket details fetch from db :Start {} and businessId: {} ",ticketNumber,businessId);
			
			Ticket ticketId = ticketdetails.findByTicketNumber(ticketNumber);
			log.debug("ticket details fetch from db :End {} and businessId: {} ",ticketNumber,businessId);
			
			if (ticketId == null) {
				log.info("Unable to find the Ticket data {} ", ticketNumber);

				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_FAILED);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_CANCELLED)) {
				log.info("Ticket ID {} is alerady been cancelled ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
						+ DispatchControllerConstants.STATUS_CANCELLED);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_MISSING_INFO)) {
				log.info("Ticket ID {} is having MissingInfo ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_MISSING_INFO);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_COMPLETED)) {
				log.info("Ticket ID {} is completed ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
						+ DispatchControllerConstants.STATUS_COMPLETED);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_RESCHEDULE)) {
				log.info("Ticket ID {} is rescheduled ", ticketNumber);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
						+ DispatchControllerConstants.STATUS_RESCHEDULE);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

			} else if (ticketId.getSupervisorId() == toSupervisorId) {
				if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_TRANSFERED)) {
					log.info("Ticket ID {} is alerady been transfered ", ticketNumber);
					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_ALREADY + " "
							+ DispatchControllerConstants.STATUS_TRANSFERED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
			}
			log.debug("Exist : Sup {} and Sup {} check in db :Start {} and businessId: {} ",fromSupervisorId,toSupervisorId,businessId);
			
			boolean isFromSupervisorPresent = supervisorRepo.existsBySupervisorId(fromSupervisorId);
			boolean isToSupervisorPresent = supervisorRepo.existsBySupervisorId(toSupervisorId);
			log.debug("Exist : Sup {} and Sup {} check in db :End {} and businessId: {} ",fromSupervisorId,toSupervisorId,businessId);
			
			if (!isFromSupervisorPresent) {
				log.info("From Supervisor ID {} is not present ", fromSupervisorId);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.FROM_SUPERVISOR_NOT_PRESENT);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			if (!isToSupervisorPresent) {
				log.info("To Supervisor ID {} is not present ", toSupervisorId);
				response.setStatus(DispatchControllerConstants.STATUS_FAILED);
				response.setMessage(DispatchControllerConstants.TO_SUPERVISOR_NOT_PRESENT);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
			log.debug("FetchDetails : Sup {} and Sup {} from db :Start {} and businessId: {} ",fromSupervisorId,toSupervisorId,businessId);
			
			SupervisorPolygonMapping fromSupervisorDetails = supervisorRepo.findBySupervisorId(fromSupervisorId);
			SupervisorPolygonMapping toSupervisorDetails = supervisorRepo.findBySupervisorId(toSupervisorId);
			log.debug("FetchDetails : Sup {} and Sup {} from db :End {} and businessId: {} ",fromSupervisorId,toSupervisorId,businessId);
			
			fromSupervisorName = fromSupervisorDetails.getFirstName() + " " + fromSupervisorDetails.getLastName();
			toSupervisorName = toSupervisorDetails.getFirstName() + " " + toSupervisorDetails.getLastName();

			actionBy = StringUtils.isEmpty(transferTicketByIds.getActionBy()) ?ticketId.getSupervisorId()+":"+ticketId.getSupervisorName() : transferTicketByIds.getActionBy() ;
			
			String pattern = "\\d+:.*";
			Pattern regex = Pattern.compile(pattern);
			Matcher matcher = regex.matcher(actionBy);

			if (!matcher.matches()) {
				log.info("Pattern not match for {}", ticketId.getTicketNumber());
				actionBy = ticketId.getSupervisorId() + ":" + ticketId.getSupervisorName();
			}

			// Check ticket is unassigned or assigned
			if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED)
					|| ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_PASTDUE)) {
				log.info("Ticket global status is  {}  ", ticketId.getGlobalStatus());

				ticketId.setSupervisorId(toSupervisorId);
				ticketId.setSupervisorName(toSupervisorName);
				ticketId.setSupervisorPolygonId(toSupervisorId);
				ticketId.setGlobalStatus(globalStatus);
				
				TicketActionTrail ticketActionTrail = ticketActionTrail(action, actionBy,
						fromSupervisorId + ":" + fromSupervisorName, toSupervisorId + ":" + toSupervisorName);
				ticketId.getTicketActionTrails().add(ticketActionTrail);

				if (!StringUtils.equalsIgnoreCase(ticketId.getIsAssistTicket(), DispatchControllerConstants.YES)) {
					// Updating master ticket collection
					LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();

					lumenCollectionUpdateDTO.setGlobalStatus(globalStatus);
					lumenCollectionUpdateDTO.setActionOnTicket(actionStatus);
					lumenCollectionUpdateDTO.setTechnicianId("");
					lumenCollectionUpdateDTO.setTechnicianFirstName("");
					lumenCollectionUpdateDTO.setTechnicianLastName("");
					lumenCollectionUpdateDTO.setTechnicianEmailId("");
					lumenCollectionUpdateDTO.setSupervisorName(toSupervisorName);
					lumenCollectionUpdateDTO.setSupervisorId(toSupervisorId);
					lumenCollectionUpdateDTO.setAssignmentDateTime(LocalDateTime.now());
					lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
					lumenCollectionUpdateDTO.setTicketNumber(ticketId.getTicketNumber());
					lumenCollectionUpdateDTO.setConversationId(ticketId.getConversationId());

					ticketRepository.updateLumenTicketCollection(lumenCollectionUpdateDTO);
					try {
						executor.execute(() -> {
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(businessToken);
							dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO, businessId, businessToken);
						});
					} catch (Exception e) {
						log.info("Unable to save Audit due to {} ", e.getMessage());
					}
				}
				log.info(" updateTicketdetails 1 {} ", ticketId);

				// Save Supervisor details to
				ticketdetails.save(ticketId);
				log.info("Business Id : {} Request to transfer ticket from : {} to {} with status : {}",BusinessContext.getTenantId(),fromSupervisorId,ticketId.getSupervisorId(),ticketId.getGlobalStatus());

				if (ticketId.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES)) {
					try {
						executor.execute(() -> {
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(businessToken);
							dispatchControllerSupportUtils.notifySupervisorAndTech(null, ticketId,
									DispatchControllerConstants.STATUS_UNASSIGNED, businessId, businessToken);
						});
					} catch (Exception e) {
						log.info("Unable to notifySupervisorAndTech due to {} ", e.getMessage());
					}

				}
			} else if (ticketId.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED)) {
				log.info("Ticket global status is  {}  ", ticketId.getGlobalStatus());

				// Find the tickets to transfer based on ticketNumber and fromSupervisorId
				AgentAssignmentSolutionModel fromSupervisorAssignment = assignmentRepository
						.findByAgentSupervisorIdAndAgentTicketListTicketNumber(fromSupervisorId, ticketNumber)
						.findFirst().orElse(null);

				log.info(" fromSupervisorAssignment {} ", fromSupervisorAssignment);
				log.info(" fromSupervisorAssignment.getAgent().getTotalWorkHourGlobal() {} ",
						fromSupervisorAssignment.getAgent().getTotalWorkHourGlobal());

				// Handle case when the ticket is not found for the given fromTechnicianId
				// return Bad Request
				if (fromSupervisorAssignment == null) {

					log.info("Unable to find the data fromSupervisorAssignment {} ", fromSupervisorAssignment);

					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(fromSupervisorAssignment + " " + DispatchControllerConstants.STATUS_FAILED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}
				// Find the ticket to be transferred in the fromTechnician's ticket list
				Ticket ticketToTransfer = null;
				List<Ticket> ticketList = fromSupervisorAssignment.getAgent().getTicketList();

				for (Ticket ticket : ticketList) {
					if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber)) {
						ticketToTransfer = ticket;
						break;
					}
				}
				log.info(" ticketToTransfer {} ", ticketToTransfer);

				Location specificTicketLocation = null;
				Location precedingTicketLocation = null;

				for (int i = 0; i < ticketList.size(); i++) {
					Ticket ticket = ticketList.get(i);
					log.info(" ticket {} ", ticket);
					if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber)) {
						specificTicketLocation = ticket.getLocation();
						log.info(" specificTicketLocation {} ", specificTicketLocation);
						// Check if there is a preceding ticket
						if (i > 0) {
							precedingTicketLocation = ticketList.get(i - 1).getLocation();
							log.info(" precedingTicketLocation {} ", precedingTicketLocation);
						} else {
							precedingTicketLocation = fromSupervisorAssignment.getAgent().getLocation();
							log.info(" Agent Location {} ", precedingTicketLocation);
						}
						break;
					}

				}

				// Remove ticket from the fromSupervisor
				if (ticketToTransfer != null) {
					ticketList.remove(ticketToTransfer);

				}

				// Update TotalWorkHourGlobal and AssignmentStatus of fromSupervisorAssignment
				/*
				 * long travelTime =
				 * dispatchControllerSupportUtils.calculateDistance(precedingTicketLocation,
				 * specificTicketLocation) / 60000;
				 */

				RouteSolverPath routeSolverPath = dispatchControllerSupportUtils
						.getLocationDistanceDetails(precedingTicketLocation, specificTicketLocation);
				long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath)
						? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME
						: routeSolverPath.getTime();
				double evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0
						: routeSolverPath.getDistance();

				long travelTime = travelTimeLocal / 60000;

				/*
				 * long workHourTime =
				 * fromTechnicianAssignment.getAgent().getTotalWorkHourGlobal() - (travelTime +
				 * MINUTES .between(ticketToTransfer.getWorkBeginDateTime(),
				 * ticketToTransfer.getWorkCompletionDateTime()));
				 */

				// WorkBegintime and WorkCompletion Time for Contractor not for technician.

				long workHourTime = fromSupervisorAssignment.getAgent().getTotalWorkHourGlobal()
						- (travelTime + ticketToTransfer.getTicketETA());
				double evaluatedDistance = fromSupervisorAssignment.getAgent().getEvaluatedDistance()
						- evaluatedDistanceLocal;

				if (evaluatedDistance < 0) {
					evaluatedDistance = 0;
				}

				if (workHourTime < 0) {
					workHourTime = 0;
				}

				// Set TotalWorkHourGlobal to 0 if ticketlist is empty for fromTechnician
				if (fromSupervisorAssignment.getAgent().getTicketList() == null) {
					workHourTime = 0;
					evaluatedDistance = 0;
				}

				fromSupervisorAssignment.getAgent().setTotalWorkHourGlobal(workHourTime);
				fromSupervisorAssignment.getAgent().setEvaluatedDistance(evaluatedDistance);

				log.info(" ticketToTransfer.getTicketETA() {} ", ticketToTransfer.getTicketETA());
				log.info(" travelTime of fromSupervisorAssignment  {} ", travelTime);
				log.info(" travel work time before of fromSupervisorAssignment  {} ",
						fromSupervisorAssignment.getAgent().getTotalWorkHourGlobal());
				log.info(" total work Time of fromSupervisorAssignment  {} ", workHourTime);
				String assignmentStatus = fromSupervisorAssignment.getAgent()
						.getAvailableTime() > fromSupervisorAssignment.getAgent().getTotalWorkHourGlobal()
								? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED
								: fromSupervisorAssignment.getAgent().getAvailableTime() == fromSupervisorAssignment
										.getAgent().getTotalWorkHourGlobal()
												? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED
												: DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;

				fromSupervisorAssignment.getAgent().setAssignmentStatus(assignmentStatus);

				TicketActionTrail ticketActionTrail = ticketActionTrail(action, actionBy,
						fromSupervisorId + ":" + fromSupervisorDetails.getFirstName() + " "
								+ fromSupervisorDetails.getLastName(),
						toSupervisorId + ":" + toSupervisorDetails.getFirstName() + " "
								+ toSupervisorDetails.getLastName());
				log.info(" updated the details of ticketActionTrail  {} ", ticketActionTrail);
				// Update the required fields of ticket in TechnicanDCSolver and ticketDCSolver
				// and TechnicianAssignmentSolution Document
				updateTicketdetailsForToSupervisor(ticketId, toSupervisorId, toSupervisorName, ticketActionTrail,
						globalStatus, actionStatus);

				log.info(" ticketToTransfer 1 {} ", ticketToTransfer);
				log.info(" toTechnician 1A {} ", toSupervisorId);
				log.info(" fromTechnicianAssignment 1 {} ", fromSupervisorId);

				log.info(" AssignmentStatus of fromTechnicianAssignment  {} ", assignmentStatus);
				// Save the updated documents

				technicianDataRepo.save(fromSupervisorAssignment.getAgent());
				assignmentRepository.save(fromSupervisorAssignment);

				if (ticketId.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES)) {
					try {
						executor.execute(() -> {
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(businessToken);
							dispatchControllerSupportUtils.notifySupervisorAndTech(null, ticketId,
									DispatchControllerConstants.STATUS_UNASSIGNED, businessId, businessToken);
						});
					} catch (Exception e) {
						log.info("Unable to notifySupervisorAndTech due to {} ", e.getMessage());
					}

				}

				response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
				response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);

				return new ResponseEntity<>(response, HttpStatus.OK);

			}

			response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
			response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			log.info(" Unable to find the data due to : {}", e);
			response.setStatus(DispatchControllerConstants.STATUS_FAILED);
			response.setMessage("Unable to find the Supervisor data" + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public Ticket updateTicketdetailsForToSupervisor(Ticket ticketDetails, String toSupervisorId,
			String toSupervisorName, TicketActionTrail ticketActionTrail, String globalStatus, String actionStatus) {

		// Updating TicketDCSolver
		ticketDetails.getTicketActionTrails().add(ticketActionTrail);
		ticketDetails.setGlobalStatus(globalStatus);
		ticketDetails.setActionOnTicket(actionStatus);
		ticketDetails.setTechnicianId("");
		ticketDetails.setTechnicianFirstName("");
		ticketDetails.setTechnicianLastName("");
		ticketDetails.setTechnicianEmailId("");
		ticketDetails.setSupervisorId(toSupervisorId);
		ticketDetails.setSupervisorName(toSupervisorName);
		ticketDetails.setTransferCount(ticketDetails.getTransferCount() + 1);
		ticketDetails.setSupervisorPolygonId(toSupervisorId);

		if (StringUtils.equalsIgnoreCase(actionStatus, DispatchControllerConstants.STATUS_TRANSFERED)) {
			ticketDetails.setAssignmentDateTime(LocalDateTime.now());
		}
		ticketdetails.save(ticketDetails);
		if (!StringUtils.equalsIgnoreCase(ticketDetails.getIsAssistTicket(), DispatchControllerConstants.YES)) {
			// Updating master ticket collection
			LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();

			lumenCollectionUpdateDTO.setGlobalStatus(globalStatus);
			lumenCollectionUpdateDTO.setActionOnTicket(actionStatus);
			lumenCollectionUpdateDTO.setTechnicianId("");
			lumenCollectionUpdateDTO.setTechnicianFirstName("");
			lumenCollectionUpdateDTO.setTechnicianLastName("");
			lumenCollectionUpdateDTO.setTechnicianEmailId("");
			lumenCollectionUpdateDTO.setSupervisorName(toSupervisorName);
			lumenCollectionUpdateDTO.setSupervisorId(toSupervisorId);
			lumenCollectionUpdateDTO.setAssignmentDateTime(LocalDateTime.now());
			lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
			lumenCollectionUpdateDTO.setTicketNumber(ticketDetails.getTicketNumber());
			lumenCollectionUpdateDTO.setConversationId(ticketDetails.getConversationId());

			String businessId = BusinessContext.getTenantId();
			String businessToken = BusinessTokenContext.getBusinessToken();

			ticketRepository.updateLumenTicketCollection(lumenCollectionUpdateDTO);
			try {
				executor.execute(() -> {
					BusinessContext.setTenantId(businessId);
					BusinessTokenContext.setBusinessToken(businessToken);
					dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO, businessId, businessToken);
				});
			} catch (Exception e) {
				log.info("Unable to save Audit due to {} ", e.getMessage());
			}
		}
		log.info(" updateTicketdetails 1 {} ", ticketDetails);
		return ticketDetails;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<CountResponseDTO> getTicketDetailsBySupervisorId(TicketDetailsBySupervisorIdRequest request) {
		CountResponseDTO response = new CountResponseDTO();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {
			Page<Ticket> ticketList = null;
			Integer pageSize = (request.getPageSize() != null && request.getPageSize() > 0) ? request.getPageSize()
					: DispatchControllerConstants.DEFAULT_PAGE_SIZE;
			Integer pageNumber = (request.getPageNo() != null && request.getPageNo() > 0) ? (request.getPageNo() - 1)
					: DispatchControllerConstants.DEFAULT_PAGE_NUMBER;

			Pageable processPage = PageRequest.of(pageNumber, pageSize,
					Sort.by(DispatchControllerConstants.TICKETDUEDATEANDTIME).descending());
			LocalDateTime startDate;
			LocalDateTime endDate;
			LocalDateTime ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
			if (!StringUtils.isEmpty(request.getStartDate()) && !StringUtils.isEmpty(request.getEndDate())
					&& !StringUtils.isEmpty(request.getTicketStatus())
					&& !StringUtils.isEmpty(request.getSupervisorId())) {
				startDate = LocalDateTime.parse(request.getStartDate(), formatter);
				endDate = LocalDateTime.parse(request.getEndDate(), formatter);
				if (request.getTicketStatus().equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketList = ticketdetails.findByGlobalStatusInAndSupervisorIdAndDueDateBetween(
							new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
									DispatchControllerConstants.STATUS_RESCHEDULE },
							request.getSupervisorId(), startDate, endDate, processPage);
				} else if (StringUtils.equalsIgnoreCase(request.getTicketStatus(),
						DispatchControllerConstants.STATUS_COMPLETE)) {
					 ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

					ticketList = ticketdetails
							.findByGlobalStatusInAndSupervisorIdAndDueDateBetweenAndCompleteDateTimeGreatherThan(
									request.getTicketStatus(), request.getSupervisorId(), startDate, endDate, ldt_start,
									processPage);
				} else {
					ticketList = ticketdetails.findByGlobalStatusAndSupervisorIdAndDueDateBetween(
							request.getTicketStatus(), request.getSupervisorId(), startDate, endDate, processPage);
				}

			} else if (StringUtils.isEmpty(request.getStartDate()) && StringUtils.isEmpty(request.getEndDate())
					&& StringUtils.isEmpty(request.getTicketStatus())
					&& StringUtils.isEmpty(request.getSupervisorId())) {
				
				
				ticketList = ticketdetails
						.findByGlobalStatusInOrGlobalStatusAndCompletionDateTimeGreaterThan(
								new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
										DispatchControllerConstants.STATUS_UNASSIGNED,
										DispatchControllerConstants.STATUS_MISSING_INFO,
										DispatchControllerConstants.STATUS_RESCHEDULE,
										DispatchControllerConstants.STATUS_CANCELLED},
								DispatchControllerConstants.STATUS_COMPLETE, ldt_start, processPage);
				
			} else if (StringUtils.isEmpty(request.getStartDate()) && StringUtils.isEmpty(request.getEndDate())
					&& StringUtils.isEmpty(request.getTicketStatus())
					&& !StringUtils.isEmpty(request.getSupervisorId())) {
			
				ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
				
				ticketList = ticketdetails
						.findByGlobalStatusInOrGlobalStatusAndCompletionDateTimeGreaterThanAndSuperviorId(
								new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
										DispatchControllerConstants.STATUS_UNASSIGNED,
										DispatchControllerConstants.STATUS_MISSING_INFO,
										DispatchControllerConstants.STATUS_RESCHEDULE,
										DispatchControllerConstants.STATUS_CANCELLED},
								DispatchControllerConstants.STATUS_COMPLETE, ldt_start,request.getSupervisorId(), processPage);

			} else if (StringUtils.isEmpty(request.getStartDate()) && StringUtils.isEmpty(request.getEndDate())
					&& !StringUtils.isEmpty(request.getTicketStatus())
					&& StringUtils.isEmpty(request.getSupervisorId())) {
				
				if (request.getTicketStatus().equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketList = ticketdetails.findByGlobalStatusInAndIsAssistTicket(
							new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
									DispatchControllerConstants.STATUS_RESCHEDULE },
							DispatchControllerConstants.NO, processPage);
				} else if (StringUtils.equalsIgnoreCase(request.getTicketStatus(),
						DispatchControllerConstants.STATUS_COMPLETE)) {
					 ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

					ticketList = ticketdetails.findByGlobalStatusAndIsAssistTicketAndCompletionDateTimeGreaterThan(
							request.getTicketStatus(), DispatchControllerConstants.NO, ldt_start, processPage);
				} else {
					ticketList = ticketdetails.findByGlobalStatusAndIsAssistTicket(request.getTicketStatus(),
							DispatchControllerConstants.NO, processPage);
				}
			} else if (!StringUtils.isEmpty(request.getStartDate()) && !StringUtils.isEmpty(request.getEndDate())
					&& StringUtils.isEmpty(request.getTicketStatus())
					&& StringUtils.isEmpty(request.getSupervisorId())) {
				
				startDate = LocalDateTime.parse(request.getStartDate(), formatter);
				endDate = LocalDateTime.parse(request.getEndDate(), formatter);
				ticketList = ticketdetails.findBy(new String[] { DispatchControllerConstants.STATUS_PASTDUE },
						startDate, endDate, processPage);
				// ticketList = ticketdetails.findByDueDateBetween(startDate,endDate,
				// processPage);
			} else if (StringUtils.isEmpty(request.getStartDate()) && StringUtils.isEmpty(request.getEndDate())
					&& !StringUtils.isEmpty(request.getTicketStatus())
					&& !StringUtils.isEmpty(request.getSupervisorId())) {
				if (request.getTicketStatus().equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketList = ticketdetails.findByGlobalStatusInAndSupervisorIdAndIsAssistTicket(
							new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
									DispatchControllerConstants.STATUS_RESCHEDULE },
							request.getSupervisorId(), DispatchControllerConstants.NO, processPage);
				} else {
					ticketList = ticketdetails.findByGlobalStatusAndSupervisorIdAndIsAssistTicket(
							request.getTicketStatus(), request.getSupervisorId(), DispatchControllerConstants.NO,
							processPage);
				}
			} else if (!StringUtils.isEmpty(request.getStartDate()) && !StringUtils.isEmpty(request.getEndDate())
					&& !StringUtils.isEmpty(request.getTicketStatus())
					&& StringUtils.isEmpty(request.getSupervisorId())) {
				
				startDate = LocalDateTime.parse(request.getStartDate(), formatter);
				endDate = LocalDateTime.parse(request.getEndDate(), formatter);
				if (request.getTicketStatus().equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketList = ticketdetails
							.findByGlobalStatusInAndDueDateBetween(
									new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
											DispatchControllerConstants.STATUS_RESCHEDULE },
									startDate, endDate, processPage);
				} else {
					ticketList = ticketdetails.findByGlobalStatusAndDueDateBetween(request.getTicketStatus(), startDate,
							endDate, processPage);
				}

			}

			else if (!StringUtils.isEmpty(request.getStartDate()) && !StringUtils.isEmpty(request.getEndDate())
					&& StringUtils.isEmpty(request.getTicketStatus())
					&& !StringUtils.isEmpty(request.getSupervisorId())) {
				log.info("startdate and enddate not empty and sup not empty,ticketstaus empty");
				startDate = LocalDateTime.parse(request.getStartDate(), formatter);
				endDate = LocalDateTime.parse(request.getEndDate(), formatter);
				ticketList = ticketdetails
						.findByGlobalStatusInOrGlobalStatusWithQuery(
								new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
										DispatchControllerConstants.STATUS_UNASSIGNED,
										DispatchControllerConstants.STATUS_MISSING_INFO,
										DispatchControllerConstants.STATUS_RESCHEDULE,
										DispatchControllerConstants.STATUS_CANCELLED},
								DispatchControllerConstants.STATUS_COMPLETE, ldt_start, startDate, endDate,request.getSupervisorId(), processPage);
			}

			response.setTotalPages(ticketList.getTotalPages());
			response.setTotalElements(ticketList.getTotalElements());
			response.setResponseData(ticketList.getContent());
			response.setResponseText(HttpStatus.OK.getReasonPhrase());
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			log.info("Unable to find the data while getting getTicketDetailsBySupervisorId:{}", e.getMessage());
			response.setResponseText(DispatchControllerConstants.STATUS_FAILED);
			response.setResponseData(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	public ResponseEntity<ApiResponseDto> generateTechnicianHierarchy() {

		String logKey = DispatchControllerConstants.GET_USERHIERARCHY_REQUEST;

		ApiResponseDto apiResponseDto = new ApiResponseDto();
		String businessId = BusinessContext.getTenantId();
		String businessToken = BusinessTokenContext.getBusinessToken();
		try {
			// Executor executor = Executors.newSingleThreadExecutor();
			// executor.execute(()->{
			// BusinessContext.setTenantId(businessId);
			// BusinessTokenContext.setBusinessToken(businessToken);
			// createUserHierachy(businessId,businessToken);
			// });
			createUserHierachy(businessId, businessToken);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_SUBMITTED);
			apiResponseDto.setResponseData(DispatchControllerConstants.RESPONSE_SUBMITTED);

			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		} catch (Exception e) {
			log.info("{} Unable to create Technician Hierarchy {} , for businessId : {} due to {}", logKey,
					BusinessContext.getTenantId(), e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		}

	}

	private void createUserHierachy(String businessId, String businessToken) {
		BusinessContext.setTenantId(businessId);
		BusinessTokenContext.setBusinessToken(businessToken);
		try {
			UserHierarchyModel userHierarchyModel = new UserHierarchyModel();
			List<SupervisorPolygonMapping> supervisorList = supervisorRepo
					.findByIsActiveOrderByFirstNameAscLastNameAsc(DispatchControllerConstants.FLAG_Y);
			if (supervisorList != null && !supervisorList.isEmpty()) {
				List<UserHierarchySupervisorDTO> userHierarchySupervisorDTOList = new ArrayList<>();

				for (SupervisorPolygonMapping supervisor : supervisorList) {
					List<TechnicianDTO> technicianDtoList = new ArrayList<>();
					List<Agent> techniciansList = technicianDataRepo
							.findByIsActiveAndAgentTypeAndSupervisorIdOrderByFirstNameAscLastNameAsc(
									DispatchControllerConstants.FLAG_Y, DispatchControllerConstants.AGENT_TYPE_ACTUAL,
									supervisor.getSupervisorId());

					if (techniciansList != null && !techniciansList.isEmpty()) {
						for (Agent technician : techniciansList) {
							DataDTO dataDto = new DataDTO();
							dataDto.setId(technician.getTechnicianId());
							dataDto.setDesignation("Technician");
							dataDto.setEmailId(technician.getEmailId());
							// using ternary operator for technician name
							technicianDtoList
									.add(new TechnicianDTO(
											technician.getLastName().isEmpty() ? technician.getFirstName()
													: technician.getFirstName() + " " + technician.getLastName(),
											dataDto));
						}
					}

					DataDTO dataDto = new DataDTO();
					dataDto.setId(supervisor.getSupervisorId());
					dataDto.setDesignation("Supervisor");
					dataDto.setEmailId(supervisor.getEmailId());
					// sorting with technician Name in ascending order
//					technicianDtoList.sort(Comparator.comparing(TechnicianDTO::getLabel));
					UserHierarchySupervisorDTO userHierarchySupervisorDTO = new UserHierarchySupervisorDTO(
							supervisor.getLastName().isEmpty() ? supervisor.getFirstName()
									: supervisor.getFirstName() + " " + supervisor.getLastName(),
							dataDto, technicianDtoList);
					userHierarchySupervisorDTOList.add(userHierarchySupervisorDTO);
				}
				// sorting with supervisor Name in ascending order
//				userHierarchySupervisorDTOList.sort(Comparator.comparing(UserHierarchySupervisorDTO::getLabel));
				userHierarchyModel.getUserHierarchy().addAll(userHierarchySupervisorDTOList);
				userHierarchyRepo.deleteAll();
				userHierarchyRepo.save(userHierarchyModel);
			}
		} catch (Exception e) {
			log.info("Unable to create User Hierachy" + e.getMessage());
		}
	}

	@Override
	public ResponseEntity<ApiResponseDto> getUserHierarchy() {

		ApiResponseDto apiResponseDto = new ApiResponseDto();

		try {

			List<UserHierarchyModel> userHierarchyModelList = userHierarchyRepo.findAll();
			if (CollectionUtils.isEmpty(userHierarchyModelList)) {
				log.info(" User Hierarchy Not Found for request ");
			}
			UserHierarchyModel userHierarchyModel = userHierarchyModelList.get(0);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_OK);
			apiResponseDto.setResponseData(userHierarchyModel.getUserHierarchy());

			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		} catch (Exception e) {
			log.info(" Unable to create Technician Hierarchy " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	public Ticket updateTicketdetails(Ticket ticketDetails, Agent agentDetails, TicketActionTrail ticketActionTrail,
			String globalStatus, String actionStatus) {

		// Updating TicketDCSolver
		ticketDetails.getTicketActionTrails().add(ticketActionTrail);
		ticketDetails.setGlobalStatus(globalStatus);
		ticketDetails.setActionOnTicket(actionStatus);
		ticketDetails.setTechnicianId(agentDetails.getTechnicianId());
		ticketDetails.setTechnicianFirstName(agentDetails.getFirstName());
		ticketDetails.setTechnicianLastName(agentDetails.getLastName());
		ticketDetails.setTechnicianEmailId(agentDetails.getEmailId());
		ticketDetails.setSupervisorId(agentDetails.getSupervisorId());
		ticketDetails.setSupervisorName(agentDetails.getSupervisorName());
		ticketDetails.setTransferCount(ticketDetails.getTransferCount() + 1);
		ticketDetails.setSupervisorPolygonId(agentDetails.getSupervisorId());

		if (StringUtils.equalsIgnoreCase(actionStatus, DispatchControllerConstants.STATUS_TRANSFERED)) {
			ticketDetails.setAssignmentDateTime(LocalDateTime.now());
		}
		ticketdetails.save(ticketDetails);
		if (!StringUtils.equalsIgnoreCase(ticketDetails.getIsAssistTicket(), DispatchControllerConstants.YES)) {
			// Updating master ticket collection
			LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();

			lumenCollectionUpdateDTO.setGlobalStatus(globalStatus);
			lumenCollectionUpdateDTO.setActionOnTicket(actionStatus);
			lumenCollectionUpdateDTO.setTechnicianId(agentDetails.getTechnicianId());
			lumenCollectionUpdateDTO.setTechnicianFirstName(agentDetails.getFirstName());
			lumenCollectionUpdateDTO.setTechnicianLastName(agentDetails.getLastName());
			lumenCollectionUpdateDTO.setTechnicianEmailId(agentDetails.getEmailId());
			lumenCollectionUpdateDTO.setSupervisorName(agentDetails.getSupervisorName());
			lumenCollectionUpdateDTO.setSupervisorId(agentDetails.getSupervisorId());
			lumenCollectionUpdateDTO.setAssignmentDateTime(LocalDateTime.now());
			lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
			lumenCollectionUpdateDTO.setTicketNumber(ticketDetails.getTicketNumber());
			lumenCollectionUpdateDTO.setConversationId(ticketDetails.getConversationId());
			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_TRANSFERRED
					+ agentDetails.getSupervisorId() + " : " + agentDetails.getSupervisorName());

			ticketRepository.updateLumenTicketCollection(lumenCollectionUpdateDTO);
			String businessId = BusinessContext.getTenantId();
			String businessToken = BusinessTokenContext.getBusinessToken();
			try {
				executor.execute(() -> {
					BusinessContext.setTenantId(businessId);
					BusinessTokenContext.setBusinessToken(businessToken);
					dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO, businessId, businessToken);
				});
			} catch (Exception e) {
				log.info("Unable to save Audit due to {} ", e.getMessage());
			}
		}
		log.info(" updateTicketdetails 1 {} ", ticketDetails);
		return ticketDetails;
	}

	public TicketActionTrail ticketActionTrail(String action, String actionBy, String preAction, String postAction) {
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

	public ResponseEntity<ApiResponseDto> UpdatetoTechnicianTransferTicketDetails(String action, String actionBy,
			String fromTechnicianId, String toTechnicianId, Ticket ticketToTransfer, String ticketNumber)

	{
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.FILE_DATETIME_FORMAT);
		ApiResponseDto response = new ApiResponseDto();
		String businessId = BusinessContext.getTenantId();
		String businessToken = BusinessTokenContext.getBusinessToken();
		// Find the toTechnicianId based on currentDate and toTechnicianId from
		// technicianAssignmentSolution
		try {
			LocalDate currentDate = LocalDate.now();
			LocalDateTime startOfDay = currentDate.atStartOfDay();
			LocalDateTime endOfDay = currentDate.atTime(LocalTime.MAX);
			String globalStatus = DispatchControllerConstants.STATUS_ASSIGNED;
			String actionStatus = DispatchControllerConstants.STATUS_TRANSFERED;

			String currentTicketStatus = ticketToTransfer.getGlobalStatus();

			Sort sort = Sort.by(Sort.Order.desc(DispatchControllerConstants.TIMESTAMP));

			AgentAssignmentSolutionModel toTechnicianAssignment = assignmentRepository
					.findByAgentTechnicianIdAndTimestampBetween(toTechnicianId, startOfDay, endOfDay, sort).findFirst()
					.orElse(null);

			log.info(" currentDate {} startOfDay {} endOfDay {} ", currentDate, startOfDay, endOfDay);
			log.info(" toTechnicianAssignment {} ", toTechnicianAssignment);

			// Handle case when the toTechnicianId is not found form the given
			// technicianAssignmentSolution Document , find it from technicianDCSolver
			// Document
			if (toTechnicianAssignment == null) {
				toTechnicianAssignment = new AgentAssignmentSolutionModel();
				log.info(
						"Unable to find the data of ToTechnicianId {} in technicianAssignmentSolution Document ,now fetching it from technicianDCSolver Document",
						fromTechnicianId);
				Agent toTechnician = technicianDataRepo.findByTechnicianId(toTechnicianId);
				log.info(" ToTechnicianId 1 {} ", toTechnician);
				// Handle case when the toTechnicianId is not found in TechnicanDCSolver
				// Document-return Bad Request
				if (toTechnician == null || StringUtils.isEmpty(toTechnician.getSupervisorId())
						|| StringUtils.isEmpty(toTechnician.getTechnicianId())) {

					log.info("Unable to find the data ToTechnicianId in {} technicianDCSolver Document",
							fromTechnicianId);
					response.setStatus(DispatchControllerConstants.STATUS_FAILED);
					response.setMessage(DispatchControllerConstants.STATUS_FAILED);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}

				// Find dcSolverProcessId and dcSolverTaskId
				LocalDateTime latestDate = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
				LocalDateTime startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

				List<DCSolverProcessAuditModel> dcSolverProcessId = dcSolverProcessAuditRepo
						.findByTimestampBetweenOrderByDcSolverProcessIdDesc(startDate, latestDate);
				log.info(" dcSolverProcessAuditId 1 : {} ", dcSolverProcessId);
				DCSolverProcessAuditModel latestDocument = null;
				if (dcSolverProcessId != null && !dcSolverProcessId.isEmpty()) {

					latestDocument = dcSolverProcessId.get(0);
					toTechnicianAssignment.setDcSolverProcessId(latestDocument.getDcSolverProcessId());
					String supervisorId = toTechnician.getSupervisorId();
					DCSolverTaskAuditModel dcSolverTaskId = dcSolverTaskAuditRepo
							.findByDcSolverProcessIdAndSupervisorId(latestDocument.getDcSolverProcessId(),
									supervisorId);
					if (ObjectUtils.isEmpty(dcSolverTaskId)) {
						toTechnicianAssignment.setDcSolverTaskId(Long.parseLong(LocalDateTime.now().format(dtf)));
					} else {
						toTechnicianAssignment.setDcSolverTaskId(dcSolverTaskId.getDcSolverTaskId());
					}

				} else {
					toTechnicianAssignment.setDcSolverTaskId(Long.parseLong(LocalDateTime.now().format(dtf)));
					toTechnicianAssignment.setDcSolverProcessId(Long.parseLong(LocalDateTime.now().format(dtf)));
				}

				// Set the required fields toTechnicianId if not found in TechnicanDCSolver
				// Document

				toTechnicianAssignment.setTimestamp(LocalDateTime.now());
				toTechnicianAssignment.setAgent(toTechnician);
				log.info(" Creating new Document 1 {} ", toTechnicianAssignment);

				TicketActionTrail ticketActionTrail = ticketActionTrail(action, actionBy, currentTicketStatus,
						toTechnicianId + ":" + toTechnician.getFirstName() + " " + toTechnician.getLastName());
				log.info(" updated the details of ticketActionTrail  {} ", ticketActionTrail);
				// Update the required fields of ticket in TechnicanDCSolver and ticketDCSolver
				// and TechnicianAssignmentSolution Document
				updateTicketdetails(ticketToTransfer, toTechnician, ticketActionTrail, globalStatus, actionStatus);

				log.info(" ticketToTransfer 1 {} ", ticketToTransfer);
				log.info(" toTechnician 1A {} ", toTechnician);
				// log.info(" fromTechnicianAssignment 1 {} ", fromTechnicianAssignment);

				// Add the ticket to the toTechnician
				List<Ticket> toTechnicianTicketList = toTechnicianAssignment.getAgent().getTicketList();
				toTechnicianTicketList.add(ticketToTransfer);

				log.info(" totechnicianLocation {} ", toTechnicianAssignment.getAgent().getLocation());
				Location specificTicketLocation = ticketToTransfer.getLocation();

				RouteSolverPath routeSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(
						toTechnicianAssignment.getAgent().getLocation(), specificTicketLocation);
				long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath)
						? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME
						: routeSolverPath.getTime();
				double evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0
						: routeSolverPath.getDistance();
				long toTravelTime = travelTimeLocal / 60000;

				long toWorkHourTime = toTechnicianAssignment.getAgent().getTotalWorkHourGlobal() + toTravelTime
						+ ticketToTransfer.getTicketETA();

				double evaluatedDistance = toTechnicianAssignment.getAgent().getEvaluatedDistance()
						+ evaluatedDistanceLocal;

				if (toWorkHourTime < 0) {
					toWorkHourTime = 0;
				}

				// Set default available time as 600 for newly created agentsolution doc
				toTechnicianAssignment.getAgent()
						.setDefaultAvailableTime(DispatchControllerConstants.DEFAULT_AVAILABLETIME);
				toTechnicianAssignment.getAgent().setAvailableTime(DispatchControllerConstants.DEFAULT_AVAILABLETIME);

				toTechnicianAssignment.getAgent().setTotalWorkHourGlobal(toWorkHourTime);
				toTechnicianAssignment.getAgent().setEvaluatedDistance(evaluatedDistance);

				log.info(" total work Time of toTechnicianAssignment  {} ", toWorkHourTime);
				String toAssignmentStatus = toTechnicianAssignment.getAgent()
						.getAvailableTime() > toTechnicianAssignment.getAgent().getTotalWorkHourGlobal()
								? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED
								: toTechnicianAssignment.getAgent().getAvailableTime() == toTechnicianAssignment
										.getAgent().getTotalWorkHourGlobal()
												? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED
												: DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;

				toTechnicianAssignment.getAgent().setAssignmentStatus(toAssignmentStatus);
				log.info(" AssignmentStatus of toTechnicianAssignment  {} ", toAssignmentStatus);

				// Save the updated documents
				technicianDataRepo.save(toTechnicianAssignment.getAgent());
				// technicianDataRepo.save(fromTechnicianAssignment.getAgent());
				// assignmentRepository.save(fromTechnicianAssignment);
				assignmentRepository.save(toTechnicianAssignment);

				if (ticketToTransfer.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES)) {
					try {
						Agent assignAgent = toTechnicianAssignment.getAgent();
						executor.execute(() -> {
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(businessToken);
							dispatchControllerSupportUtils.notifySupervisorAndTech(assignAgent, ticketToTransfer,
									DispatchControllerConstants.STATUS_ASSIGNED, businessId, businessToken);
						});
					} catch (Exception e) {
						log.info("Unable to notifySupervisorAndTech due to {} ", e.getMessage());
					}

				}

				log.info(" toTechnicianAssignment 1 {} ", toTechnicianAssignment);
				// log.info(" fromTechnicianAssignment 1AA {} ", fromTechnicianAssignment);
				response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
				response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);

				return new ResponseEntity<>(response, HttpStatus.OK);

			}

			TicketActionTrail ticketActionTrail = ticketActionTrail(action, actionBy, currentTicketStatus,
					toTechnicianId + ":" + toTechnicianAssignment.getAgent().getFirstName() + " "
							+ toTechnicianAssignment.getAgent().getLastName());

			log.info(" updated the details of ticketActionTrail 1 {} ", ticketActionTrail);

			updateTicketdetails(ticketToTransfer, toTechnicianAssignment.getAgent(), ticketActionTrail, globalStatus,
					actionStatus);

			// Add the ticket to the toTechnician
			List<Ticket> toTechnicianTicketList = toTechnicianAssignment.getAgent().getTicketList();
			toTechnicianTicketList.add(ticketToTransfer);

			List<Ticket> toTicketList = toTechnicianAssignment.getAgent().getTicketList();

			Location toTicketLocation = ticketToTransfer.getLocation();
			Location toPrecedingTicketLocation = null;

			for (int i = 0; i < toTicketList.size(); i++) {
				Ticket ticket = toTicketList.get(i);
				log.info(" ticket {} ", ticket);
				if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketNumber)) {
					toTicketLocation = ticket.getLocation();
					log.info(" toTicketLocation {} ", toTicketLocation);
					// Check if there is a preceding ticket
					if (i > 0) {
						toPrecedingTicketLocation = toTicketList.get(i - 1).getLocation();
						log.info(" toPrecedingTicketLocation {} ", toPrecedingTicketLocation);
					} else {
						toPrecedingTicketLocation = toTechnicianAssignment.getAgent().getLocation();
						log.info(" At 1 toPrecedingTicketLocation {} ", toPrecedingTicketLocation);
					}
					break;
				}

			}

			// Update TotalWorkHourGlobal and AssignmentStatus of toTechnicianAssignment
			log.info(" totechnicianLocation 1 {} ", toTechnicianAssignment.getAgent().getLocation());

			// long toTravelTime =
			// dispatchControllerSupportUtils.calculateDistance(toPrecedingTicketLocation,
			// toTicketLocation) / 60000;

			RouteSolverPath routeSolverPath = dispatchControllerSupportUtils
					.getLocationDistanceDetails(toPrecedingTicketLocation, toTicketLocation);
			long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath)
					? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME
					: routeSolverPath.getTime();
			double evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
			long toTravelTime = travelTimeLocal / 60000;

			long toWorkHourTime = toTechnicianAssignment.getAgent().getTotalWorkHourGlobal() + toTravelTime
					+ ticketToTransfer.getTicketETA();
			double evaluatedDistance = toTechnicianAssignment.getAgent().getEvaluatedDistance()
					+ evaluatedDistanceLocal;

			if (toWorkHourTime < 0) {
				toWorkHourTime = 0;
			}

			toTechnicianAssignment.getAgent().setTotalWorkHourGlobal(toWorkHourTime);
			toTechnicianAssignment.getAgent().setEvaluatedDistance(evaluatedDistance);

			log.info(" total work Time of fromTechnicianAssignment  {} ", toWorkHourTime);
			String toAssignmentStatus = toTechnicianAssignment.getAgent().getAvailableTime() > toTechnicianAssignment
					.getAgent().getTotalWorkHourGlobal()
							? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED
							: toTechnicianAssignment.getAgent().getAvailableTime() == toTechnicianAssignment.getAgent()
									.getTotalWorkHourGlobal()
											? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED
											: DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;

			toTechnicianAssignment.getAgent().setAssignmentStatus(toAssignmentStatus);

			// log.info(" AssignmentStatus of fromTechnicianAssignment {} ",
			// assignmentStatus);
			// Save the updated documents
			technicianDataRepo.save(toTechnicianAssignment.getAgent());
			// technicianDataRepo.save(fromTechnicianAssignment.getAgent());
			// assignmentRepository.save(fromTechnicianAssignment);
			assignmentRepository.save(toTechnicianAssignment);

			if (ticketToTransfer.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES)) {
				try {
					Agent assignAgent = toTechnicianAssignment.getAgent();
					executor.execute(() -> {
						BusinessContext.setTenantId(businessId);
						BusinessTokenContext.setBusinessToken(businessToken);
						dispatchControllerSupportUtils.notifySupervisorAndTech(assignAgent, ticketToTransfer,
								DispatchControllerConstants.STATUS_ASSIGNED, businessId, businessToken);
					});
				} catch (Exception e) {
					log.info("Unable to notifySupervisorAndTech due to {} ", e.getMessage());
				}

			}

			log.info(" toTechnicianAssignment in solution Document {} ", toTechnicianAssignment);
			log.info(" fromTechnicianAssignment in solution Document  {} ", toTechnicianAssignment);

			response.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
			response.setMessage(DispatchControllerConstants.STATUS_SUCCESS);

			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			log.info(" Unable to find the data while getting getAgentByTechnicianID", e);
			response.setStatus(DispatchControllerConstants.STATUS_FAILED);
			response.setMessage(DispatchControllerConstants.STATUS_FAILED);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public ResponseEntity<ApiResponseDto> getTotalUnAssignedAssignedAndComletedCountBySupervisorId(
			supervisorCountRequest request) {
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		Map<String, Object> response = new HashMap<>();
		response.put(DispatchControllerConstants.STATUS_ASSIGNED, 0);
		response.put(DispatchControllerConstants.STATUS_UNASSIGNED, 0);
		response.put(DispatchControllerConstants.STATUS_COMPLETE, 0);
		try {

			if (StringUtils.isNotEmpty(request.getSupervisorId())) {
				DateTimeFormatter formatter = DateTimeFormatter
						.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
				LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), formatter);
				LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), formatter);

				//Open Count = Assigned Count + ReSchedule Count
				long assignedCount = ticketRepository.getSupervisorCountByStatus(request.getSupervisorId(),
						DispatchControllerConstants.STATUS_ASSIGNED, startDate, endDate);
				long rescheduleCount = ticketRepository.getSupervisorCountByStatus(request.getSupervisorId(),
						DispatchControllerConstants.STATUS_RESCHEDULE, startDate, endDate);
				long openCount = assignedCount + rescheduleCount;
				response.put(DispatchControllerConstants.STATUS_ASSIGNED, openCount);
				
				long unAssignedCount = ticketRepository.getSupervisorCountByStatus(request.getSupervisorId(),
						DispatchControllerConstants.STATUS_UNASSIGNED, startDate, endDate);
				response.put(DispatchControllerConstants.STATUS_UNASSIGNED, unAssignedCount);
				long completedCount = ticketRepository.getSupervisorCountByStatus(request.getSupervisorId(),
						DispatchControllerConstants.STATUS_COMPLETE, startDate, endDate);
				response.put(DispatchControllerConstants.STATUS_COMPLETE, completedCount);

				apiResponseDto.setResponseData(response);
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
			}
			log.info("Unable to find Supervisor Id");
			apiResponseDto.setResponseData(response);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);

		} catch (Exception e) {
			log.info("Unable to get Total Unassigned count Supervisor Id");
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			apiResponseDto.setResponseData(response);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<ApiResponseDto> cockpitBubbleStatCount(CockpitBubbleCountStatRequest request) {

		ApiResponseDto responseData = new ApiResponseDto();

		try {

			Object statusCount = ticketRepository.getCockpitBubbleStatCount(request);

			responseData.setResponseData(statusCount);
			responseData.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			responseData.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			return new ResponseEntity<>(responseData, HttpStatus.OK);

		} catch (Exception e) {
			log.info(" Unable to find the data while getting cockpitBubbleStatCount:{}", e.getMessage());
			responseData.setResponseData(null);
			responseData.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			responseData.setMessage(DispatchControllerConstants.STATUS_FAILED);
			return new ResponseEntity<>(responseData, HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public ResponseEntity<ApiResponseDto> fetchOpenAndCompletedStatusCountById(FetchCountById fetchCountById,
			String businessId) {
		ApiResponseDto responseDto = new ApiResponseDto();
		String supervisorId = fetchCountById.getSupervisorId();
		String technicianId = fetchCountById.getTechnicianId();
		try {

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDate = LocalDateTime.parse(fetchCountById.getStartDate(), formatter);
			LocalDateTime endDate = LocalDateTime.parse(fetchCountById.getEndDate(), formatter);

			if (StringUtils.isEmpty(supervisorId) && StringUtils.isEmpty(technicianId)) {
				log.info("For businessId: {} Request recevied fo SupervisorId and TechnicianId is null ", businessId);
				responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
				responseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
				return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
			}

//	            Fetch technicianIds from technicianDCSolver
			List<String> technicianIds = fetchTechnicianIdsBySupervisorId(supervisorId, businessId);
			log.info("For businessId: {} Fetch technician data by supervisorId {} and technicianId {}", businessId,
					supervisorId, technicianIds);
			if (technicianIds == null) {
				log.info("For businessId: {} Data not Found for SupervisorId {} ", businessId, supervisorId);
				responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
				responseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
				return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
			}

//	            Fetch globalStatus count from ticketDCSolver for each technicianId
			List<TechnicianStatusCount> technicianStatusCounts = getTechnicianStatusCounts(technicianIds, startDate,
					endDate);
			log.info("For businessId: {} Fetch TechnicianStatusCount {} data by technicianId {}", businessId,
					technicianStatusCounts, technicianIds);
			responseDto.setResponseData(technicianStatusCounts);
			responseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			responseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			return new ResponseEntity<>(responseDto, HttpStatus.OK);

		} catch (DateTimeParseException e) {
			log.info("For businessId: {} Invalid date format Data not Found for technicianIds {} ", businessId,
					supervisorId);
			responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
			responseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
			return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			log.info(
					"For businessId: {}  Unable to find the data while getting Open And Completed Status Count By Id {} ",
					businessId, e);
			responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
			responseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
			return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);

		}

	}

	private List<String> fetchTechnicianIdsBySupervisorId(String supervisorId, String businessId) {
		List<String> technicianIds = null;
		log.info("For businessId: {} Fetching technician data by supervisorId {}", businessId, supervisorId);
		try {
			/*
			 * technicianIds = mongoTemplate .aggregate( Aggregation.newAggregation(
			 * Aggregation.match(Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE)
			 * .is(DispatchControllerConstants.FLAG_Y)
			 * .and(DispatchControllerConstants.FIELD_AGENT_TYPE)
			 * .is(DispatchControllerConstants.AGENT_TYPE_ACTUAL)
			 * .and(DispatchControllerConstants.SUPERVISORID).is(supervisorId)
			 * .and(DispatchControllerConstants.FIELD_TICKETLIST_IS_ASSIST_TICKET)
			 * .is(DispatchControllerConstants.NO)),
			 * Aggregation.project(DispatchControllerConstants.TECHNICIANID)),
			 * DispatchControllerConstants.TECHNICIAN_DC_SOLVER_COLLECTION, Agent.class)
			 * .getMappedResults().stream().map(Agent::getTechnicianId).collect(Collectors.
			 * toList());
			 */
			
			technicianIds = mongoTemplate
					.aggregate(
							Aggregation.newAggregation(
									Aggregation.match(Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE)
											.is(DispatchControllerConstants.FLAG_Y)
											.and(DispatchControllerConstants.FIELD_AGENT_TYPE)
											.is(DispatchControllerConstants.AGENT_TYPE_ACTUAL)
											.and(DispatchControllerConstants.SUPERVISORID).is(supervisorId)
											),
									Aggregation.project(DispatchControllerConstants.TECHNICIANID)),
							DispatchControllerConstants.TECHNICIAN_DC_SOLVER_COLLECTION, Agent.class)
					.getMappedResults().stream().map(Agent::getTechnicianId).collect(Collectors.toList());
			
		} catch (EmptyResultDataAccessException e) {
			log.info("For businessId: {} While Fetching technician data by supervisorId {} due to {} ",
					businessId, supervisorId, e);
			technicianIds = Collections.emptyList();
		}

		return technicianIds;
	}

	private List<TechnicianStatusCount> getTechnicianStatusCounts(List<String> technicianIds, LocalDateTime startDate,
			LocalDateTime endDate) {
		log.info("Fetching getTechnicianStatusCounts data by technicianIds {}", technicianIds);
		List<TechnicianStatusCount> technicianStatusCounts = new ArrayList<>();

		for (String technicianId : technicianIds) {
			Criteria criteria = Criteria.where(DispatchControllerConstants.TECHNICIANID).is(technicianId)
					.and(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO);
			if (startDate != null && endDate != null) {
				criteria = criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDate).lte(endDate);
			}

			AggregationResults<Document> results = mongoTemplate
					.aggregate(
							Aggregation.newAggregation(Aggregation.match(criteria),
									Aggregation.group(DispatchControllerConstants.FIELD_GLOBAL_STATUS).count()
											.as("count")),
							DispatchControllerConstants.TICKET_COLLECTION, Document.class);

			Map<String, Integer> statusCountMap = new HashMap<>();
			results.getMappedResults().forEach(doc -> {
				String globalStatus = doc.getString("_id");
				Integer count = doc.getInteger("count");
				;
				statusCountMap.put(globalStatus, count);
			});

			Query query = new Query();

			query.addCriteria(Criteria.where(DispatchControllerConstants.TECHNICIANID).is(technicianId));

			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS)
					.is(DispatchControllerConstants.STATUS_COMPLETE));

			query.addCriteria(
					Criteria.where(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO));

			query.addCriteria(
					Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDate).lte(endDate));

			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_COMPLETION_DATE_TIME)
					.gte(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));

			long count = mongoTemplate.count(query, Ticket.class);

			TechnicianStatusCount statusCountDto = new TechnicianStatusCount();
			statusCountDto.setTechnicianId(technicianId);
			statusCountDto.setOpen(statusCountMap.getOrDefault(DispatchControllerConstants.STATUS_RESCHEDULE, 0)
					+ statusCountMap.getOrDefault(DispatchControllerConstants.STATUS_ASSIGNED, 0));
			statusCountDto.setCompleted((int) count);

			technicianStatusCounts.add(statusCountDto);
		}
		return technicianStatusCounts;
	}

	@Override
	public ResponseEntity<ApiResponseDto> fetchLocationFromAssignmentSolByTechId(TechnicianIdDto technicianIds,
			String businessId) {
		log.info("For businessId: {} Inside fetchLocationFromAssignmentSolByTechId Method ", businessId);

		ApiResponseDto responseDto = new ApiResponseDto();

		try {
			if (technicianIds == null || technicianIds.getTechnicianIds() == null
					|| technicianIds.getStartDate() == null || technicianIds.getEndDate() == null) {
				log.info(
						"For businessId: {} Request Received for fetchLocationFromAssignmentSolByTechId Method is Invalid ",
						businessId);
				responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
				responseDto.setMessage("Invalid request body");
				return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
			}

			List<String> technicianIdList = technicianIds.getTechnicianIds();
			String startDate = technicianIds.getStartDate();
			String endDate = technicianIds.getEndDate();

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);

			Criteria criteria = Criteria.where("agent.technicianId").in(technicianIdList).and("timestamp")
					.gte(startDateTime).lte(endDateTime);
			MatchOperation matchOperation = Aggregation.match(criteria);

			// Add the match operation to the aggregation pipeline
			TypedAggregation<AgentAssignmentSolutionModel> aggregation = Aggregation
					.newAggregation(AgentAssignmentSolutionModel.class, matchOperation);

			// Execute the aggregation query
			List<AgentAssignmentSolutionModel> results = mongoTemplate
					.aggregate(aggregation, AgentAssignmentSolutionModel.class).getMappedResults();

			if (results.isEmpty()) {
				log.info("For businessId: {} No data found for the given technicianIds and date range", businessId);
				responseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
				responseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				return new ResponseEntity<>(responseDto, HttpStatus.OK);
			} else {

				responseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
				responseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				responseDto.setResponseData(results);
				return new ResponseEntity<>(responseDto, HttpStatus.OK);
			}
		} catch (DateTimeParseException e) {
			log.info("For businessId: {} Invalid date format for fetchLocationFromAssignmentSolByTechId Method ",
					businessId);
			responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
			responseDto.setMessage("Invalid date format");
			return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			log.info("For businessId: {} Unable to process the request for fetchLocationFromAssignmentSolByTechId {} ",
					businessId, e);
			responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
			responseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
			return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResponseEntity<CountResponseDTO> getAssistTicketByMasterExternalId(
			TicketDetailsByMasterExternalIdRequest request) {
		CountResponseDTO response = new CountResponseDTO();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {
			Page<Ticket> ticketList = null;
			Integer pageSize = (request.getPageSize() != null && request.getPageSize() > 0) ? request.getPageSize()
					: DispatchControllerConstants.DEFAULT_PAGE_SIZE;
			Integer pageNumber = (request.getPageNo() != null && request.getPageNo() > 0) ? (request.getPageNo() - 1)
					: DispatchControllerConstants.DEFAULT_PAGE_NUMBER;

			Pageable processPage = PageRequest.of(pageNumber, pageSize,
					Sort.by(DispatchControllerConstants.TICKETDUEDATEANDTIME).descending());
			LocalDateTime startDate;
			LocalDateTime endDate;

			if (!StringUtils.isEmpty(request.getMasterTicketExternalId())) {
				ticketList = ticketdetails.findByMasterTicketExternalIdAndIsAssistTicket(
						request.getMasterTicketExternalId(), DispatchControllerConstants.YES, processPage);
			} else {
				ticketList = null;
			}

			response.setTotalPages(ticketList.getTotalPages());
			response.setTotalElements(ticketList.getTotalElements());
			response.setResponseData(ticketList.getContent());
			response.setResponseText(HttpStatus.OK.getReasonPhrase());
			return new ResponseEntity<>(response, HttpStatus.OK);

		} catch (Exception e) {
			log.info("Unable to find the data while getting getTicketDetailsByMasterExternalIdId and IsAssist:yes:{}",
					e.getMessage());
			response.setResponseText(DispatchControllerConstants.STATUS_FAILED);
			response.setResponseData(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

	}

	@Override
	public ResponseEntity<CountResponseDTO> getTicketDetialsByglobalStatusForReports(
			TechnicianDetailsDTO technicianDetailsDTO) {
		String businessId = BusinessContext.getTenantId();
		CountResponseDTO responseDTO = new CountResponseDTO();
		String globalStatus = technicianDetailsDTO.getGlobalStatus();
		int pageSize = technicianDetailsDTO.getSize();
		int pageNo = 0;

		log.info(" pageNo : {} ", pageNo);

		try {
			Page<Ticket> ticketPage = null;
			if (technicianDetailsDTO.getPageNo() > 0) {
				pageNo = technicianDetailsDTO.getPageNo() - 1;
			}

			log.info("pageNo : {} ", pageNo);

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDate;
			LocalDateTime endDate;

			Pageable pageable;
			pageable = PageRequest.of(pageNo, pageSize).withSort(Direction.DESC,
					DispatchControllerConstants.TICKETDUEDATEANDTIME);

			if (StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && StringUtils.isEmpty(globalStatus)) {
				ticketPage = ticketdetails.findByGlobalStatusNot(DispatchControllerConstants.STATUS_PASTDUE, pageable);
			} else if (StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && !StringUtils.isEmpty(globalStatus)) {
				if (globalStatus.equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketPage = ticketdetails
							.findByGlobalStatusIn(new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
									DispatchControllerConstants.STATUS_RESCHEDULE }, pageable);
				} else {
					ticketPage = ticketdetails.findByGlobalStatus(globalStatus, pageable);
				}
			} else if (!StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& !StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && StringUtils.isEmpty(globalStatus)) {
				startDate = LocalDateTime.parse(technicianDetailsDTO.getStartDate(), formatter);
				endDate = LocalDateTime.parse(technicianDetailsDTO.getEndDate(), formatter);
				// PastDue not in
				ticketPage = ticketdetails.findByCreatedDateTimeBetweenAndGlobalStatus(
						new String[] { DispatchControllerConstants.STATUS_PASTDUE }, startDate, endDate, pageable);
			} else if (!StringUtils.isEmpty(technicianDetailsDTO.getStartDate())
					&& !StringUtils.isEmpty(technicianDetailsDTO.getEndDate()) && !StringUtils.isEmpty(globalStatus)) {
				startDate = LocalDateTime.parse(technicianDetailsDTO.getStartDate(), formatter);
				endDate = LocalDateTime.parse(technicianDetailsDTO.getEndDate(), formatter);

				if (globalStatus.equals(DispatchControllerConstants.STATUS_ASSIGNED)) {
					ticketPage = ticketdetails
							.findByGlobalStatusInAndcreatedDateTime(
									new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
											DispatchControllerConstants.STATUS_RESCHEDULE },
									startDate, endDate, pageable);
				} else {
					ticketPage = ticketdetails.findByGlobalStatusAndcreatedDateTime(globalStatus, startDate, endDate,
							pageable);
				}

			}

			log.info("tickets : {} ", ticketPage);

			log.info("ticketPageByGlobalStatusByDateRange ForReports : {} ", ticketPage);
			responseDTO.setResponseText("OK");
			responseDTO.setResponseData(ticketPage.getContent());
			responseDTO.setTotalPages(ticketPage.getTotalPages());
			responseDTO.setTotalElements(ticketPage.getTotalElements());
			log.info("ticketPageByGlobalStatusByDateRange request completed with statusCode ForReports: {}",
					HttpStatus.OK);
			return new ResponseEntity<>(responseDTO, HttpStatus.OK);
		} catch (Exception e) {
			// Handle exceptions
			log.info(businessId
					+ " Unable to find the data while fetching ticketPageByGlobalStatusByDateRange ForReports:{}",
					e.getMessage());
			return new ResponseEntity<>(new CountResponseDTO("Unable to find the data", null, null, null),
					HttpStatus.EXPECTATION_FAILED);
		}
	}
@Override
public ResponseEntity<ResponsePageDto> ticketDCSolverGlobalSearchDTO(TicketDCSolverGlobalSearchDTO globalSearchDTO) {
	ResponsePageDto response = new ResponsePageDto();
	String businessId=BusinessContext.getTenantId();
	String globalStatus = globalSearchDTO.getGlobalStatus();
	String supervisorId = globalSearchDTO.getSupervisorId();
	
	
	
	try {

		if (globalSearchDTO == null || StringUtils.isAllEmpty(globalSearchDTO.getStartDate())
				|| StringUtils.isAllEmpty(globalSearchDTO.getEndDate())) {
			log.info(" Bad Request received for globalSearchDTOOnTicketDCSolver where request is {} , for businessId : {}",globalSearchDTO, BusinessContext.getTenantId());
			response.setStatusCode( DispatchControllerConstants.STATUS_BAD_REQUEST);
			response.setResponseData(new ArrayList<>());
			response.setStatusMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		
		int pageSize = globalSearchDTO.getPageSize() < 1 ? DispatchControllerConstants.DEFAULT_PAGE_SIZE:globalSearchDTO.getPageSize();
		int pageNo = 0;
		Page<Ticket> ticketPage = null;
		if (globalSearchDTO.getPageNo() > 0) {
			pageNo = globalSearchDTO.getPageNo() - 1;
		}

		Pageable pageable;
		pageable = PageRequest.of(pageNo, pageSize).withSort(Direction.DESC,DispatchControllerConstants.TICKETDUEDATEANDTIME);

		String startDate = globalSearchDTO.getStartDate();
		String endDate = globalSearchDTO.getEndDate();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
		LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);
           
		ticketPage=ticketRepository.globalSearch(supervisorId, globalStatus, globalSearchDTO.getSearchText(), startDateTime, endDateTime, pageable);
        
		response.setResponseData(ticketPage.getContent());
		response.setTotalElements(ticketPage.getTotalElements());
		response.setTotalPages(ticketPage.getTotalPages());
		response.setStatusCode(DispatchControllerConstants.STATUS_OK);
		response.setStatusMessage(DispatchControllerConstants.STATUS_SUCCESS);

		return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (DateTimeParseException e) {
    	log.info("For businessId: {} Invalid date format for fetchLocationFromAssignmentSolByTechId Method ",businessId);
   	response.setStatusCode( DispatchControllerConstants.STATUS_BAD_REQUEST);
	response.setResponseData(new ArrayList<>());
	response.setStatusMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
   	
       return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}catch(Exception e) {
		log.info("Unable to find the data while getting advanceSearch:{}", e.getMessage());
		response.setStatusCode( DispatchControllerConstants.STATUS_CODE_INTERNAL_ERROR);
		response.setResponseData(new ArrayList<>());
		response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}	

}

@Override
public ResponseEntity<CountResponseDTO> getAllSupervisorDetails(TechnicianDetailsDTO technicianDetailsDTO) {
	String businessId = BusinessContext.getTenantId();
	CountResponseDTO responseDTO = new CountResponseDTO();
	int pageNo = 0;
	int pageSize=0;
	try {
		
		if (technicianDetailsDTO.getPageNo() > 0) {
			pageNo = technicianDetailsDTO.getPageNo() - 1;
		}
		log.info("pageNo : {} ", pageNo);
		
		Pageable pageable = PageRequest.of(pageNo, technicianDetailsDTO.getSize());
		
		AgentListResponse pageOfAgent = null;

			 pageOfAgent = agentRepository.findAllSupervisorData(pageable,technicianDetailsDTO.getIsActive());
			log.info("pageOfAgent : {} ", pageOfAgent);
		
		
		
		if(pageOfAgent.getCount() %technicianDetailsDTO.getSize() !=0) {
			pageSize = (pageOfAgent.getCount() /technicianDetailsDTO.getSize()) + 1;
		}else {
			pageSize = (pageOfAgent.getCount() /technicianDetailsDTO.getSize());
		}
		
		responseDTO.setTotalPages(pageSize);
		responseDTO.setTotalElements(Long.valueOf(pageOfAgent.getCount()));
		responseDTO.setResponseData(pageOfAgent.getResponseList());
		responseDTO.setResponseText(HttpStatus.OK.getReasonPhrase());

		log.info("getAllTechnicianDetails request completed with statusCode: {}", HttpStatus.OK);
		return new ResponseEntity<>(responseDTO, HttpStatus.OK);
	} catch (Exception e) {
		log.info(businessId + " Unable to find the data while getting getAllTechnicianDetails due to : {}", e.getMessage());
		return new ResponseEntity<>(new CountResponseDTO("Unable to find the data", null, null, null),
				HttpStatus.EXPECTATION_FAILED);
	}
}


         // Implement logic to fetch all supervisors using SupervisorRepository or any other relevant method
     //   List<SupervisorPolygonMapping> supervisors =SupervisorRepository.findAll(); // Example
         
         // Create a CountResponseDTO and set the count of supervisors
      //CountResponseDTO responseDTO = new CountResponseDTO();//
      // responseDTO.setCount(supervisors.size()); 

         // Return the response with the count of supervisors
     //    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
   //  } catch (Exception e) {
       // e.printStackTrace(); // Log the exception or handle it appropriately
       //  return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		 
  

@Override
     public List<SupervisorPolygonMapping> fetchAllSupervisors(SupervisorPolygonMapping supervisorPolygonMapping)  {
        
           try {
        	   
       	 List<SupervisorPolygonMapping> supervisorList = supervisorRepo.findAll(supervisorPolygonMapping);

             log.info("fetchAllSupervisors request completed with statusCode: {}", HttpStatus.OK); 
     	  return supervisorList;
         } catch (Exception ex) {
             log.error("Error occurred while fetching supervisors: {}", ex.getMessage());
            return null;
         }
     }
 
}




////@Override
//public List<SupervisorPolygonMapping> fetchAllSupervisors() {
//    return supervisorRepo.findAll();
//}
//}//


    
	


 
		
		
	



