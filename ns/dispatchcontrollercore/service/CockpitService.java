package com.straviso.ns.dispatchcontrollercore.service;

import java.util.Collection;
import java.util.List;

import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;

import com.straviso.ns.dispatchcontrollercore.dto.CalenderViewDto;
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
import com.straviso.ns.dispatchcontrollercore.dto.response.CountResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.GroupByActionResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO; 
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponsePageDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianIdDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianResponseDto;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;

public interface CockpitService {

	ResponseEntity<ApiResponseDto> getTicketStatusBubbleCount(TicketCountDTO ticketDetails);

	ResponseEntity<CountResponseDTO> getAllTechnicianDetials(TechnicianDetailsDTO technicianDetailsDTO);

	ResponseEntity<CountResponseDTO> getTicketDetialsByglobalStatus(TechnicianDetailsDTO technicianDetailsDTO);

	ResponseEntity<ApiResponseDto> getTechnicianAssismentStatusCounts(TicketCountDTO ticketDetails);

	ResponseEntity<CountResponseDTO> getTechnicianAssismentStatusDetails(TicketCountDTO ticketDetails);

	ResponseEntity<ResponsePageDto> getCalenderViewDetailsService(CalenderViewDto calenderViewDto);
	
	ResponseEntity<TechnicianResponseDto> getTechnicianWorkloadByTechnicianId(CalenderViewDto calenderViewDto);

	ResponseEntity<ApiResponseDto> getAllTicketsDetailsService(CalenderViewDto calenderViewDto);

	ResponseEntity<ApiResponseDto> getTechnicianDetailsById(TechnicianIdDto technicianIds);

	ResponseEntity<ApiResponseDto> getTicketGlobalStatusCount(TicketCountDTO ticketDetails);
	ResponseEntity<CountResponseDTO> advanceSearch(AdvanceSearchRequest request);
	
	ResponseEntity<ApiResponseDto> updateByTicketNumber(List<TicketNumbersDto> ticketNumbersDto);

	ResponseEntity<CountResponseDTO> getAdvanceSearchFields(AdvanceSearchFieldRequest request);
	ResponseEntity<ApiResponseDto> cancelByTicketNumber(List<TicketNumbersDto> ticketNumbersDto);

	ResponseEntity<ApiResponseDto> transferTicketByIds(TransferTicketDTO transferTicketByIds);

	ResponseEntity<CountResponseDTO> getTicketDetailsBySupervisorId(TicketDetailsBySupervisorIdRequest request);
	ResponseEntity<ApiResponseDto> generateTechnicianHierarchy();

	ResponseEntity<ApiResponseDto> getUserHierarchy();

	ResponseEntity<ApiResponseDto> getTotalUnAssignedAssignedAndComletedCountBySupervisorId(supervisorCountRequest request);

	ResponseEntity<ApiResponseDto> cockpitBubbleStatCount(CockpitBubbleCountStatRequest request);
	
	ResponseEntity<ApiResponseDto> fetchOpenAndCompletedStatusCountById(FetchCountById fetchCountById, String businessId);

	ResponseEntity<ApiResponseDto> fetchLocationFromAssignmentSolByTechId(TechnicianIdDto technicianIds,
			String businessId);

	ResponseEntity<CountResponseDTO> getAssistTicketByMasterExternalId(TicketDetailsByMasterExternalIdRequest request);

	ResponseEntity<CountResponseDTO> getTicketDetialsByglobalStatusForReports(
			TechnicianDetailsDTO technicianDetailsDTO);

	ResponseEntity<ResponsePageDto> ticketDCSolverGlobalSearchDTO(TicketDCSolverGlobalSearchDTO searchText);

	ResponseEntity<CountResponseDTO> getAllSupervisorDetails(TechnicianDetailsDTO technicianDetailsDTO);

	//List<SupervisorPolygonMapping> fetchAllSupervisors();

	

	List<SupervisorPolygonMapping> fetchAllSupervisors(SupervisorPolygonMapping supervisorPolygonMapping);

	
	
	

	

	
	
	
}
