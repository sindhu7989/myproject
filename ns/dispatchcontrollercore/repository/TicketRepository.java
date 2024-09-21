package com.straviso.ns.dispatchcontrollercore.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.straviso.ns.dispatchcontrollercore.dto.GetTicketTrailsCollectionDTO;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketTrailsCollectionDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchFieldRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.CockpitBubbleCountStatRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.DashboardRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.CountResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.DataExportFieldResponse;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;

public interface TicketRepository {

	List<Map<String, Map<String, Integer>>> getTicketStatusCountByMonth(DashboardRequest request);
	List<Map<String, Map<String, Integer>>> getTicketActionCountByMonth(DashboardRequest request);
	Object getTopTicketCount(ToptechnicianCountRequest request);
	CountResponseDTO getAdvanceSearchData(AdvanceSearchRequest request);
	Object getAdvancedSearchFields(AdvanceSearchFieldRequest request);
	List<Ticket> findAll();
	void updateLumenTicketCollection(LumenCollectionUpdateDTO lumenCollectionUpdateDTO);
	List<Map<String, String>> getTicketData(String ticketNumber,String conversationId);
	Object getTicketJsonView(String ticketNumber, String conversationId);
	ResponseEntity<ApiResponseDto> saveTicketAuditTrails(TicketTrailsCollectionDTO lumenCollectionUpdateDTO);
	ResponseEntity<ApiResponseDto> getTicketAuditTrails(GetTicketTrailsCollectionDTO lumenCollectionUpdateDTO);
	Object getCockpitBubbleStatCount(CockpitBubbleCountStatRequest request);
	long getSupervisorCountByStatus(String supervisorId, String statusAssigned,
			LocalDateTime startDate, LocalDateTime endDate);
	DataExportFieldResponse getTicketAdvancedSearchFields(AdvanceSearchFieldRequest request);
	DataExportFieldResponse getCockpitTicketAdvancedSearchFields(AdvanceSearchFieldRequest request);

	Page<Ticket> globalSearch(String supervisorId, String globalStatus, String searchText, LocalDateTime startDateTime,
			LocalDateTime endDateTime ,Pageable pageable);
}
