package com.straviso.ns.dispatchcontrollercore.controller;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import java.time.*;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.AgentAndSupervisorRequestDTO;
import com.straviso.ns.dispatchcontrollercore.dto.CalenderViewDto;
import com.straviso.ns.dispatchcontrollercore.dto.CallTechnicianDto;
import com.straviso.ns.dispatchcontrollercore.dto.GetTicketTrailsCollectionDTO;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketCountDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketTrailsCollectionDTO;
import com.straviso.ns.dispatchcontrollercore.dto.UpdateTicketNumbersDto;
import com.straviso.ns.dispatchcontrollercore.dto.firstTicketNumberDto;
import com.straviso.ns.dispatchcontrollercore.dto.request.ExternalTicketRequestDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.FetchCountById;
import com.straviso.ns.dispatchcontrollercore.dto.request.MyTeamWorkloadRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechIdDto;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechnicianAvailabilityDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechnicianAvailabilityRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ExternalAppResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponsePageDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianIdDto;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.entity.TechnicianAvailability;
import com.straviso.ns.dispatchcontrollercore.entity.TechnicianWorkHour;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianWorkHourRepository;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.service.ExternalService;
import com.straviso.ns.dispatchcontrollercore.serviceImpl.SFTPFileService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@RestController
@CrossOrigin(origins = "*")
@Log4j2
@RequestMapping("/external")
public class DispatchControllerExternal {
	
	@Autowired
	private ExternalService externalService;
	
	@Autowired
	TicketRepository ticketRepo;
	
    @Autowired
    private SFTPFileService sftpFileService;
	
	@Autowired
    private TechnicianWorkHourRepository technicianWorkHourRepository;
	
	@GetMapping("/testing")
	public Object testing() {
		
		log.info("Dispatch Controller Core External Test API Request Received for business: {}",BusinessContext.getTenantId());
		return "Dispatch Controller Core is Up & Running!!!";
	}
	

	private double getObjectSize(Object input, String logKey) {
		try {
		Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
		 String inputString = gson.toJson(input);
		return input != null && !inputString.trim().isEmpty() ? (double) inputString.getBytes().length / 1000
				: 0;
		}catch (Exception e) {
			log.info("{} Unable to find size due to {} " ,logKey,e.getMessage());	
			return 0;
		}
	}
	    
