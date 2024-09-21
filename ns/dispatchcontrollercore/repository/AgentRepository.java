package com.straviso.ns.dispatchcontrollercore.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.straviso.ns.dispatchcontrollercore.dto.MyTeamWorkloadTechnicianDetails;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketExportDataRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.AgentListResponse;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;


public interface AgentRepository {

	Object getTopTechnicianDataByFieldName(ToptechnicianCountRequest request);

	Object getTechnicianCountByStatus();

	Object getTechnicianJsonView(String technicianId);
	
	MyTeamWorkloadTechnicianDetails getTechnicianTicketCount(Agent technician, LocalDateTime startDate, LocalDateTime endDate);

	Integer getTicketCountByCriteria(TicketExportDataRequest request);

	List<AgentAssignmentSolutionModel> getTechnicianListData(TicketExportDataRequest request, Integer pageNumberLoop);

	Integer getCockpitTicketCountByCriteria(TicketExportDataRequest request);

	List<Ticket> getTicketListData(TicketExportDataRequest request, Integer pageNumberLoop);

	AgentListResponse findAllData(Pageable pageable, String isActive);

	AgentListResponse findAllSupervisorData(Pageable pageable, String isActive);

	

	
	

}
