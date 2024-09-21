package com.straviso.ns.dispatchcontrollercore.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.straviso.ns.dispatchcontrollercore.dto.GroupByTransferList;
import com.straviso.ns.dispatchcontrollercore.dto.TransferTicketDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDtoAPK;
import com.straviso.ns.dispatchcontrollercore.dto.response.GroupByActionResponse;


public interface GroupByActionService  {

	ResponseEntity<ApiResponseDto> GroupByAction(GroupByTransferList groupByTransferList);
	ResponseEntity<ApiResponseDtoAPK> GroupByActionAPK(GroupByTransferList groupByTransferList);

}