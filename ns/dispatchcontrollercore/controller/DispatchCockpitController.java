package com.straviso.ns.dispatchcontrollercore.controller;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import java.time.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.CalenderViewDto;
import com.straviso.ns.dispatchcontrollercore.dto.GroupByTransferList;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketDCSolverGlobalSearchDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TechnicianDetailsDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketCountDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketCountReportsIpDto;
import com.straviso.ns.dispatchcontrollercore.dto.TicketNumbersDto;
import com.straviso.ns.dispatchcontrollercore.dto.TransferTicketDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchFieldRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.CockpitBubbleCountStatRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.FetchCountById;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketDetailsByMasterExternalIdRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketDetailsBySupervisorIdRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.supervisorCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDtoAPK;
import com.straviso.ns.dispatchcontrollercore.dto.response.CountResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.GroupByActionResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponsePageDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianIdDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianResponseDto;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianAssignmentSolutionRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.repositoryImpl.AgentRepositoryImpl;
import com.straviso.ns.dispatchcontrollercore.repositoryImpl.SupervisorRepositoryImpl;
import com.straviso.ns.dispatchcontrollercore.service.CockpitService;
import com.straviso.ns.dispatchcontrollercore.service.GroupByActionService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@RestController
@CrossOrigin(origins = "*")
@Log4j2
@RequestMapping("/Cockpit")
public class DispatchCockpitController {
	
	@Autowired
	CockpitService getCockpitService;
	
	@Autowired
	GroupByActionService groupByActionService ;
	
	
	@Autowired
	TicketRepository ticketRepo;

	@Autowired
	TechnicianAssignmentSolutionRepo technicianAssignmentSolutionRepo;

	@Autowired
	AgentRepositoryImpl agentRepositoryImpl;

	@Autowired
	SupervisorRepositoryImpl supervisorRepositoryImpl;
	
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
	
