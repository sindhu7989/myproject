package com.straviso.ns.dispatchcontrollercore.serviceImpl;

import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.TicketCountReportsIpDto;
import com.straviso.ns.dispatchcontrollercore.dto.request.DashboardRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.CountDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.Data;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.StateCountData;
import com.straviso.ns.dispatchcontrollercore.dto.response.StateCountResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketCount;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketCountResponse;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.repository.AgentRepository;
import com.straviso.ns.dispatchcontrollercore.repository.TicketDataRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.service.DashboardService;
import com.straviso.ns.dispatchcontrollercore.utils.DispatchControllerSupportUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DashboardServiceImpl implements DashboardService {

	@Autowired
	private DispatchControllerSupportUtils dispatchControllerSupportUtils;

	@Autowired
	TicketRepository ticketRepo;
	
	@Autowired
    TicketDataRepo ticketDataRepo;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	AgentRepository agentRepository;

	@Override
	public ResponseEntity<ResponseDTO> getTicketCountStatByStatus(DashboardRequest request) {

		String logKey = DispatchControllerConstants.GET_TICKET_COUNT_BY_STATUS_REQUEST;

		try {

			if (request == null || StringUtils.isEmpty(request.getStartDate())
					|| StringUtils.isEmpty(request.getEndDate())) {
				log.info("{} Bad Request received where request is {} , for businessId : {}", logKey, request,
						BusinessContext.getTenantId());
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),
						DispatchControllerConstants.RESPONSE_BAD_REQUEST, request, HttpStatus.OK);
			}

			List<Map<String, Map<String, Integer>>> response = ticketRepo.getTicketStatusCountByMonth(request);

			Object responseData = response;

			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),
					DispatchControllerConstants.RESPONSE_OK, responseData, HttpStatus.OK);

		} catch (Exception e) {
			log.info("{} Unable to get Ticket Count Details, for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@Override
	public ApiResponseDto ticketFetchCount(TicketCountReportsIpDto ticketCountReportsIpDto) {

		TicketCountResponse countResponse = new TicketCountResponse();
		ApiResponseDto apiResponseDto = new ApiResponseDto();

		try {
			if (ticketCountReportsIpDto != null) {
				String fromDate = ticketCountReportsIpDto.getStartDate();
				String toDate = ticketCountReportsIpDto.getEndDate();
				long completedCount = 0;
				long unassignedCount = 0;
				long missingInfoCount = 0;
				if (StringUtils.isNotEmpty(ticketCountReportsIpDto.getGlobalStatus())) {
					if (DispatchControllerConstants.STATUS_COMPLETED
							.equalsIgnoreCase(ticketCountReportsIpDto.getGlobalStatus())) {
						completedCount = countByStatus(DispatchControllerConstants.STATUS_COMPLETED,
								ticketCountReportsIpDto);
					} else if (DispatchControllerConstants.STATUS_UNASSIGNED
							.equalsIgnoreCase(ticketCountReportsIpDto.getGlobalStatus())) {
						unassignedCount = countByStatus(DispatchControllerConstants.STATUS_UNASSIGNED,
								ticketCountReportsIpDto);
					} else if (DispatchControllerConstants.STATUS_MISSING_INFO
							.equalsIgnoreCase(ticketCountReportsIpDto.getGlobalStatus())) {
						missingInfoCount = countByStatus(DispatchControllerConstants.STATUS_MISSING_INFO,
								ticketCountReportsIpDto);
					}
				} else {
					completedCount = countByStatus(DispatchControllerConstants.STATUS_COMPLETED,
							ticketCountReportsIpDto);
					unassignedCount = countByStatus(DispatchControllerConstants.STATUS_UNASSIGNED,
							ticketCountReportsIpDto);
					missingInfoCount = countByStatus(DispatchControllerConstants.STATUS_MISSING_INFO,
							ticketCountReportsIpDto);
				}

				countResponse.setCompleted(completedCount);
				countResponse.setUnassigned(unassignedCount);
				countResponse.setMissingInfo(missingInfoCount);

				long totalCount = completedCount + unassignedCount + missingInfoCount;
				countResponse.setTotalCount(totalCount);

				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				apiResponseDto.setResponseData(countResponse);

			} else {
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
				apiResponseDto.setMessage("Unable to execute Query");

			}

		} catch (Exception e) {
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
			apiResponseDto.setMessage("Unable to execute Query");
		}

		return apiResponseDto;

	}

	private long countByStatus(String globalStatus, TicketCountReportsIpDto ticketCountReportsIpDto) {
		Query query = new Query();

		if (StringUtils.isNotEmpty(ticketCountReportsIpDto.getStartDate())
				&& StringUtils.isNotEmpty(ticketCountReportsIpDto.getEndDate())) {

			DateTimeFormatter formatter = DateTimeFormatter
					.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDateTime = LocalDateTime.parse(ticketCountReportsIpDto.getStartDate(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(ticketCountReportsIpDto.getEndDate(), formatter);

			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_CREATED_DATETIME).gte(startDateTime)
					.lte(endDateTime));
		}

		query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(globalStatus));

		return mongoTemplate.count(query, DispatchControllerConstants.TICKET_COLLECTION);
	}

	@Override
	public ResponseEntity<ResponseDTO> getTicketCountStatByAction(DashboardRequest request) {
		String logKey = DispatchControllerConstants.GET_TICKET_COUNT_BY_ACTION_REQUEST;

		try {

			if (request == null || StringUtils.isEmpty(request.getStartDate())
					|| StringUtils.isEmpty(request.getEndDate())) {
				log.info("{} Bad Request received where request is {} , for businessId : {}", logKey, request,
						BusinessContext.getTenantId());
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),
						DispatchControllerConstants.RESPONSE_BAD_REQUEST, request, HttpStatus.OK);
			}

			List<Map<String, Map<String, Integer>>> response = ticketRepo.getTicketActionCountByMonth(request);

			Object responseData = response;

			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),
					DispatchControllerConstants.RESPONSE_OK, responseData, HttpStatus.OK);

		} catch (Exception e) {
			log.info("{} Unable to get Ticket By Action Count Details, for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@Override
	public ResponseEntity<ResponseDTO> getTopTechnicianCount(ToptechnicianCountRequest request) {

		String logKey = DispatchControllerConstants.GET_TOP_TECHNICIAN_COUNT_REQUEST;
		try {
			String fieldname = request.getFieldName();
			if (StringUtils.isEmpty(request.getFieldName())) {
				fieldname = DispatchControllerConstants.FIELD_CITY;
			}

			Object response = agentRepository.getTopTechnicianDataByFieldName(request);

			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),
					DispatchControllerConstants.RESPONSE_OK, response, HttpStatus.OK);

		} catch (Exception e) {
			log.info("{} Unable to get Ticket By Action Count Details, for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<ResponseDTO> getTopTicketCount(ToptechnicianCountRequest request) {
		try {

			Object response = ticketRepo.getTopTicketCount(request);

			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),
					DispatchControllerConstants.RESPONSE_OK, response, HttpStatus.OK);
		} catch (Exception e) {
			log.info("{} Unable to get Ticket By Action Count Details, for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	public ResponseEntity<ResponseDTO> getTechnicianCount() {

		String logKey = DispatchControllerConstants.GET_TECHNICIAN_COUNT_REQUEST;
		try {

			Object count = agentRepository.getTechnicianCountByStatus();

			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),
					DispatchControllerConstants.RESPONSE_OK, count, HttpStatus.OK);

		} catch (Exception e) {
			log.info("{} Unable to get Technician Count Details, for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
					DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<ApiResponseDto> getTotalManagerCount() {
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		Map<String, Object> response = new HashMap<>();
		try {

			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where(DispatchControllerConstants.DESIGNATION)
							.is(DispatchControllerConstants.MANAGER)),
					Aggregation.unwind(DispatchControllerConstants.USER_HIERARCHY),
					Aggregation.group(DispatchControllerConstants.FIELD_ID).count()
							.as(DispatchControllerConstants.WORD_COUNT),
					Aggregation.project(DispatchControllerConstants.WORD_COUNT)
							.and(DispatchControllerConstants.FIELD_ID).previousOperation());

			AggregationResults<CountDTO> results = mongoTemplate.aggregate(aggregation,
					DispatchControllerConstants.USER_HIERARCHY, CountDTO.class);
			int count = 0;
			count = results.getMappedResults().get(0).getCount();
			response.put(DispatchControllerConstants.WORD_TOTAL, count);

			apiResponseDto.setResponseData(response);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		} catch (Exception e) {
			log.info("Unable to get Manager count");
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			response.put(DispatchControllerConstants.WORD_TOTAL, 0);
			apiResponseDto.setResponseData(response);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<ApiResponseDto> getTotalSupervisorCount() {
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		Map<String, Object> response = new HashMap<>();
		try {

			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where(DispatchControllerConstants.DESIGNATION)
							.is(DispatchControllerConstants.SUPERVISOR)),
					Aggregation.unwind(DispatchControllerConstants.USER_HIERARCHY),
					Aggregation.group(DispatchControllerConstants.FIELD_ID).count()
							.as(DispatchControllerConstants.WORD_COUNT),
					Aggregation.project(DispatchControllerConstants.WORD_COUNT)
							.and(DispatchControllerConstants.FIELD_ID).previousOperation());

			AggregationResults<CountDTO> results = mongoTemplate.aggregate(aggregation,
					DispatchControllerConstants.USER_HIERARCHY, CountDTO.class);
			int count = results.getMappedResults().get(0).getCount();

			response.put(DispatchControllerConstants.WORD_TOTAL, count);

			apiResponseDto.setResponseData(response);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		} catch (Exception e) {
			log.info("Unable to get Supervisor count");
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			response.put(DispatchControllerConstants.WORD_TOTAL, 0);
			apiResponseDto.setResponseData(response);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		}
	}
	
	@Override
	public ResponseEntity<ResponseDTO> getStateWiseTicketCount(DashboardRequest request) {
	    try {
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
	        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate() + DispatchControllerConstants.START_DATETIME_APPEND, formatter);
	        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate() + DispatchControllerConstants.END_DATETIME_APPEND, formatter);

	        Aggregation aggregation = Aggregation.newAggregation(
	                Aggregation.match(Criteria.where(DispatchControllerConstants.CREATEDDATETIME)
	                        .gte(startDate).lte(endDate)
	                        .and(DispatchControllerConstants.WORKSTATE).ne(null)
	                        .and(DispatchControllerConstants.WORKCOUNTY).ne(null)
	                        .and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).ne("Cancelled")),
	                Aggregation.project(DispatchControllerConstants.WORKSTATE, DispatchControllerConstants.WORKCOUNTY)
	        );

	        // Execute Aggregation pipeline and retrieve results
	        List<TicketCount> tickets = mongoTemplate.aggregate(aggregation, DispatchControllerConstants.TICKET_COLLECTION, TicketCount.class).getMappedResults();

	        List<StateCountResponse> stateCountResponseList = new ArrayList<>();
	        
	        if (tickets != null && !tickets.isEmpty()) {
	            // Use to check the count based on state
	            LinkedHashMap<String, Long> workStateCount = tickets.stream().collect(Collectors.groupingBy(
	                    data -> data.getWorkState(), LinkedHashMap::new, Collectors.counting()));
	            workStateCount.entrySet().forEach(x -> {
	                List<Data> listData = new ArrayList<>();
	                List<StateCountData> listStateCountData = new ArrayList<>();
	                StateCountData stateCountData = new StateCountData();
	                StateCountResponse stateCountResponse = new StateCountResponse();

	                // Group tickets by work state and county and count occurrences
	                LinkedHashMap<String, Long> workCountyCount = tickets.stream().collect(Collectors.groupingBy(data ->
	                        data.getWorkState().equals(x.getKey()) && data.getWorkCounty() != null ?
	                                data.getWorkCounty() : "", LinkedHashMap::new, Collectors.counting()));
	                workCountyCount.entrySet().forEach(county -> {
	                    if (county.getKey() != null && !county.getKey().isEmpty()) {
	                        Data countyData = new Data();
	                        countyData.setName(county.getKey());
	                        countyData.setValue(county.getValue());
	                        listData.add(countyData);
	                    }
	                });

	                // Prepare state count data
	                stateCountData.setName(x.getKey());
	                stateCountData.setDrilldown(x.getKey());
	                stateCountData.setValue(x.getValue());
	                stateCountData.setData(listData);
	                listStateCountData.add(stateCountData);

	                // Prepare state count response
	                stateCountResponse.setData(listStateCountData);
	                stateCountResponseList.add(stateCountResponse);
	            });

	            return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),
	                    DispatchControllerConstants.RESPONSE_OK, stateCountResponseList, HttpStatus.OK);
	        } else {
	            return dispatchControllerSupportUtils.generateResponse(HttpStatus.NOT_FOUND.value(),
	                    DispatchControllerConstants.RESPONSE_NOT_FOUND,new ArrayList<>(), HttpStatus.OK);
	        }
	    } catch (Exception e) {
	        log.info("{} Unable to get the count due to {} ", DispatchControllerConstants.GET_TICKET_COUNT_BY_STATEWISE_REQUEST, e.getMessage());
	        return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
	                DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	


	
	

}
