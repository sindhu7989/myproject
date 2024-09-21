package com.straviso.ns.dispatchcontrollercore.serviceImpl;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.controller.DispatchCockpitController;
import com.straviso.ns.dispatchcontrollercore.dto.GroupByTransferList;
import com.straviso.ns.dispatchcontrollercore.dto.TransferTicketDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDtoAPK;
import com.straviso.ns.dispatchcontrollercore.dto.response.GroupByActionResponse;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.service.GroupByActionService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class GroupByActionServiceImpl implements GroupByActionService {

	@Autowired
	CockpitServiceImpl cockpitServiceImpl;

	ExecutorService executor = Executors.newFixedThreadPool(50);

	private static Logger logger = LoggerFactory.getLogger(GroupByActionServiceImpl.class);

	@Override
	public ResponseEntity<ApiResponseDto> GroupByAction(GroupByTransferList groupByTransferList) {
		GroupByActionResponse groupByActionResponse = new GroupByActionResponse();
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		
		String logKey = DispatchControllerConstants.TRANSFER_IN_DC;
		String businessId = BusinessContext.getTenantId();
		
		try {

			if (!CollectionUtils.isEmpty(groupByTransferList.getGroupByTransferList())) {
				for (TransferTicketDTO transferTicket : groupByTransferList.getGroupByTransferList()) {
					logger.info("## DC transferTicket: ** " + transferTicket);
				
				if (!ObjectUtils.isEmpty(transferTicket.getFromSupervisorId()) && !ObjectUtils.isEmpty(transferTicket.getToSupervisorId())) {
						logger.info("## DC TransferTicketDTO Request is for Supervisor's: ** " + transferTicket);
						
						ResponseEntity<ApiResponseDto> responseEntity =cockpitServiceImpl.transferTicketBySupervisorIds(transferTicket);
						
						String responseValue= responseEntity.getBody().getMessage();
						String responseKey=String.valueOf(transferTicket.getTicketNumbers());
						groupByActionResponse.getResponse().put(responseKey, responseValue);
				}
				//else loop to check transfer is between Technicians or Supervisor
				//if(!ObjectUtils.isEmpty(transferTicket.getToTechnicianId()) || !ObjectUtils.isEmpty(transferTicket.getFromTechnicianId()))
				else
				{
					//If loop..If from and to technician are same then retuen both are same.
					if(transferTicket.getFromTechnicianId().equalsIgnoreCase(transferTicket.getToTechnicianId()))
					{
						logger.info("## DC FROM and To Technician are same: ** " +transferTicket.getFromTechnicianId(),transferTicket.getToTechnicianId());
						
						String responseValue=DispatchControllerConstants.STATUS_FROM_AND_TO_TECHNICIAN_ARE_SAME;
						String responseKey=String.valueOf(transferTicket.getTicketNumbers());
						groupByActionResponse.getResponse().put(responseKey, responseValue);
						
					}
					else
					{
						logger.info("## DC TransferTicketDTO Request is for Technician's: ** " + transferTicket);
						
						ResponseEntity<ApiResponseDto> responseEntity =cockpitServiceImpl.transferTicketByIds(transferTicket);
						
						String responseValue= responseEntity.getBody().getMessage();
						String responseKey=String.valueOf(transferTicket.getTicketNumbers());
						groupByActionResponse.getResponse().put(responseKey, responseValue);
					}
					
				}
				//else if (!ObjectUtils.isEmpty(transferTicket.getFromSupervisorId()) || !ObjectUtils.isEmpty(transferTicket.getToSupervisorId())) {
					
				}
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			}
			else
			{
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
			}
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);

		} catch (Exception e) {
			log.info(" {} DC : Unable to Transfer Ticket , for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			groupByActionResponse.getResponse().put(DispatchControllerConstants.STATUS_FAILED,DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}

	}
	
	@Override
	public ResponseEntity<ApiResponseDtoAPK> GroupByActionAPK(GroupByTransferList groupByTransferList) {
		GroupByActionResponse groupByActionResponse = new GroupByActionResponse();
		ApiResponseDtoAPK apiResponseDto = new ApiResponseDtoAPK();
		
		String logKey = DispatchControllerConstants.TRANSFER_IN_APK;
		String businessId = BusinessContext.getTenantId();
		
		try {

			if (!CollectionUtils.isEmpty(groupByTransferList.getGroupByTransferList())) {
				for (TransferTicketDTO transferTicket : groupByTransferList.getGroupByTransferList()) {
					logger.info("## transferTicket APK: ** " + transferTicket);
				
				if (!ObjectUtils.isEmpty(transferTicket.getFromSupervisorId()) && !ObjectUtils.isEmpty(transferTicket.getToSupervisorId())) {
						logger.info("## APK TransferTicketDTO Request is for Supervisor's: ** " + transferTicket);
						log.debug("{} Transfer between two  Sup's :Start {} and businessId: {} ",DispatchControllerConstants.TRANSFER_IN_APK,transferTicket,businessId);
						ResponseEntity<ApiResponseDto> responseEntity =cockpitServiceImpl.transferTicketBySupervisorIds(transferTicket);
						log.debug("{} Transfer between two  Sup's :End {} and businessId: {} ",DispatchControllerConstants.TRANSFER_IN_APK,transferTicket,businessId);
						
						String responseValue= responseEntity.getBody().getMessage();
						String responseKey=String.valueOf(transferTicket.getTicketNumbers());
						groupByActionResponse.getResponse().put(responseKey, responseValue);
						
				}
				//else loop to check transfer is between Technicians or Supervisor
				//if(!ObjectUtils.isEmpty(transferTicket.getToTechnicianId()) || !ObjectUtils.isEmpty(transferTicket.getFromTechnicianId()))
				else
				{
					//If loop..If from and to technician are same then retuen both are same.
					if(transferTicket.getFromTechnicianId().equalsIgnoreCase(transferTicket.getToTechnicianId()))
					{
						logger.info("##  APK : FROM and To Technician are same: ** " +transferTicket.getFromTechnicianId(),transferTicket.getToTechnicianId());
						
						String responseValue=DispatchControllerConstants.STATUS_FROM_AND_TO_TECHNICIAN_ARE_SAME;
						String responseKey=String.valueOf(transferTicket.getTicketNumbers());
						groupByActionResponse.getResponse().put(responseKey, responseValue);
						
					}
					else
					{
						logger.info("## APK : TransferTicketDTO Request is for Technician's: ** " + transferTicket);
						log.debug("{} Transfer between two Tech's :Start {} and businessId: {} ",DispatchControllerConstants.TRANSFER_IN_APK,transferTicket,businessId);
						ResponseEntity<ApiResponseDto> responseEntity =cockpitServiceImpl.transferTicketByIds(transferTicket);
						log.debug("{} Transfer between two Tech's :End {} and businessId: {} ",DispatchControllerConstants.TRANSFER_IN_APK,transferTicket,businessId);
						
						String responseValue= responseEntity.getBody().getMessage();
						String responseKey=String.valueOf(transferTicket.getTicketNumbers());
						groupByActionResponse.getResponse().put(responseKey, responseValue);
					
					}
					
				}
				//else if (!ObjectUtils.isEmpty(transferTicket.getFromSupervisorId()) || !ObjectUtils.isEmpty(transferTicket.getToSupervisorId())) {
					
				}
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setResponseData(groupByActionResponse.getResponse());
				
				if (groupByActionResponse.getResponse().containsValue(DispatchControllerConstants.STATUS_SUCCESS)  && groupByActionResponse.getResponse().containsValue(DispatchControllerConstants.STATUS_FAILED))
				{
					apiResponseDto.setResponseMessage(DispatchControllerConstants.STATUS_PARTIAL_SUCCESS);
				}
				else if(groupByActionResponse.getResponse().containsValue(DispatchControllerConstants.STATUS_SUCCESS))
				{
					apiResponseDto.setResponseMessage(DispatchControllerConstants.STATUS_SUCCESS);
				}
				else
				{
					apiResponseDto.setResponseMessage(DispatchControllerConstants.STATUS_FAILED);
				}
				
				
				
			}
			else
			{
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
			}
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);

		} catch (Exception e) {
			log.info(" {} APK : Unable to Transfer Ticket , for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			groupByActionResponse.getResponse().put(DispatchControllerConstants.STATUS_FAILED,DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}

	}
}
