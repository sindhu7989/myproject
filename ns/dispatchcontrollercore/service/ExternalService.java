package com.straviso.ns.dispatchcontrollercore.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.straviso.ns.dispatchcontrollercore.dto.AgentAndSupervisorRequestDTO;
import com.straviso.ns.dispatchcontrollercore.dto.CallTechnicianDto;
import com.straviso.ns.dispatchcontrollercore.dto.GetTicketTrailsCollectionDTO;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
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
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.entity.TechnicianAvailability;

public interface ExternalService {

	ResponseEntity<ResponseDTO> sendTicketDetailsToDC(ExternalTicketRequestDTO request);

	ResponseEntity<ResponseDTO> addTechnicianDetailsToDC(Agent agent);
	
	ResponseEntity<ApiResponseDto> getUnassignedTicketCounts();

	ResponseEntity<ResponseDTO> addSupervisorDetailsToDC(SupervisorPolygonMapping request);
	
	ResponseEntity<ApiResponseDto> updateByTicketStatus(List<UpdateTicketNumbersDto> ticketNumbersDto);

	ResponseEntity<ApiResponseDto> callTechnician(CallTechnicianDto callTechnicianDto);

	ResponseEntity<ExternalAppResponse> myTeamWorkload(MyTeamWorkloadRequest myTeamWorkloadRequest);

	ResponseEntity<ApiResponseDto> firstTicketUpdate(firstTicketNumberDto ticketNumberDto);

	ResponseEntity<ApiResponseDto> fetchQueueListBySupervisorId(AgentAndSupervisorRequestDTO agentAndSupervisorRequest);

	ResponseEntity<ResponseDTO> ptoTomorrowFeed(TechnicianAvailabilityRequest request);

	ResponseEntity<ResponseDTO> ptoWeeklyFeed(TechnicianAvailabilityRequest request);

	ResponseEntity<ApiResponseDto> ticketSequenceListByTechId(TechIdDto technicianId, String businessId);

	ResponseEntity<ResponseDTO> updateTechnicianDetailsToDC(Agent requestAgent);

	ResponseEntity<ResponseDTO> updateSupervisorDetailsToDC(SupervisorPolygonMapping request);

	void getEmployeeQualityScoreAndServiceData(String businessId, String businessToken);
	
}