	@ApiOperation(value = "Get ticket bubble count by status")
	@PostMapping(value = "/getTicketStatusBubbleCounts")
	public ResponseEntity<ApiResponseDto> getTicketStatusBubbleCount(@RequestBody TicketCountDTO ticketDetails,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="getTicketStatusBubbleCounts API";
		log.info("Request received for {} with requestData: {} and businessId: {} ",ticketDetails,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketStatusBubbleCounts API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketDetails, getObjectSize(ticketDetails,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.getTicketStatusBubbleCount(ticketDetails);
		
		log.info("T-ID : {} , ****#### getTicketStatusBubbleCounts API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTicketStatusBubbleCounts API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
		return responseEntity;
	}
	
	@ApiOperation(value = "Get all technician Details")
	@PostMapping(value = "/getAllTechnicianDetials")
	public ResponseEntity<CountResponseDTO> getAllTechnicianDetials(@RequestBody TechnicianDetailsDTO technicianDetailsDTO,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="getAllTechnicianDetials API";
		log.info("Request received for getAllTechnicianDetials  with requestData: for {}and Business: {} ",technicianDetailsDTO ,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getAllTechnicianDetials API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), technicianDetailsDTO, getObjectSize(technicianDetailsDTO,logKey));
		
		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getAllTechnicianDetials(technicianDetailsDTO);
		
		log.info("T-ID : {} , ****#### getAllTechnicianDetials API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getAllTechnicianDetials API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "Get all Ticket Details by global status")
	@PostMapping(value = "/getAllTicketDetialsByglobalStatus")
	public ResponseEntity<CountResponseDTO> getTicketDetialsByglobalStatus(@RequestBody TechnicianDetailsDTO technicianDetailsDTO,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="getAllTicketDetialsByglobalStatus API";
		log.info("Request received getAllTicketDetialsByglobalStatus with requestData: for {}  and Business: {} ",technicianDetailsDTO,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getAllTicketDetialsByglobalStatus API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), technicianDetailsDTO, getObjectSize(technicianDetailsDTO,logKey));
		
		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getTicketDetialsByglobalStatus(technicianDetailsDTO);
		log.info("T-ID : {} , ****#### getAllTicketDetialsByglobalStatus API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getAllTicketDetialsByglobalStatus API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "Get total count by global status for Tickets")
	@PostMapping(value = "/getTicketGlobalStatusCount")
	public ResponseEntity<ApiResponseDto> getTicketGlobalStatusCount(@RequestBody TicketCountDTO ticketDetails,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="getTicketGlobalStatusCount API";
		log.info("Request received getTicketGlobalStatusCount with requestData: for {}  and Business: {} ",ticketDetails,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketGlobalStatusCount API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketDetails, getObjectSize(ticketDetails,logKey));
	
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.getTicketGlobalStatusCount(ticketDetails);
		
		log.info("T-ID : {} , ****#### getTicketGlobalStatusCount API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTicketGlobalStatusCount API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity, logKey));
		return responseEntity;
	}
	
	@ApiOperation(value = "Get technician's assignment status count")
	@PostMapping(value = "/getTechnicianAssismentStatusCounts")
	public ResponseEntity<ApiResponseDto> getTechnicianAssismentStatusCounts(@RequestBody TicketCountDTO ticketDetails,
			HttpServletRequest request) {		
		String businessId = BusinessContext.getTenantId();
		String logKey="getTechnicianAssismentStatusCounts API";
		log.info("Request received for getTechnicianAssismentStatusCounts with requestData: {} and businessId: {} ",ticketDetails,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTechnicianAssismentStatusCounts API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketDetails, getObjectSize(ticketDetails,logKey));
	
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.getTechnicianAssismentStatusCounts(ticketDetails);
		
		log.info("T-ID : {} , ****#### getTechnicianAssismentStatusCounts API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTechnicianAssismentStatusCounts API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		return responseEntity;
	}
	
	@ApiOperation(value = "Get technician's details by assignment status")
	@PostMapping(value = "/getTechnicianAssismentStatusDetails")
	public ResponseEntity<CountResponseDTO> getTechnicianAssismentStatusDetails(@RequestBody TicketCountDTO ticketDetails,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTechnicianAssismentStatusDetails API";
		log.info("Request received for getTechnicianAssismentStatusDetails  with requestData: {} and businessId: {} ",ticketDetails,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTechnicianAssismentStatusDetails API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketDetails, getObjectSize(ticketDetails,logKey));
	
		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getTechnicianAssismentStatusDetails(ticketDetails);
		
		log.info("T-ID : {} , ****#### getTechnicianAssismentStatusDetails API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTechnicianAssismentStatusDetails API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to get all tickets assigned to technician by technicianId ")
	@PostMapping("/getTechnicianAssismentSolutionByTechnicianId")
	public ResponseEntity<ResponsePageDto> getCalenderViewByTechnicianId(@RequestBody CalenderViewDto calenderViewDto ,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTechnicianAssismentSolutionByTechnicianId API";
		log.info("Request received for getTechnicianAssismentSolutionByTechnicianId with requestData: {} with requestData: {} and businessId: {} ",calenderViewDto,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTechnicianAssismentSolutionByTechnicianId API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), calenderViewDto, getObjectSize(calenderViewDto,logKey));
		
		ResponseEntity<ResponsePageDto> responseEntity= getCockpitService.getCalenderViewDetailsService(calenderViewDto);
		
		log.info("T-ID : {} , ****#### getTechnicianAssismentSolutionByTechnicianId API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTechnicianAssismentSolutionByTechnicianId API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to get day wise workload of a technician ")
	@PostMapping("/getTechnicianWorkloadByTechnicianId")
	public ResponseEntity<TechnicianResponseDto> getTechnicianWorkloadByTechnicianId(@RequestBody CalenderViewDto calenderViewDto ,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTechnicianWorkloadByTechnicianId API";
		log.info("Request received for getTechnicianWorkloadByTechnicianId with requestData: {} and businessId: {} ",calenderViewDto,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTechnicianWorkloadByTechnicianId API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), calenderViewDto, getObjectSize(calenderViewDto,logKey));
		
		ResponseEntity<TechnicianResponseDto> responseEntity= getCockpitService.getTechnicianWorkloadByTechnicianId(calenderViewDto);
		
		log.info("T-ID : {} , ****#### getTechnicianWorkloadByTechnicianId API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTechnicianWorkloadByTechnicianId API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "Get all ticket details by ticket numbers")
	@PostMapping("/getAllTicketsDetailsByTicketNumbers")
	public ResponseEntity<ApiResponseDto> getAllTicketsDetailsByTechnicianId(@RequestBody CalenderViewDto calenderViewDto ,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getAllTicketsDetailsByTicketNumbers API";
		log.info("Request received for getAllTicketsDetailsByTicketNumbers with requestData: {} and businessId: {} ",calenderViewDto,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getAllTicketsDetailsByTicketNumbers API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), calenderViewDto, getObjectSize(calenderViewDto,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity= getCockpitService.getAllTicketsDetailsService(calenderViewDto);
		
		log.info("T-ID : {} , ****#### getAllTicketsDetailsByTicketNumbers API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getAllTicketsDetailsByTicketNumbers API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to show get all the  details  of the technician by passing List of technician ids ")
	@PostMapping(value = "/getTechnicianDetailsById")
	public ResponseEntity<ApiResponseDto> getTechnicianDetailsById(@RequestBody TechnicianIdDto technicianIds,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTechnicianDetailsById API";
		log.info("Request received for getTechnicianDetailsById with requestData: {} and businessId: {} ",technicianIds,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTechnicianDetailsById API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), technicianIds, getObjectSize(technicianIds,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.getTechnicianDetailsById(technicianIds);
		
		log.info("T-ID : {} , ****#### getTechnicianDetailsById API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTechnicianDetailsById API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "Advanced Search for technican And Ticket")
	@PostMapping(value = "/advanceSearch")
	public ResponseEntity<CountResponseDTO> advanceSearch(@RequestBody AdvanceSearchRequest request,
			HttpServletRequest servletRequest) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="advanceSearch API";
		log.info("Request received for advanceSearch with requestData: {} and businessId: {} ",request,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , advanceSearch API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request, getObjectSize(request,logKey));
		
		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.advanceSearch(request);
		
		log.info("T-ID : {} , ****#### advanceSearch API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , advanceSearch API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to assign ticket back to queue.i.e.Ticket is in UnAssigned state.")
	@PostMapping("/AssignBackToQueue")
	public ResponseEntity<ApiResponseDto> assignBackToQueue(@RequestBody List<TicketNumbersDto> ticketNumbersDto,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();	
		String logKey="AssignBackToQueue API";
		log.info("Request received for AssignBackToQueue with requestData: {} and businessId: {} ",ticketNumbersDto,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , AssignBackToQueue API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketNumbersDto, getObjectSize(ticketNumbersDto,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.updateByTicketNumber(ticketNumbersDto);
		
		log.info("T-ID : {} , ****#### AssignBackToQueue API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , AssignBackToQueue API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		return responseEntity;
		
	}
	
	@ApiOperation(value = "Get Advanced Search Fields List for technican And Ticket")
	@PostMapping(value = "/getAdvanceSearchFields")
	public ResponseEntity<CountResponseDTO> getAdvanceSearchFields(@RequestBody AdvanceSearchFieldRequest request,
			HttpServletRequest servletRequest) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getAdvanceSearchFields API";
		log.info("Request received for getAdvanceSearchFields  and businessId: {} ",businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getAdvanceSearchFields API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request, getObjectSize(request,logKey));
		
		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getAdvanceSearchFields(request);
		
		log.info("T-ID : {} , ****#### getAdvanceSearchFields API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getAdvanceSearchFields API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to cancel assigned ticket to technician .i.e.Ticket in C state.")
	@PostMapping("/cancelTicketByTicketNumber")
	public ResponseEntity<ApiResponseDto> CancelTicketByTicketNumber(@RequestBody List<TicketNumbersDto> ticketNumbersDto,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="cancelTicketByTicketNumber API";
		log.info("Request received for cancelTicketByTicketNumber with requestData: {} and businessId: {} ",ticketNumbersDto,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , cancelTicketByTicketNumber API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), ticketNumbersDto, getObjectSize(ticketNumbersDto,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.cancelByTicketNumber(ticketNumbersDto);
		
		log.info("T-ID : {} , ****#### cancelTicketByTicketNumber API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , cancelTicketByTicketNumber API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		return responseEntity;
		
	}
	

	 @ApiOperation(value = "This API is used to create two tier Hieararchy of supervisor and technicians ")
	 @GetMapping("/createUserHierarchy")

	    public ResponseEntity<ApiResponseDto> createTechnicianHierarchy() {

	        String businessId = BusinessContext.getTenantId();

	        log.info("Request received for Hierarchy with businessId: {}",businessId);

	        return getCockpitService.generateTechnicianHierarchy();

	    }
	
	@ApiOperation(value = "Get ticket details by supervisor id")
	@PostMapping(value = "/getTicketDetailsBySupervisorId")
	public ResponseEntity<CountResponseDTO> getTicketDetailsBySupervisorId(@RequestBody TicketDetailsBySupervisorIdRequest request,
			HttpServletRequest servletRequest) {	
		String businessId = BusinessContext.getTenantId();
		String logKey="getTicketDetailsBySupervisorId API";
		log.info("Request received for getTicketDetailsBySupervisorId  and businessId: {} ",businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketDetailsBySupervisorId API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request, getObjectSize(request,logKey));
		
		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getTicketDetailsBySupervisorId(request);
		
		log.info("T-ID : {} , ****#### getTicketDetailsBySupervisorId API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTicketDetailsBySupervisorId API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "This API is used to show two tier Hieararchy of supervisor and technicians ")
	@GetMapping("/getHierarchy")
	public ResponseEntity<ApiResponseDto> getUserHierarchy() {

        String businessId = BusinessContext.getTenantId();
        
        log.info("Request received for Hierarchy with businessId: {}",businessId);

        return getCockpitService.getUserHierarchy();

    }
	@ApiOperation(value = "This API is used to transfer the tickets from one technician to another technician.")
	@PostMapping(value = "/transferTicket")
	 public ResponseEntity<ApiResponseDto> transferTicketByIds(@RequestBody GroupByTransferList groupByTransferList,
				HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="transferTicket API";
		log.info("{} Request received for transferTicket with requestData: {} and businessId: {} ",DispatchControllerConstants.TRANSFER_IN_DC,groupByTransferList,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , transferTicket API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), groupByTransferList, getObjectSize(groupByTransferList,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = groupByActionService.GroupByAction(groupByTransferList);
		
		log.info("T-ID : {} , ****#### transferTicket API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , transferTicket API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		log.info("{} Response is for transferTicket {} for business : {}",DispatchControllerConstants.TRANSFER_IN_DC,responseEntity,BusinessContext.getTenantId());
		return responseEntity;
	}
	
	@ApiOperation(value = "Get all ticket details by ticket number and conversationId")
	@PostMapping("/getTicketData")
	public ResponseEntity<ApiResponseDto> getTicketData(@RequestBody LumenCollectionUpdateDTO lumenCollectionUpdateDTO ,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTicketData API";
		log.info("Request received for getTicketData with requestData: {} and businessId: {} ",lumenCollectionUpdateDTO,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketData API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), lumenCollectionUpdateDTO, getObjectSize(lumenCollectionUpdateDTO,logKey));
		
		
		ApiResponseDto apiResponseDto=new ApiResponseDto();
		apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
		List<Map<String, String>> ticketData = ticketRepo.getTicketData(lumenCollectionUpdateDTO.getTicketNumber(),lumenCollectionUpdateDTO.getConversationId());
		if(ticketData !=null && !ticketData.isEmpty()) {
		apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
		apiResponseDto.setResponseData(ticketData);
		}else {
			apiResponseDto.setMessage("No Data Found for the given Ticket Number and ConversationId");
		}
		
		log.info("T-ID : {} , ****#### getTicketData API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTicketData API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), apiResponseDto.getStatus(),
				getObjectSize(apiResponseDto,logKey));

		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}
	
	@ApiOperation(value = "Get all tickets  details by ticket number and conversationId and view in json format")
	@PostMapping("/getTicketJson")
	public ResponseEntity<ApiResponseDto> getTicketJsonView(@RequestBody LumenCollectionUpdateDTO lumenCollectionUpdateDTO ,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTicketJson API";
		log.info("Request received for getTicketJson with requestData: {} and businessId: {} ",lumenCollectionUpdateDTO,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketJson API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), lumenCollectionUpdateDTO, getObjectSize(lumenCollectionUpdateDTO,logKey));
		
		ApiResponseDto apiResponseDto=new ApiResponseDto();
		apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
		Object ticketJsonView = ticketRepo.getTicketJsonView(lumenCollectionUpdateDTO.getTicketNumber(),lumenCollectionUpdateDTO.getConversationId());
		if(ticketJsonView !=null) {
		apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
		apiResponseDto.setResponseData(ticketJsonView);
		}else {
			apiResponseDto.setMessage("No Data Found for the given Ticket Number and ConversationId");
		}
		
		log.info("T-ID : {} , ****#### getTicketJson API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTicketJson API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), apiResponseDto.getStatus(),
				getObjectSize(apiResponseDto,logKey));
		return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "Get total UnAssigned count by supervisor id")
	@PostMapping(value = "/getTotalUnAssignedCountBySupervisorId")
	public ResponseEntity<ApiResponseDto> getTotalUnAssignedAssignedAndComletedCountBySupervisorId(@RequestBody supervisorCountRequest supervisorId,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTotalUnAssignedCountBySupervisorId API";
		log.info("Request received for getTotalUnAssignedCountBySupervisorId  and businessId: {} ",businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTotalUnAssignedCountBySupervisorId API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), supervisorId, getObjectSize(supervisorId,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.getTotalUnAssignedAssignedAndComletedCountBySupervisorId(supervisorId);
		
		log.info("T-ID : {} , ****#### getTotalUnAssignedCountBySupervisorId API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTotalUnAssignedCountBySupervisorId API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		return responseEntity;
	}
	
	@ApiOperation(value = "Get all Technician details by TechnicianId and view in json format")
	@PostMapping("/getTechnicianJson")
	public ResponseEntity<ApiResponseDto> getTechnicianJsonView(@RequestBody Agent agent ,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getTechnicianJson API";
		log.info("Request received for getTechnicianJson with requestData: {} and businessId: {} ",agent.getTechnicianId(),businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getTechnicianJson API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), agent, getObjectSize(agent,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity  = agentRepositoryImpl.getTechnicianJsonView(agent.getTechnicianId());
		
		log.info("T-ID : {} , ****#### getTechnicianJson API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getTechnicianJson API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
		return responseEntity;
		
	}
	
	@ApiOperation(value = "Get all Supervisor details by TechnicianId and view in json format")
	@PostMapping("/getSupervisorJson")
	public ResponseEntity<ApiResponseDto> getSupervisorJsonView(@RequestBody SupervisorPolygonMapping SupervisorDetails,
			HttpServletRequest request) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="getSupervisorJson API";
		log.info("Request received for getSupervisorJson with requestData: {} and businessId: {} ",SupervisorDetails.getSupervisorId(),businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getSupervisorJson API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), SupervisorDetails, getObjectSize(SupervisorDetails,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity  = supervisorRepositoryImpl.getSupervisorJsonView(SupervisorDetails.getSupervisorId());
	
		log.info("T-ID : {} , ****#### getSupervisorJson API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getSupervisorJson API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
		return responseEntity;
		
	}
	
	@ApiOperation(value = "Get Cockpit Bubble Status Count")
	@PostMapping(value = "/cockpitBubbleStatCount")
	public ResponseEntity<ApiResponseDto> cockpitbubbleStatCount(@RequestBody CockpitBubbleCountStatRequest request,
			HttpServletRequest servletRequest) {				
		String businessId = BusinessContext.getTenantId();
		String logKey="cockpitBubbleStatCount API";
		log.info("Request received for cockpitBubbleStatCount with requestData : {} and businessId: {} ",request,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , cockpitBubbleStatCount API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request, getObjectSize(request,logKey));
		
		ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.cockpitBubbleStatCount(request);
		
		log.info("T-ID : {} , ****#### cockpitBubbleStatCount API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , cockpitBubbleStatCount API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
		return responseEntity;
	}
	@ApiOperation(value = "Get all technicianID total count of completed and Open Status By SupervisorId ")
    @PostMapping(value = "/fetchOpenAndCompletedStatusCountBySupervisorId")
    public ResponseEntity<ApiResponseDto> fetchOpenAndCompletedStatusCountById(@RequestBody FetchCountById fetchCountById,
			HttpServletRequest request) {				   
        String businessId = BusinessContext.getTenantId();
    	String logKey="fetchOpenAndCompletedStatusCountBySupervisorId API";
        log.info("Request received for fetchOpenAndCompletedStatusCountBySupervisorId Parameter {} and businessId: {} ",fetchCountById,businessId);
     
        long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , fetchOpenAndCompletedStatusCountBySupervisorId API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), fetchCountById, getObjectSize(fetchCountById,logKey));
		
        ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.fetchOpenAndCompletedStatusCountById(fetchCountById ,businessId);
       
        log.info("T-ID : {} , ****#### fetchOpenAndCompletedStatusCountBySupervisorId API execution_time : {} ms",
        		request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , fetchOpenAndCompletedStatusCountBySupervisorId API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
        return responseEntity;
    }
	
	@ApiOperation(value = "Get location of technicians from technicianAssignmentSolution document By technicianId ")
    @PostMapping(value = "/fetchLocationFromAssignmentSolByTechId")
    public ResponseEntity<ApiResponseDto> fetchLocationFromAssignmentSolByTechId(@RequestBody TechnicianIdDto technicianIds,
			HttpServletRequest request) {				       
        String businessId = BusinessContext.getTenantId();
    	String logKey="fetchLocationFromAssignmentSolByTechId API";
        log.info("Request received for fetchLocationFromAssignmentSolByTechId Parameter {} and businessId: {} ",technicianIds,businessId);
       
        long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , fetchLocationFromAssignmentSolByTechId API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), technicianIds, getObjectSize(technicianIds,logKey));
		
        ResponseEntity<ApiResponseDto> responseEntity = getCockpitService.fetchLocationFromAssignmentSolByTechId(technicianIds ,businessId);
       
        log.info("T-ID : {} , ****#### fetchLocationFromAssignmentSolByTechId API execution_time : {} ms",
        		request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , fetchLocationFromAssignmentSolByTechId API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

        return responseEntity;
    }
	
	@ApiOperation(value = "This API is used to transfer the tickets from one technician to another technician for App(APK) side.")
	@PostMapping(value = "/transferTicketAPK")
	 public ResponseEntity<ApiResponseDtoAPK> transferTicketByIdsAPK(@RequestBody GroupByTransferList groupByTransferList,
				HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		LocalDateTime startTime=LocalDateTime.now();
		String logKey="transferTicketAPK API";
		log.info("{} Request received for transferTicketAPK with requestData: {} and businessId: {} ",DispatchControllerConstants.TRANSFER_IN_APK,groupByTransferList,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , transferTicketAPK API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), groupByTransferList,
				getObjectSize(groupByTransferList,logKey));

		ResponseEntity<ApiResponseDtoAPK> responseEntity = groupByActionService.GroupByActionAPK(groupByTransferList);
		log.info("T-ID : {} , ****#### transferTicketAPK API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , transferTicketAPK API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));
		
		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("{} Response is for transferTicketAPK {} for business : {}  Completed in {}",DispatchControllerConstants.TRANSFER_IN_APK,responseEntity,BusinessContext.getTenantId(),timeDiff);
		return responseEntity;
	}
	@ApiOperation(value = "Get all ticket details by masterEcternalId id for Assist ticket")
	@PostMapping(value = "/getAssistTicketByMasterExternalId")
	public ResponseEntity<CountResponseDTO> getAssistTicketByMasterExternalId(@RequestBody TicketDetailsByMasterExternalIdRequest request,
			HttpServletRequest servletRequest) {
		String businessId = BusinessContext.getTenantId();
		String logKey="getAssistTicketByMasterExternalId API";
		log.info("Request received for getAssistTicketByMasterExternalId  and businessId: {} ",businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getAssistTicketByMasterExternalId API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));

		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getAssistTicketByMasterExternalId(request);
		
		log.info("T-ID : {} , ****#### getAssistTicketByMasterExternalId API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getAssistTicketByMasterExternalId API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	
	@ApiOperation(value = "Get all Ticket Details by global status")
	@PostMapping(value = "/getAllTicketDetialsByglobalStatusForReports")
	public ResponseEntity<CountResponseDTO> getTicketDetialsByglobalStatusForReports(@RequestBody TechnicianDetailsDTO technicianDetailsDTO,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="getAllTicketDetialsByglobalStatusForReports API";
		log.info("Request received getAllTicketDetialsByglobalStatus with requestData: for {}  and Business: {} ",technicianDetailsDTO,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getAllTicketDetialsByglobalStatusForReports API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), technicianDetailsDTO,
				getObjectSize(technicianDetailsDTO,logKey));

		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getTicketDetialsByglobalStatusForReports(technicianDetailsDTO);
		
		log.info("T-ID : {} , ****#### getAllTicketDetialsByglobalStatusForReports API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getAllTicketDetialsByglobalStatusForReports API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "Serach text on ticketDCSolver collection on specific parameters")
	@PostMapping(value = "/ticketGlobalSearch")
	public ResponseEntity<ResponsePageDto> ticketDCSolverGlobalSearchDTO(@RequestBody TicketDCSolverGlobalSearchDTO searchText,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="ticketGlobalSearch API";
		log.info("Request received ticketGlobalSearch with requestData: for {}  and Business: {} ",searchText,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , ticketGlobalSearch API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), searchText,
				getObjectSize(searchText,logKey));

		ResponseEntity<ResponsePageDto> responseEntity = getCockpitService.ticketDCSolverGlobalSearchDTO(searchText);
		

		log.info("T-ID : {} , ****#### ticketGlobalSearch API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , ticketGlobalSearch API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
	@ApiOperation(value = "Get all supervisor Details")
	@PostMapping(value = "/getAllSupervisorDetails")
	public ResponseEntity<CountResponseDTO> getAllSupervisorDetails(@RequestBody TechnicianDetailsDTO technicianDetailsDTO,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="getAllSupervisorDetails API";
		log.info("Request received for getAllSupervisorDetails  with requestData: for {}and Business: {} ",technicianDetailsDTO ,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , getAllTechnicianDetials API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), technicianDetailsDTO, getObjectSize(technicianDetailsDTO,logKey));
		
		ResponseEntity<CountResponseDTO> responseEntity = getCockpitService.getAllSupervisorDetails(technicianDetailsDTO);
		
		log.info("T-ID : {} , ****#### getAllSupervisorDetails API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , getAllSupervisorDetails API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), responseEntity.getStatusCode(),
				getObjectSize(responseEntity,logKey));

		return responseEntity;
	}
	
 @ApiOperation(value="fetch all supervisors details ")
	@GetMapping(value = "/fetchAllSupervisor")
	public List<SupervisorPolygonMapping> fetchAllSupervisor(@RequestBody SupervisorPolygonMapping supervisorPolygonMapping,
			HttpServletRequest request) {
		String businessId = BusinessContext.getTenantId();
		String logKey="fetchAllSupervisor API";
	log.info("Request received for fetchAllSupervisor  with requestData: for {}and Business: {} ",supervisorPolygonMapping ,businessId);
		
		long startTime = System.currentTimeMillis();
		log.info("T-ID : {} , fetchAllSupervisor API ctr-req is : {} , ctr-req-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), supervisorPolygonMapping, getObjectSize(supervisorPolygonMapping,logKey));
		
		List<SupervisorPolygonMapping> SupervisorPolygonMapping = getCockpitService.fetchAllSupervisors(supervisorPolygonMapping);
		
		log.info("T-ID : {} , ****#### fetchAllSupervisor API execution_time : {} ms",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID),
			(System.currentTimeMillis() - startTime));
		log.info("T-ID : {} , fetchAllSupervisor API ctr-res is : {} , ctr-res-size is : {} KB ",
				request.getAttribute(DispatchControllerConstants.TRANSCATIONID), ((ResponseDTO) SupervisorPolygonMapping).getStatusCode(),
				getObjectSize(SupervisorPolygonMapping,logKey));

		return SupervisorPolygonMapping;
 }
}


//@GetMapping(value = "/fetchAllSupervisor")
//public List<SupervisorPolygonMapping> getAllMappings() {
//    return getCockpitService.fetchAllSupervisors();
//}
//}

