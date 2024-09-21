package com.straviso.ns.dispatchcontrollercore.service;

import org.springframework.http.ResponseEntity;

import com.straviso.ns.dispatchcontrollercore.dto.ExportTicketResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketExportDataRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.DataExportFieldResponse;

public interface ExportDataService {

	ResponseEntity<ExportTicketResponseDTO> exportTicketData(TicketExportDataRequest request, String businessId);

	DataExportFieldResponse getDataExportAdvancedSearchFields();

	ResponseEntity<ExportTicketResponseDTO> exportCockpitTicketData(TicketExportDataRequest request, String businessId);

	DataExportFieldResponse getDataExportCockpitAdvancedSearchFields();

	ResponseEntity<ExportTicketResponseDTO> exportTicketTransactionData(TicketExportDataRequest request,
			String businessId);

}
