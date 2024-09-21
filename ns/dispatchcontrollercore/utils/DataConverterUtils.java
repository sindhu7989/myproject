package com.straviso.ns.dispatchcontrollercore.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.straviso.ns.dispatchcontrollercore.constants.ServiceConstants;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketExportDataRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.FileWriterResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianAssignmentResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketDataResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketTechnicianResponseData;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.request.ExportMetaDataUpdateRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DataConverterUtils {


	public FileWriterResponse convertFileDetails(FileWriterResponse fileWriterResponse) {
		FileWriterResponse response = new FileWriterResponse();
		if(!StringUtils.isEmpty(fileWriterResponse.getFilePath())) {
			response.setFilePath(StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_PATH)+fileWriterResponse.getFilePath());

		}else {
			response.setFilePath(fileWriterResponse.getFilePath());
		}		
		response.setFileSize(fileWriterResponse.getFileSize());
		response.setStatusCode(fileWriterResponse.getStatusCode());
		if(fileWriterResponse.getStatusCode() == DispatchControllerConstants.RESPONSE_OK_CODE) {
			response.setStatusMessage(DispatchControllerConstants.STATUS_COMPLETED);
		}else if(fileWriterResponse.getStatusCode() == DispatchControllerConstants.RESPONSE_NO_DATA_FOUND_CODE) {
			response.setStatusMessage(DispatchControllerConstants.NO_RECORD);
		}else if(fileWriterResponse.getStatusCode() == DispatchControllerConstants.RESPONSE_FILE_TOO_LARGE_CODE) {
			response.setStatusMessage(DispatchControllerConstants.FILE_TOO_LARGE);
		}else {
			response.setStatusMessage(DispatchControllerConstants.FAILED);
		}
		return response;
	}

	public ExportMetaDataUpdateRequest convertExportRequestToSaveMetaDataRequest(TicketExportDataRequest exportDataRequest,FileWriterResponse fileWriterResponse) {
		ExportMetaDataUpdateRequest exportMetaDataUpdateRequest = new ExportMetaDataUpdateRequest();		
		exportMetaDataUpdateRequest.setId(exportDataRequest.getId());
		exportMetaDataUpdateRequest.setServiceCode(DispatchControllerConstants.SERVICE_CODE_103);
		exportMetaDataUpdateRequest.setServiceName(DispatchControllerConstants.SERVICE_CODE_103_MESSAGE);		
		exportMetaDataUpdateRequest.setComponentName(exportDataRequest.getComponentName());
		exportMetaDataUpdateRequest.setReportName(exportDataRequest.getReportName());
		exportMetaDataUpdateRequest.setUserId(exportDataRequest.getUserId());			
		exportMetaDataUpdateRequest.setFileName(exportDataRequest.getFileName());
		exportMetaDataUpdateRequest.setFilePath(fileWriterResponse.getFilePath());		
		exportMetaDataUpdateRequest.setSize(fileWriterResponse.getFileSize());
		if(fileWriterResponse.getStatusCode() == DispatchControllerConstants.RESPONSE_OK_CODE) {
			exportMetaDataUpdateRequest.setStatus(DispatchControllerConstants.STATUS_COMPLETED);
		}else if(fileWriterResponse.getStatusCode() == DispatchControllerConstants.RESPONSE_NO_DATA_FOUND_CODE) {
			exportMetaDataUpdateRequest.setStatus(DispatchControllerConstants.NO_RECORD);
		}else if(fileWriterResponse.getStatusCode() == DispatchControllerConstants.RESPONSE_FILE_TOO_LARGE_CODE) {
			exportMetaDataUpdateRequest.setStatus(DispatchControllerConstants.FILE_TOO_LARGE);
		}else {
			exportMetaDataUpdateRequest.setStatus(DispatchControllerConstants.FAILED);
		}
		log.info("exportDataRequest : {} ,fileWriterResponse {}, Converted Data {}",exportDataRequest,fileWriterResponse,exportMetaDataUpdateRequest);
		return exportMetaDataUpdateRequest;
	}

	public List<String> convertTicketExportDataToList(TechnicianAssignmentResponse data, String timeZone, DateTimeFormatter dtf) {


		List<String> strList = new  ArrayList<>();

		strList.add(String.valueOf(""));
		strList.add(String.valueOf(data.getTicketNumber()));
		strList.add(String.valueOf(data.getTicketType()));
		strList.add(String.valueOf(data.getWorkType()));
		strList.add(String.valueOf(data.getTicketScore()));
		strList.add(String.valueOf(data.getTechnicianScore()));
		strList.add(String.valueOf(data.getEstTicketDuration()));
		strList.add(String.valueOf(data.getEmergency()));
		strList.add(String.valueOf(data.getAssignmentDateTime()));
		strList.add(String.valueOf(data.getDueDate()));
		strList.add(String.valueOf(data.getClosedDate()));
		strList.add(String.valueOf(data.getTechnicianName()));
		strList.add(String.valueOf(data.getUserId()));
		strList.add(String.valueOf(""));
		strList.add(String.valueOf(data.getSupervisorName()));
		strList.add(String.valueOf(""));
		strList.add(String.valueOf(""));
		strList.add(String.valueOf(data.getState()));
		strList.add(String.valueOf(data.getReceivedDate()));
		strList.add(String.valueOf(data.getCity()));
		strList.add(String.valueOf(data.getIndex()));
		strList.add(String.valueOf(""));
		strList.add(String.valueOf(data.getLatitude()));
		strList.add(String.valueOf(data.getLongitude()));
		strList.add(String.valueOf(data.getDistance() * 0.000621371));
		strList.add(String.valueOf(data.getAvailableCapacity()));
		strList.add(String.valueOf(data.getAssignmentETA()));
		strList.add(String.valueOf(data.getTicketNumber811()));

		return strList;
	}
	public List<String> convertCockpitTicketExportDataToList(TicketDataResponse data, String timeZone, DateTimeFormatter dtf) {

		List<String> strList = new  ArrayList<>();

		strList.add(String.valueOf(data.getTicketId()));
		strList.add(String.valueOf(data.getWorkType()));
		strList.add(String.valueOf(data.getTicketScore()));
		strList.add(String.valueOf(data.getTimeEstimate()));
		strList.add(String.valueOf(data.getWorkAddress()));
		strList.add(String.valueOf(data.getStreet()));
		strList.add(String.valueOf(data.getWorkZip()));
		strList.add(String.valueOf(data.getWorkCity()));
		strList.add(String.valueOf(data.getWorkState()));
		strList.add(String.valueOf(data.getTicketType()));
		strList.add(String.valueOf(data.getStatus()));
		strList.add(String.valueOf(data.getTechnicianName()));
		strList.add(String.valueOf(data.getTechnicianId()));
		strList.add(String.valueOf(data.getSupervisorName()));
		strList.add(String.valueOf(data.getSupervisorId()));
		strList.add(String.valueOf(data.getDueDate()));
		strList.add(String.valueOf(data.getCreatedDateTime()));
		strList.add(String.valueOf(data.getAssignmentDateTime()));
		strList.add(String.valueOf(data.getTicketNumber811()));

		return strList;
	}


	public List<String> convertTechnicianTicketExportDataToList(TicketDataResponse data, String timeZone, DateTimeFormatter dtf) {

		List<String> strList = new  ArrayList<>();

		strList.add(String.valueOf(data.getTicketId()));
		strList.add(String.valueOf(data.getTicketType()));
		strList.add(String.valueOf(data.getWorkType()));
		strList.add(String.valueOf(data.getTicketScore()));
		strList.add(String.valueOf(data.getTimeEstimate()));
		strList.add(String.valueOf(data.getWorkAddress()));
		strList.add(String.valueOf(data.getStreet()));
		strList.add(String.valueOf(data.getWorkZip()));
		strList.add(String.valueOf(data.getWorkCity()));
		strList.add(String.valueOf(data.getWorkState()));

		strList.add(String.valueOf(data.getStatus()));
		strList.add(String.valueOf(data.getTechnicianName()));
		strList.add(String.valueOf(data.getTechnicianId()));
		strList.add(String.valueOf(data.getSupervisorName()));
		strList.add(String.valueOf(data.getSupervisorId()));
		strList.add(String.valueOf(data.getDueDate()));
		strList.add(String.valueOf(data.getCreatedDateTime()));
		strList.add(String.valueOf(data.getAssignmentDateTime()));
		strList.add(String.valueOf(data.getTicketNumber811()));

		return strList;
	}


	public List<String> convertConsolidatedTicketExportDataToList(TicketTechnicianResponseData data, String timeZone, DateTimeFormatter dtf) {

		List<String> strList = new  ArrayList<>();

		strList.add(String.valueOf(data.getTicketId()));
		strList.add(String.valueOf(data.getTicketType()));
		strList.add(String.valueOf(data.getWorkType()));
		strList.add(String.valueOf(data.getTicketScore()));
		strList.add(String.valueOf(data.getStatus()));
		strList.add("");
		strList.add(String.valueOf(data.getEstTicketDuration()));
		strList.add(String.valueOf(data.getEmergency()));
		strList.add(String.valueOf(data.getCreatedDateTime()));
		strList.add(String.valueOf(data.getAssignmentDateTime()));
		strList.add(String.valueOf(data.getDueDate()));
		strList.add(String.valueOf(data.getClosedDate()));
		strList.add(String.valueOf(data.getReceivedDate()));
		strList.add(String.valueOf(data.getTechnicianId()));
		strList.add(String.valueOf(data.getTechnicianName()));
		strList.add("");
		strList.add("");
		strList.add(String.valueOf(data.getSupervisorId()));
		strList.add(String.valueOf(data.getSupervisorName()));
		strList.add("");
		strList.add("");
		strList.add(String.valueOf(data.getWorkState()));
		strList.add(String.valueOf(data.getWorkCity()));
		strList.add("");
		strList.add("");
		strList.add(String.valueOf(data.getStartLat()));
		strList.add(String.valueOf(data.getStartLon()));
		strList.add("");
		strList.add("");
		strList.add("");
		strList.add(String.valueOf(data.getTicketNumber811()));
		strList.add("");
		strList.add(String.valueOf(data.getTicketPriority()));
		strList.add("");
		strList.add("");
		strList.add("");

		//	strList.add(String.valueOf(data.getWorkAddress()));
		//	strList.add(String.valueOf(data.getStreet()));
		//	strList.add(String.valueOf(data.getWorkZip()));

		return strList;
	}
	
	public List<String> convertConsolidatedSupervisorExportDataToList(TicketTechnicianResponseData data, String timeZone, DateTimeFormatter dtf) {

		List<String> strList = new  ArrayList<>();

		strList.add(String.valueOf(data.getTicketId()));
		strList.add(String.valueOf(data.getTicketType()));
		strList.add(String.valueOf(data.getWorkType()));
		strList.add(String.valueOf(data.getTicketScore()));
		strList.add(String.valueOf(data.getStatus()));
		strList.add("");
		strList.add(String.valueOf(data.getEstTicketDuration()));
		strList.add(String.valueOf(data.getEmergency()));
		strList.add(String.valueOf(data.getCreatedDateTime()));
		strList.add(String.valueOf(data.getAssignmentDateTime()));
		strList.add(String.valueOf(data.getDueDate()));
		strList.add(String.valueOf(data.getClosedDate()));
		strList.add(String.valueOf(data.getReceivedDate()));
		strList.add(String.valueOf(data.getTechnicianId()));
		strList.add(String.valueOf(data.getTechnicianName()));
		strList.add(String.valueOf(data.getUserId()));
		strList.add(String.valueOf(data.getEmployeeId()));
		strList.add(String.valueOf(data.getSupervisorId()));
		strList.add(String.valueOf(data.getSupervisorName()));
		strList.add("");
		strList.add("");
		strList.add(String.valueOf(data.getWorkState()));
		strList.add(String.valueOf(data.getWorkCity()));
		strList.add(String.valueOf(data.getRouteIndex()));
		strList.add("");
		strList.add(String.valueOf(data.getStartLat()));
		strList.add(String.valueOf(data.getStartLon()));
		strList.add(String.valueOf(data.getEvalMiles()));
		strList.add(String.valueOf(data.getAvailableCapacity()));
		strList.add(String.valueOf(data.getAssignmentEta()));
		strList.add(String.valueOf(data.getTicketNumber811()));
		strList.add(String.valueOf(data.getSkill()));
		strList.add(String.valueOf(data.getTicketPriority()));
		strList.add(String.valueOf(data.getDriveTime()));
		strList.add(String.valueOf(data.getTotalDriveTime()));
		strList.add(String.valueOf(data.getDistance()));

		//	strList.add(String.valueOf(data.getWorkAddress()));
		//	strList.add(String.valueOf(data.getStreet()));
		//	strList.add(String.valueOf(data.getWorkZip()));

		return strList;
	}

}
