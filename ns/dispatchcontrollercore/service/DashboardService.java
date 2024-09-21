package com.straviso.ns.dispatchcontrollercore.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.straviso.ns.dispatchcontrollercore.dto.TicketCountReportsIpDto;
import com.straviso.ns.dispatchcontrollercore.dto.request.DashboardRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;

@Service
public interface DashboardService {

	ApiResponseDto ticketFetchCount(TicketCountReportsIpDto ticketCountReportsIpDto);
	ResponseEntity<ResponseDTO> getTicketCountStatByStatus(DashboardRequest request);
	ResponseEntity<ResponseDTO> getTicketCountStatByAction(DashboardRequest request);
	ResponseEntity<ResponseDTO> getTopTechnicianCount(ToptechnicianCountRequest request);
	ResponseEntity<ResponseDTO> getTopTicketCount(ToptechnicianCountRequest request);
	ResponseEntity<ResponseDTO> getTechnicianCount();
	ResponseEntity<ApiResponseDto> getTotalManagerCount();
	ResponseEntity<ApiResponseDto> getTotalSupervisorCount();
	ResponseEntity<ResponseDTO> getStateWiseTicketCount(DashboardRequest request);

}
