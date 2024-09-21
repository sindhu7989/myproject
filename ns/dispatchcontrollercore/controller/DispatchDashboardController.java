package com.straviso.ns.dispatchcontrollercore.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.request.DashboardRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.dto.TicketCountReportsIpDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.service.DashboardService;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@RestController
@CrossOrigin(origins = "*")
@Log4j2
@RequestMapping("/dashboard")
public class DispatchDashboardController {
	
	@Autowired
	DashboardService dashboardService;
	
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

	@ApiOperation(value = "Get Ticket Count By Status")
	@PostMapping("/getTicketCountStatByStatus")
	public ResponseEntity<ResponseDTO> getRoute(@RequestBody DashboardRequest request,
			HttpServletRequest servletRequest) {
		String logKey="getTicketCountStatByStatus API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TICKET_COUNT_BY_STATUS_REQUEST,BusinessContext.getTenantId(),request);
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketCountStatByStatus API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = dashboardService.getTicketCountStatByStatus(request);
		
		log.info("T-ID : {} , ****#### getTicketCountStatByStatus API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTicketCountStatByStatus API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
    	log.info("{} response is {} for business : {}",DispatchControllerConstants.GET_TICKET_COUNT_BY_STATUS_REQUEST,response,BusinessContext.getTenantId());

		return response;
	}
	
	@ApiOperation(value = "This API is used to get count of ticket on different Status")
	@PostMapping("/ticketFetchCount")
	public ResponseEntity<ApiResponseDto> ticketFetchCount(@RequestBody TicketCountReportsIpDto ticketCountReportsIpDto) {
	
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TICKET_COUNT_REQUEST,BusinessContext.getTenantId(),ticketCountReportsIpDto);	
		return new ResponseEntity<ApiResponseDto>(dashboardService.ticketFetchCount(ticketCountReportsIpDto),HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "This API is used to get count of ticket on graph for different action.")
	@PostMapping("/getTicketCountStatByAction")
	public ResponseEntity<ResponseDTO> getTicketCountStatByAction(@RequestBody DashboardRequest request,
			HttpServletRequest servletRequest) {
		String logKey="getTicketCountStatByAction API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TICKET_COUNT_BY_ACTION_REQUEST,BusinessContext.getTenantId(),request);

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTicketCountStatByAction API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = dashboardService.getTicketCountStatByAction(request);

		log.info("T-ID : {} , ****#### getTicketCountStatByAction API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTicketCountStatByAction API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
    	log.info("{} response is {} for business : {}",DispatchControllerConstants.GET_TICKET_COUNT_BY_STATUS_REQUEST,response,BusinessContext.getTenantId());

		return response;
	}
	
	@ApiOperation(value = "Get Top Techinican Count By Action Status")
	@PostMapping("/getTopTechnicianCount")
	public ResponseEntity<ResponseDTO> getTicketCountStatByAction(@RequestBody ToptechnicianCountRequest request,
			HttpServletRequest servletRequest) {
		String logKey="getTopTechnicianCount API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TOP_TECHNICIAN_COUNT_REQUEST,BusinessContext.getTenantId(),request);

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTopTechnicianCount API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = dashboardService.getTopTechnicianCount(request);

		log.info("T-ID : {} , ****#### getTopTechnicianCount API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTopTechnicianCount API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
		log.info("{} response is {} for business : {}",DispatchControllerConstants.GET_TOP_TECHNICIAN_COUNT_REQUEST,response,BusinessContext.getTenantId());

		return response;
	}
	
    @ApiOperation(value = "Get Top Ticket Count By Action Status")
	@PostMapping("/getTopTicketCount")
	public ResponseEntity<ResponseDTO> getTopTicketCount(@RequestBody ToptechnicianCountRequest request,
			HttpServletRequest servletRequest) {
    	String logKey="getTopTicketCount API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TOP_TICKET_COUNT_REQUEST,BusinessContext.getTenantId(),request);

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTopTicketCount API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), request,
				getObjectSize(request,logKey));
		
		ResponseEntity<ResponseDTO> response = dashboardService.getTopTicketCount(request);

		log.info("T-ID : {} , ****#### getTopTicketCount API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTopTicketCount API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
		log.info("{} response is {} for business : {}",DispatchControllerConstants.GET_TOP_TICKET_COUNT_REQUEST,response,BusinessContext.getTenantId());

		return response;
	}
	
    @ApiOperation(value = "Get Techinican Count By Action Status")
	@GetMapping("/getTechnicianCount")
	public ResponseEntity<ResponseDTO> getTechnicianCount(HttpServletRequest servletRequest) {
    	String logKey="getTechnicianCount API";
		log.info("{} Received for business: {}",DispatchControllerConstants.GET_TECHNICIAN_COUNT_REQUEST,BusinessContext.getTenantId());
		

		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTechnicianCount API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);
		
		ResponseEntity<ResponseDTO> response = dashboardService.getTechnicianCount();
		
		log.info("T-ID : {} , ****#### getTechnicianCount API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTechnicianCount API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
    	log.info("{} response is {} for business : {}",DispatchControllerConstants.GET_TECHNICIAN_COUNT_REQUEST,response,BusinessContext.getTenantId());

		return response;
	}
	
	@ApiOperation(value = "This API is used to get count of total Managers.")
	@GetMapping("/getTotalManagerCount")
	public ResponseEntity<ApiResponseDto> getTotalManagerCount(HttpServletRequest servletRequest) {
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TOTAL_MANAGER_COUNT_REQUEST,BusinessContext.getTenantId());
		String logKey="getTotalManagerCount API";
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTotalManagerCount API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);
		
		ResponseEntity<ApiResponseDto> response = dashboardService.getTotalManagerCount();
		
		log.info("T-ID : {} , ****#### getTotalManagerCount API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTotalManagerCount API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
    	log.info("{} response is {} for business : {}",DispatchControllerConstants.GET_TOTAL_MANAGER_COUNT_REQUEST,response,BusinessContext.getTenantId());
		return response;
	}
	
	@ApiOperation(value = "This API is used to get count of total supervisors.")
	@GetMapping("/getTotalSupervisorCount")
	public ResponseEntity<ApiResponseDto> getTotalSeperviserCount(HttpServletRequest servletRequest) {
		String logKey="getTotalSupervisorCount API";
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TOTAL_SUPERVISER_COUNT_REQUEST,BusinessContext.getTenantId());
		
		long startTimes = System.currentTimeMillis();
		log.info("T-ID : {} , getTotalSupervisorCount API ctr-req is : {} , ctr-req-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), null,
				null);
		
		ResponseEntity<ApiResponseDto> response = dashboardService.getTotalSupervisorCount();
		
		log.info("T-ID : {} , ****#### getTotalSupervisorCount API execution_time : {} ms",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID),
				(System.currentTimeMillis() - startTimes));
		log.info("T-ID : {} , getTotalSupervisorCount API ctr-res is : {} , ctr-res-size is : {} KB ",
				servletRequest.getAttribute(DispatchControllerConstants.TRANSCATIONID), response.getStatusCode(),
				getObjectSize(response,logKey));
		
    	log.info("{} response is {} for business : {}",DispatchControllerConstants.GET_TOTAL_SUPERVISER_COUNT_REQUEST,response,BusinessContext.getTenantId());
		return response;
	}
	
	@ApiOperation(value = "This API is used to show the State Wise tickets count.")
	@PostMapping("/getStateWiseTicketCount")
	public ResponseEntity<ResponseDTO> getTotalSeperviserCount(@RequestBody DashboardRequest request) {
		log.info("{} Received for business: {} and request : {}",DispatchControllerConstants.GET_TICKET_COUNT_BY_STATEWISE_REQUEST,BusinessContext.getTenantId());
		return dashboardService.getStateWiseTicketCount(request);
	}



	
}