	@ApiOperation(value = "This API is used to get ticket details from Master collection")
	@PostMapping("/sendTicketDetailsToDC")
	public ResponseEntity<ResponseDTO> sendTicketDetailsToDC(@RequestBody ExternalTicketRequestDTO request,
			HttpServletRequest servletRequest) {
		LocalDateTime startTime=LocalDateTime.now();
		String logKey="sendTicketDetailsToDC API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.SEND_TICKET_DETAILS_TO_DC,BusinessContext.getTenantId(),request);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , sendTicketDetailsToDC API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = externalService.sendTicketDetailsToDC(request);
		
		log.info("T-ID : {} , ****#### sendTicketDetailsToDC API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , sendTicketDetailsToDC API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("{} response is {} for business : {} Completed in {} ",DispatchControllerConstants.SEND_TICKET_DETAILS_TO_DC,response,BusinessContext.getTenantId(),timeDiff);

		return response;
	}
	
	@ApiOperation(value = "This API is used to get technician details from Connector which in turn gets the details from external source")
	@PostMapping("/addTechnicianDetailsToDC")
	public ResponseEntity<ResponseDTO> addTechnicianDetailsToDC(@RequestBody Agent requestAgent,
			HttpServletRequest servletRequest) {
		LocalDateTime startTime=LocalDateTime.now();
		String logKey="addTechnicianDetailsToDC API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.ADD_TECHNICIAN_DETAILS_TO_DC,BusinessContext.getTenantId(),requestAgent);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , addTechnicianDetailsToDC API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), requestAgent,
				getObjectSize(requestAgent,logKey));
		
		ResponseEntity<ResponseDTO> response = externalService.addTechnicianDetailsToDC(requestAgent);

		log.info("T-ID : {} , ****#### addTechnicianDetailsToDC API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , addTechnicianDetailsToDC API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("{} response is {} for business : {}  Completed in {}",DispatchControllerConstants.ADD_TECHNICIAN_DETAILS_TO_DC,response,BusinessContext.getTenantId(),timeDiff);

		return response;
	}
	
	@ApiOperation(value = "This API is used to get Supervisor details from Connector which in turn gets the details from external source")
	@PostMapping("/addSupervisorDetailsToDC")
	public ResponseEntity<ResponseDTO> addSupervisorDetailsToDC(@RequestBody SupervisorPolygonMapping request,
			HttpServletRequest servletRequest) {
		LocalDateTime startTime=LocalDateTime.now();
		String logKey="addSupervisorDetailsToDC API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.ADD_SUPERVISOR_DETAILS_TO_DC,BusinessContext.getTenantId(),request);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , addSupervisorDetailsToDC API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = externalService.addSupervisorDetailsToDC(request);
		
		log.info("T-ID : {} , ****#### addSupervisorDetailsToDC API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , addSupervisorDetailsToDC API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("{} response is {} for business : {}  Completed in {}",DispatchControllerConstants.ADD_SUPERVISOR_DETAILS_TO_DC,response,BusinessContext.getTenantId(),timeDiff);

		return response;
	}
	
	@ApiOperation(value = "Get total Unassigned tickets count")
    @GetMapping(value = "/getUnassignedTicketCounts")
    public ResponseEntity<ApiResponseDto> getUnassignedTicketCounts(HttpServletRequest servletRequest){
		LocalDateTime startTime=LocalDateTime.now();
        String businessId = BusinessContext.getTenantId();
        String logKey="getUnassignedTicketCounts API";
        log.info("Request received getUnassignedTicketCounts with requestData for Business: {} ",businessId);
        
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getUnassignedTicketCounts API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);
        ResponseEntity<ApiResponseDto> responseEntity = externalService.getUnassignedTicketCounts();
        
		
		log.info("T-ID : {} , ****#### getUnassignedTicketCounts API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getUnassignedTicketCounts API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
        long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
        log.info("Response received getUnassignedTicketCounts with responseData: for {}  and Business: {}  Completed in {} ",responseEntity,businessId,timeDiff);
        
        return responseEntity;
    }
	
	@ApiOperation(value = "This API is used to update ticketStatus.")
	@PostMapping("/updateTicketStatus")
	public ResponseEntity<ApiResponseDto> updateTicketStatus(@RequestBody List<UpdateTicketNumbersDto> ticketNumbersDto,
			HttpServletRequest servletRequest) {
		LocalDateTime startTime=LocalDateTime.now();
		String businessId = BusinessContext.getTenantId();
		 String logKey="updateTicketStatus API";
		log.info("Request received for updateTicketStatus with requestData: {} and businessId: {} ",ticketNumbersDto,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , updateTicketStatus API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketNumbersDto,
				getObjectSize(ticketNumbersDto,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = externalService.updateByTicketStatus(ticketNumbersDto);
		
		log.info("T-ID : {} , ****#### updateTicketStatus API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , updateTicketStatus API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("{} response is for updateTicketStatus for business : {}  Completed in {}",responseEntity,BusinessContext.getTenantId(),timeDiff);
		return responseEntity;
		
	}
	
	@ApiOperation(value = "This API is used to call Technician.")
	@PostMapping("/callTechnician")
	public ResponseEntity<ApiResponseDto> callTechnician(@RequestBody CallTechnicianDto callTechnicianDto,
			HttpServletRequest servletRequest) {
		LocalDateTime startTime=LocalDateTime.now();
		String businessId = BusinessContext.getTenantId();	
		String logKey="callTechnician API";
		log.info("Request received for callTechnician with requestData: {} and businessId: {} ",callTechnicianDto,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , callTechnician API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), callTechnicianDto,
				getObjectSize(callTechnicianDto,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = externalService.callTechnician(callTechnicianDto);
		
		log.info("T-ID : {} , ****#### callTechnician API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , callTechnician API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("Response received for callTechnician with responseData: {} and businessId: {}  Completed in {} ",responseEntity,businessId,timeDiff);
		
		return responseEntity;
		
	}
	@ApiOperation(value = "This API is used to save TicketAuditTrails.")
	@PostMapping("/saveTicketAuditTrails")
	public ResponseEntity<ApiResponseDto> saveTicketAuditTrails(@RequestBody TicketTrailsCollectionDTO lumenCollectionUpdateDTO,
			HttpServletRequest servletRequest) {
		String businessId = BusinessContext.getTenantId();	
		String logKey="saveTicketAuditTrails API";
		log.info("Request received for saveTicketAuditTrails with requestData: {} and businessId: {} ",lumenCollectionUpdateDTO,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , saveTicketAuditTrails API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), lumenCollectionUpdateDTO,
				getObjectSize(lumenCollectionUpdateDTO,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = ticketRepo.saveTicketAuditTrails(lumenCollectionUpdateDTO);
		
		
		log.info("T-ID : {} , ****#### saveTicketAuditTrails API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , saveTicketAuditTrails API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		
		return responseEntity;
		
	}
	
	@ApiOperation(value = "This API is used to get TicketAuditTrails.")
	@PostMapping(value = "/getTicketAuditTrails")
	public ResponseEntity<ApiResponseDto> getTicketStatusBubbleCount(@RequestBody GetTicketTrailsCollectionDTO lumenCollectionUpdateDTO,
			HttpServletRequest servletRequest) {	
		String businessId = BusinessContext.getTenantId();	
		String logKey="getTicketAuditTrails API";
		log.info("Request received for getTicketAuditTrails with requestData: {} and businessId: {} ",lumenCollectionUpdateDTO,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketAuditTrails API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), lumenCollectionUpdateDTO,
				getObjectSize(lumenCollectionUpdateDTO,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = ticketRepo.getTicketAuditTrails(lumenCollectionUpdateDTO);
		
		
		log.info("T-ID : {} , ****#### getTicketAuditTrails API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTicketAuditTrails API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to get Team Workload Details for Manager APP")
	@PostMapping(value = "/myTeamWorkload")
	public ResponseEntity<ExternalAppResponse> myTeamWorkload(@RequestBody MyTeamWorkloadRequest myTeamWorkloadRequest,
			HttpServletRequest servletRequest) {		
		String businessId = BusinessContext.getTenantId();
		String logKey="myTeamWorkload API";
		LocalDateTime startTime=LocalDateTime.now();
		log.info("{} received with requestData: {} and businessId: {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,myTeamWorkloadRequest,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , myTeamWorkload API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), myTeamWorkloadRequest,
				getObjectSize(myTeamWorkloadRequest,logKey));
		
		ResponseEntity<ExternalAppResponse> responseEntity = externalService.myTeamWorkload(myTeamWorkloadRequest);
		
		log.info("T-ID : {} , ****#### myTeamWorkload API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , myTeamWorkload API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("{} response is {} for businessId: {} Completed in {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,responseEntity.getStatusCode(),businessId,timeDiff);
		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to update first Ticket.")
	@PostMapping("/firstTicketUpdate")
	public ResponseEntity<ApiResponseDto> firstTicketUpdate(@RequestBody firstTicketNumberDto ticketNumberDto,
			HttpServletRequest servletRequest) {
		String businessId = BusinessContext.getTenantId();
		String logKey="firstTicketUpdate API";
		LocalDateTime startTime=LocalDateTime.now();
		log.info("Request received for firstTicketUpdate with requestData: {} and businessId: {} ",ticketNumberDto,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , firstTicketUpdate API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketNumberDto,
				getObjectSize(ticketNumberDto,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = externalService.firstTicketUpdate(ticketNumberDto);
		
		
		log.info("T-ID : {} , ****#### firstTicketUpdate API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , firstTicketUpdate API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("Responce received for firstTicketUpdate with responseData: {} and businessId: {}  Completed in {} ",responseEntity,businessId,timeDiff);
		
		return responseEntity;
		
	}
	
	@ApiOperation(value = "This API is used to fetch all UnAssigned ticket for technician by technicianId and SupervisorId ")
	@PostMapping("/fetchQueueListBySupervisorId")
	public ResponseEntity<ApiResponseDto> fetchQueueListBySupervisorId(@RequestBody AgentAndSupervisorRequestDTO agentAndSupervisorRequest,
			HttpServletRequest servletRequest) {
		String businessId = BusinessContext.getTenantId();
		String logKey="fetchQueueListBySupervisorId API";
		LocalDateTime startTime=LocalDateTime.now();
		log.info("Request received for fetchQueueListBySupervisorId {} with requestData: {} and businessId: {} ",agentAndSupervisorRequest,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , fetchQueueListBySupervisorId API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), agentAndSupervisorRequest,
				getObjectSize(agentAndSupervisorRequest,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity= externalService.fetchQueueListBySupervisorId(agentAndSupervisorRequest);
		
		log.info("T-ID : {} , ****#### fetchQueueListBySupervisorId API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , fetchQueueListBySupervisorId API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("Response received for fetchQueueListBySupervisorId with responseData: {} and businessId: {}  Completed in {} ",responseEntity,businessId,timeDiff);
		
		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to save ptoTomorrowFeed for Technician Availability")
	@PostMapping("/ptoTomorrowFeed")
	public ResponseEntity<ResponseDTO> ptoTomorrowFeed(@RequestBody TechnicianAvailabilityRequest request,
			HttpServletRequest servletRequest) {
		String logKey="ptoTomorrowFeed API";
		log.info("{} Received for  business: {} and request for ptoTomorrowFeed : {}",DispatchControllerConstants.PTO_TOMMORROW_FEED,BusinessContext.getTenantId(),request);
		
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , ptoTomorrowFeed API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = externalService.ptoTomorrowFeed(request);
		
		log.info("T-ID : {} , ****#### ptoTomorrowFeed API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , ptoTomorrowFeed API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
    	log.info("{} response is {} for business : {}",DispatchControllerConstants.PTO_TOMMORROW_FEED,response,BusinessContext.getTenantId());

		return response;
	}
	
	@ApiOperation(value = "This API is used to save ptoWeeklyFeed for Technician Availability")
	@PostMapping("/ptoWeeklyFeed")
	public ResponseEntity<ResponseDTO> ptoWeeklyFeed(@RequestBody TechnicianAvailabilityRequest request,
			HttpServletRequest servletRequest) {
		String logKey="ptoWeeklyFeed API";
		log.info("{} Received for  business: {} and request for ptoWeeklyFeed : {}",DispatchControllerConstants.PTO_WEEKLY_FEED,BusinessContext.getTenantId(),request);
		
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , ptoWeeklyFeed API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = externalService.ptoWeeklyFeed(request);
		
		
		log.info("T-ID : {} , ****#### ptoWeeklyFeed API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , ptoWeeklyFeed API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
    	log.info("{} response is {} for business : {}",DispatchControllerConstants.PTO_WEEKLY_FEED,response,BusinessContext.getTenantId());

		return response;
	}
	
	@ApiOperation(value = "Get sequence of tickets By technicianId ")
    @PostMapping(value = "/ticketSequenceListByTechId")
    public ResponseEntity<ApiResponseDto> ticketSequenceListByTechId(@RequestBody TechIdDto technicianId,
			HttpServletRequest servletRequest) {
		LocalDateTime startTime=LocalDateTime.now();
        String businessId = BusinessContext.getTenantId();
		String logKey="ticketSequenceListByTechId API";
        log.info("{} Request received for ticketSequenceListByTechId Parameter {} and businessId: {} ",DispatchControllerConstants.TICKET_SEQUENCE_lIST,technicianId,businessId);
    	
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , ticketSequenceListByTechId API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), technicianId,
				getObjectSize(technicianId,logKey));
		
        ResponseEntity<ApiResponseDto> responseEntity = externalService.ticketSequenceListByTechId(technicianId ,businessId);
    	
		log.info("T-ID : {} , ****#### ticketSequenceListByTechId API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , ticketSequenceListByTechId API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

        long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
        log.info("{} Responce received for ticketSequenceListByTechId Parameter {} and businessId: {}  Completed in {}",DispatchControllerConstants.TICKET_SEQUENCE_lIST,technicianId,businessId,timeDiff);
        return responseEntity;
    }
	
	@ApiOperation(value = "This API is used to update technician details from Connector which in turn gets the details from external source")
	@PostMapping("/updateTechnicianDetailsToDC")
	public ResponseEntity<ResponseDTO> updateTechnicianDetailsToDC(@RequestBody Agent requestAgent,
			HttpServletRequest servletRequest) {
		String logKey="updateTechnicianDetailsToDC API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.UPDATE_TECHNICIAN_DETAILS_TO_DC,BusinessContext.getTenantId(),requestAgent);
		
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , updateTechnicianDetailsToDC API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), requestAgent,
				getObjectSize(requestAgent,logKey));
		
		ResponseEntity<ResponseDTO> response = externalService.updateTechnicianDetailsToDC(requestAgent);
		
		log.info("T-ID : {} , ****#### updateTechnicianDetailsToDC API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , updateTechnicianDetailsToDC API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));

    	log.info("{} response is {} for business : {}",DispatchControllerConstants.UPDATE_TECHNICIAN_DETAILS_TO_DC,response,BusinessContext.getTenantId());

		return response;
	}
	
	@ApiOperation(value = "This API is used to update Supervisor details from Connector which in turn gets the details from external source")
	@PostMapping("/updateSupervisorDetailsToDC")
	public ResponseEntity<ResponseDTO> updateSupervisorDetailsToDC(@RequestBody SupervisorPolygonMapping request,
			HttpServletRequest servletRequest) {
		String logKey="updateSupervisorDetailsToDC API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.UPDATE_SUPERVISOR_DETAILS_TO_DC,BusinessContext.getTenantId(),request);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , updateSupervisorDetailsToDC API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = externalService.updateSupervisorDetailsToDC(request);
		
		log.info("T-ID : {} , ****#### updateSupervisorDetailsToDC API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , updateSupervisorDetailsToDC API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));

    	log.info("{} response is {} for business : {}",DispatchControllerConstants.UPDATE_SUPERVISOR_DETAILS_TO_DC,response,BusinessContext.getTenantId());

		return response;
	}
	
	@ApiOperation(value = "This API is used to Fetch data from SFTP server and workday url to read file data and update ticket score data")
    @GetMapping("/AutoUpdateTicketScore")
    public String getEmployeeQualityScoreAndServiceData() {
    	log.info("Request Recived for AutoUpdateTicketScore API for business Id : {}",BusinessContext.getTenantId());
    	externalService.getEmployeeQualityScoreAndServiceData(BusinessContext.getTenantId(),BusinessTokenContext.getBusinessToken());
        return DispatchControllerConstants.STATUS_SUCCESS;
    }
}
