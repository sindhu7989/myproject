package com.straviso.ns.dispatchcontrollercore.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketExportDataRequest;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.CalenderViewDto;
import com.straviso.ns.dispatchcontrollercore.dto.GroupByTransferList;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
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
import com.straviso.ns.dispatchcontrollercore.dto.response.DataExportFieldResponse;
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
import com.straviso.ns.dispatchcontrollercore.dto.ExportTicketResponseDTO;
import com.straviso.ns.dispatchcontrollercore.service.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@RestController
@CrossOrigin(origins = "*")
@Log4j2
@RequestMapping("/exportData")
public class ExportDataController {
	
	@Autowired
	ExportDataService exportDataService;
	
	private double getObjectSize(Object input) {
		return input != null && !input.toString().trim().isEmpty() ? (double) input.toString().getBytes().length / 1000
				: 0;
	}
	
	@ApiOperation(value = "ExportTicket data to file")
	@PostMapping(value = "/exportTicketData")
	public ResponseEntity<ExportTicketResponseDTO> exportTicketData(@RequestBody TicketExportDataRequest request,
			HttpServletRequest servletRequest) {
		String businessId = BusinessContext.getTenantId();
		log.info("Request received for exportTicketData with requestData: {} and businessId: {} ",request,businessId);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , exportTicketData API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);
		
		ResponseEntity<ExportTicketResponseDTO> responseEntity = exportDataService.exportTicketData(request,businessId);

		log.info("T-ID : {} , ****#### exportTicketData API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		
		return responseEntity;
	}
	
	@ApiOperation(value = "Data Export Advanced Search Fields  for Ticket")
	@GetMapping(value = "/getDataExportAdvancedSearchFields")
	public DataExportFieldResponse getDatExportAdvancedSearchFields(HttpServletRequest servletRequest) {

		String businessId = BusinessContext.getTenantId();

		log.info("Request received for getDataExportAdvancedSearchFields and businessId: {} ",businessId);

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getDataExportAdvancedSearchFields API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);

		DataExportFieldResponse responseEntity = exportDataService.getDataExportAdvancedSearchFields();

		log.info("T-ID : {} , ****#### getDataExportAdvancedSearchFields API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		
		return responseEntity;
	}
	
	@ApiOperation(value = "ExportTicket data to file")
	@PostMapping(value = "/exportCockpitTicketData")
	public ResponseEntity<ExportTicketResponseDTO> exportCockpitTicketData(@RequestBody TicketExportDataRequest request,
			HttpServletRequest servletRequest) {
		String businessId = BusinessContext.getTenantId();
		log.info("Request received for exportTicketData with requestData: {} and businessId: {} ",request,businessId);

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , exportCockpitTicketData API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);
		
		ResponseEntity<ExportTicketResponseDTO> responseEntity = exportDataService.exportCockpitTicketData(request,businessId);

		log.info("T-ID : {} , ****#### exportCockpitTicketData API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		
		return responseEntity;
	}
	
	@ApiOperation(value = "Data Export Advanced Search Fields  for Ticket")
	@GetMapping(value = "/getDataExportCockpitAdvancedSearchFields")
	public DataExportFieldResponse getDatExportCockpitAdvancedSearchFields(HttpServletRequest servletRequest) {

		String businessId = BusinessContext.getTenantId();

		log.info("Request received for getDataExportCockpitAdvancedSearchFields and businessId: {} ",businessId);

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getDataExportCockpitAdvancedSearchFields API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);

		DataExportFieldResponse responseEntity = exportDataService.getDataExportCockpitAdvancedSearchFields();
		
		log.info("T-ID : {} , ****#### getDataExportCockpitAdvancedSearchFields API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		
		return responseEntity;
	}
	
	
	
	@ApiOperation(value = "ExportTicket data to file")
	@PostMapping(value = "/exportTicketTransactionData")
	public ResponseEntity<ExportTicketResponseDTO> exportTicketTransactionData(@RequestBody TicketExportDataRequest request,
			HttpServletRequest servletRequest) {
		String businessId = BusinessContext.getTenantId();
		log.info("Request received for exportTicketData with requestData: {} and businessId: {} ",request,businessId);

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , exportCockpitTicketData API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);
		
		ResponseEntity<ExportTicketResponseDTO> responseEntity = exportDataService.exportTicketTransactionData(request,businessId);

		log.info("T-ID : {} , ****#### exportCockpitTicketData API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		
		return responseEntity;
	}

}
