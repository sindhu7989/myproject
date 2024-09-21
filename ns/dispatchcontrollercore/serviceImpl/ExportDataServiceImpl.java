package com.straviso.ns.dispatchcontrollercore.serviceImpl;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.constants.MultiTenantConstants;
import com.straviso.ns.dispatchcontrollercore.constants.ServiceConstants;
import com.straviso.ns.dispatchcontrollercore.dto.ExportTicketResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.NSUserDetailsDTO;
import com.straviso.ns.dispatchcontrollercore.dto.RouteSolverPath;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchFieldRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ColumnFilters;
import com.straviso.ns.dispatchcontrollercore.dto.request.NSUserRoleRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketExportDataRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.DataExportFieldResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.FileWriterResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianAssignmentResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketDataResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketTechnicianResponseData;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.Location;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.repository.AgentRepository;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.service.ExportDataService;
import com.straviso.ns.dispatchcontrollercore.utils.DataConverterUtils;
import com.straviso.ns.dispatchcontrollercore.utils.DispatchControllerSupportUtils;
import com.straviso.ns.dispatchcontrollercore.utils.ExportDataSupportUtils;
import com.straviso.ns.dispatchcontrollercore.utils.ExportDataUtils;

import org.springframework.beans.factory.annotation.Autowired;
import com.straviso.ns.dispatchcontrollercore.utils.StartupApplicationListener;
import org.springframework.beans.factory.annotation.Qualifier;


import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ExportDataServiceImpl  implements ExportDataService{

	private Map<String,NSUserDetailsDTO> nsUserRoleMap = new ConcurrentHashMap<>();

	@Autowired
	ExportDataUtils exportDataUtils;

	@Autowired(required = true)
	@Qualifier(value = "taskExecutor")
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	DataConverterUtils dataConverterUtils;

	@Autowired
	ExportDataSupportUtils exportDataSupportUtils;

	@Autowired
	AgentRepository agentRepository;

	@Autowired
	TicketRepository ticketRepository;
	
	@Autowired
	DispatchControllerSupportUtils dispatchControllerSupportUtils;

	@Override
	public ResponseEntity<ExportTicketResponseDTO> exportTicketData(TicketExportDataRequest request,
			String businessId) {
		String logKey = null;
		String businessToken = BusinessTokenContext.getBusinessToken();
		ExportTicketResponseDTO response = new ExportTicketResponseDTO();
		//		String userId = request.getUserId();
		Integer pageNumber = DispatchControllerConstants.DEFAULT_PAGE_NUMBER;
		Integer pageSize = StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_PAGE_SIZE) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_PAGE_SIZE)) : (DispatchControllerConstants.DEFAULT_PAGE_SIZE) ;
		Integer numberOfThreads = StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_NUMBER_OF_THREADS) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_NUMBER_OF_THREADS)) : (DispatchControllerConstants.DEFAULT_NUMBER_OF_THREADS) ;
		String startDate = request.getStartDate();
		String endDate = request.getEndDate();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT);
		FileWriterResponse fileWriterResponse = new FileWriterResponse();
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Integer maxRecordFetchLimit = StartupApplicationListener.getProperty(ServiceConstants.MAX_RECORD_FETCH_LIMIT) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.MAX_RECORD_FETCH_LIMIT)) : (DispatchControllerConstants.MAX_RECORD_FETCH_LIMIT) ;
		request.setTimeZone(DispatchControllerConstants.TIME_ZONE_CST);
		Map<Integer , FileWriterResponse> threadOperationStatus = new ConcurrentHashMap<>();

		try {

			if(StringUtils.isEmpty(request.getReportName())
					|| StringUtils.isEmpty(request.getUserId())
					|| StringUtils.isEmpty(request.getFileName())
					|| request.getId() == null
					|| request.getId() < 0
					) {
				log.info("{} Incomplete request ,for business {} where request was {} ",logKey,businessId,request);
				response.setStatusCode(HttpStatus.BAD_REQUEST.value());
				response.setStatusMessage(DispatchControllerConstants.RESPONSE_INCOMPLETE_REQUEST);
				return new ResponseEntity<>(response, HttpStatus.OK); 
			}


			if(StringUtils.isEmpty(request.getStartDate()) || StringUtils.isEmpty(request.getEndDate())) {
				log.info("{} Date field missing in request for business {} with request {}",logKey,businessId,request);
				startDate = exportDataUtils.getDateTimeByTimeZone(LocalDateTime.now().minusHours(DispatchControllerConstants.HOURS_INT).toString(),request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
				endDate = exportDataUtils.getDateTimeByTimeZone(LocalDateTime.now().toString(),request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
			}else {
				startDate = StringUtils.replace(request.getStartDate(), " ", DispatchControllerConstants.TIME_FORMAT_T);
				endDate = StringUtils.replace(request.getEndDate(), " ", DispatchControllerConstants.TIME_FORMAT_T);
				startDate = exportDataUtils.getDateTimeByTimeZone(startDate,request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
				endDate = exportDataUtils.getDateTimeByTimeZone(endDate,request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
			}
			log.info("{} Date range applied {} to {} with Timezone {} for business {}",logKey,startDate,endDate,request.getTimeZone(),businessId);
			request.setStartDate(startDate);
			request.setEndDate(endDate);
			request.setPageNumber(pageNumber);
			request.setPageSize(pageSize);
			String[] ticketExportColumns = DispatchControllerConstants.TICKET_EXPORT_COLUMNS;



			Integer ticketCount = agentRepository.getTicketCountByCriteria(request);


			if(ticketCount == null || ticketCount == 0) {
				response.setStatusCode(HttpStatus.OK.value());
				response.setStatusMessage(DispatchControllerConstants.NO_RECORD);
				response.setTotalElements(0L);
				response.setTotalPages(0);
				fileWriterResponse.setFilePath(null);
				fileWriterResponse.setFileSize(0L);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_NO_DATA_FOUND_CODE);

				FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
				threadPoolTaskExecutor.submit(()->{
					exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
				});
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			if(ticketCount > maxRecordFetchLimit) {
				response.setStatusCode(HttpStatus.OK.value());
				response.setStatusMessage(DispatchControllerConstants.FILE_TOO_LARGE);
				response.setTotalElements(0L);
				response.setTotalPages(0);
				fileWriterResponse.setFilePath(null);
				fileWriterResponse.setFileSize(0L);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_FILE_TOO_LARGE_CODE);

				FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
				threadPoolTaskExecutor.submit(()->{
					exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
				});
				return new ResponseEntity<>(response, HttpStatus.OK);
			}



			Integer maxPageCount = ticketCount / pageSize;

			threadPoolTaskExecutor.submit(()->{

				Integer totalPageCount = ticketCount / pageSize;

				if(ticketCount % pageSize != 0) {
					totalPageCount++;
				}
				Integer numberOfRounds = totalPageCount / numberOfThreads ;
				if(totalPageCount % numberOfThreads != 0) {
					numberOfRounds++;
				}
				Integer totalPageCountLoop = totalPageCount ;
				FileWriterResponse fileWriterResponse2 = exportDataUtils.createCSVFileInVolume(request.getReportName(), ticketExportColumns, StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),logKey);
				if(fileWriterResponse2.getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE) {
					log.info("{} error occurred while creating csv file for business {} where request was {} ",logKey,businessId,request);
					response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
					response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
					response.setTotalElements(0L);
					response.setTotalPages(0);
					FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
					threadPoolTaskExecutor.submit(()->{
						exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
					});
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				ThreadPoolExecutor executor = new ThreadPoolExecutor(numberOfThreads,numberOfThreads,0,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());

				for(int r = 1; r <= numberOfRounds; r++) {
					Integer pageCount = 0;
					Integer startPageNo = (r-1) * numberOfThreads;
					Integer endPageNo = totalPageCountLoop;

					if( numberOfThreads > totalPageCountLoop) {
						pageCount = totalPageCountLoop;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = ((r-1) * numberOfThreads) + totalPageCountLoop -1;
					}  else { 
						pageCount = numberOfThreads;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = (r * numberOfThreads) - 1;
					}

					CountDownLatch countDownLatch = new CountDownLatch(pageCount);
					for(int i=startPageNo;i<=endPageNo ; i++ ) {
						Integer pageNumberLoop = i;

						request.setPageNumber(pageNumberLoop);

						executor.execute(()->{
							try {

								RequestContextHolder.setRequestAttributes(requestAttributes);
								BusinessContext.setTenantId(businessId);
								BusinessTokenContext.setBusinessToken(businessToken);
								List<AgentAssignmentSolutionModel> ticketListData = agentRepository.getTechnicianListData(request,pageNumberLoop);

								List<TechnicianAssignmentResponse> techinicianList = getTechnicianTicketData(ticketListData);

								List<Object[]> stringListArray = new ArrayList<>();

								techinicianList.forEach(data ->{
									List<String> strList = dataConverterUtils.convertTicketExportDataToList(data, request.getTimeZone(), dtf);
									stringListArray.add(strList.toArray());
								});

								FileWriterResponse	fileWriterResponseThread = exportDataUtils.exportToCSV(stringListArray, ticketExportColumns,request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),logKey);
								threadOperationStatus.put(pageNumberLoop, fileWriterResponseThread);
							}catch(Exception e) {
								log.info("{} error occurred in Thread, pageNumber {} for business {} where request was {} , exception: {}",logKey,pageNumberLoop,businessId,request,ExceptionUtils.getStackTrace(e));
								FileWriterResponse fileWriterExceptionResponse = new FileWriterResponse();
								fileWriterExceptionResponse.setFilePath(null);
								fileWriterExceptionResponse.setFileSize(0L);
								fileWriterExceptionResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
								threadOperationStatus.put(pageNumberLoop, fileWriterExceptionResponse);
							}
							countDownLatch.countDown();
						});				
					}			
					totalPageCountLoop = totalPageCountLoop - numberOfThreads;
					countDownLatch.await();
				}

				executor.shutdown();

				for(Integer threadOperationStatusKey : threadOperationStatus.keySet()) {

					if(threadOperationStatus.get(threadOperationStatusKey).getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE )  {
						response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
						response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
						response.setTotalElements(0L);
						response.setTotalPages(0);
						fileWriterResponse2.setFilePath(null);
						fileWriterResponse2.setFileSize(0L);
						fileWriterResponse2.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
						exportDataUtils.deleteFile(request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),DispatchControllerConstants.FILE_EXTENSION_CSV,logKey);
						FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
						threadPoolTaskExecutor.submit(()->{
							exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
						});
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}				
				}
				return new ResponseEntity<>(response, HttpStatus.OK);
			});
			response.setTotalElements(Long.valueOf(ticketCount));
			response.setTotalPages(maxPageCount);
			response.setStatusCode(HttpStatus.OK.value());
			response.setStatusMessage(DispatchControllerConstants.RESPONSE_OK);
			fileWriterResponse = exportDataUtils.getFileDetails(request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),DispatchControllerConstants.FILE_EXTENSION_CSV,logKey);
			FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
			threadPoolTaskExecutor.submit(()->{
				exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
			});

		}catch(Exception e) {
			log.info("{} error occurred for business {} where request was {} , exception: {}",logKey,businessId,request,ExceptionUtils.getStackTrace(e));
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			response.setTotalElements(0L);
			response.setTotalPages(0);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
			threadPoolTaskExecutor.submit(()->{
				exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
			});
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	@Override
	public DataExportFieldResponse getDataExportAdvancedSearchFields() {
		DataExportFieldResponse responseData =new DataExportFieldResponse();
		try {

			AdvanceSearchFieldRequest request = new AdvanceSearchFieldRequest();

			request.setSearchFor(DispatchControllerConstants.SEARCH_COLLECTION_TECHNICIAN);
			responseData = ticketRepository.getTicketAdvancedSearchFields(request);

			return responseData;
		}catch(Exception e) {
			e.getMessage();
			log.info("Unable to find the data while getting getAdvancedSearchFields:{}", e.getMessage());
			responseData.setResult(DispatchControllerConstants.STATUS_FAILED);
			return responseData;
		}

	}

	private List<TechnicianAssignmentResponse> getTechnicianTicketData(List<AgentAssignmentSolutionModel> ticketListData) {

		List<TechnicianAssignmentResponse> listData = new ArrayList<>();
		try {
			for(AgentAssignmentSolutionModel agentData: ticketListData) {

				Integer count = 0;
				if(agentData.getAgent().getTicketList().size()!=0) {
					for(Ticket ticketData: agentData.getAgent().getTicketList()) {

						TechnicianAssignmentResponse technicianData =new TechnicianAssignmentResponse();

						technicianData.setTechnicianId(agentData.getAgent().getTechnicianId());
						technicianData.setTicketNumber(ticketData.getTicketNumber());
						technicianData.setTicketType(ticketData.getTicketType());
						technicianData.setWorkType(ticketData.getWorkType());
						technicianData.setEstTicketDuration(ticketData.getTicketETA());
						technicianData.setEmergency(ticketData.getEmergencyFlag());
						technicianData.setDueDate(ticketData.getTicketDueDateAndTime());
						technicianData.setClosedDate(ticketData.getCompletionDateTime());
						technicianData.setTechnicianName(agentData.getAgent().getFirstName() + " "+ agentData.getAgent().getLastName());
						technicianData.setUserId(agentData.getAgent().getTechnicianId());
						technicianData.setEmployeeId(agentData.getAgent().getTechnicianId());
						technicianData.setSupervisorName(agentData.getAgent().getSupervisorName());
						technicianData.setState(ticketData.getWorkState());
						technicianData.setReceivedDate(ticketData.getAssignmentDateTime());
						technicianData.setCity(ticketData.getWorkCity());
						technicianData.setIndex(count);
						technicianData.setLatitude(agentData.getAgent().getLocation().getLatitude());
						technicianData.setLongitude(agentData.getAgent().getLocation().getLongitude());
						technicianData.setDistance(agentData.getAgent().getEvaluatedDistance());
						technicianData.setAvailableCapacity(agentData.getAgent().getAvailableTime());
						technicianData.setAssignmentETA(agentData.getAgent().getTotalWorkHourGlobal());
						technicianData.setTechnicianScore(agentData.getAgent().getTechnicianScore());
						technicianData.setTicketNumber811(ticketData.getTicketNumber811());
						technicianData.setAssignmentDateTime(ticketData.getAssignmentDateTime());
						technicianData.setTicketScore(ticketData.getTicketScore());
						count++;

						listData.add(technicianData);
					}


				}


			}

			return listData;

		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public ResponseEntity<ExportTicketResponseDTO> exportCockpitTicketData(TicketExportDataRequest request,
			String businessId) {
		String logKey = null;
		String businessToken = BusinessTokenContext.getBusinessToken();
		ExportTicketResponseDTO response = new ExportTicketResponseDTO();
		//		String userId = request.getUserId();
		Integer pageNumber = DispatchControllerConstants.DEFAULT_PAGE_NUMBER;
		Integer pageSize = StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_PAGE_SIZE) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_PAGE_SIZE)) : (DispatchControllerConstants.DEFAULT_PAGE_SIZE) ;
		Integer numberOfThreads = StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_NUMBER_OF_THREADS) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_NUMBER_OF_THREADS)) : (DispatchControllerConstants.DEFAULT_NUMBER_OF_THREADS) ;
		String startDate = request.getStartDate();
		String endDate = request.getEndDate();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT);
		FileWriterResponse fileWriterResponse = new FileWriterResponse();
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Integer maxRecordFetchLimit = StartupApplicationListener.getProperty(ServiceConstants.MAX_RECORD_FETCH_LIMIT) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.MAX_RECORD_FETCH_LIMIT)) : (DispatchControllerConstants.MAX_RECORD_FETCH_LIMIT) ;
		request.setTimeZone(DispatchControllerConstants.TIME_ZONE_CST);
		Map<Integer , FileWriterResponse> threadOperationStatus = new ConcurrentHashMap<>();

		try {

			if(StringUtils.isEmpty(request.getReportName())
					|| StringUtils.isEmpty(request.getUserId())
					|| StringUtils.isEmpty(request.getFileName())
					|| request.getId() == null
					|| request.getId() < 0
					) {
				log.info("{} Incomplete request ,for business {} where request was {} ",logKey,businessId,request);
				response.setStatusCode(HttpStatus.BAD_REQUEST.value());
				response.setStatusMessage(DispatchControllerConstants.RESPONSE_INCOMPLETE_REQUEST);
				return new ResponseEntity<>(response, HttpStatus.OK); 
			}


			if(StringUtils.isEmpty(request.getStartDate()) || StringUtils.isEmpty(request.getEndDate())) {
				log.info("{} Date field missing in request for business {} with request {}",logKey,businessId,request);
				startDate = exportDataUtils.getDateTimeByTimeZone(LocalDateTime.now().minusHours(DispatchControllerConstants.HOURS_INT).toString(),request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
				endDate = exportDataUtils.getDateTimeByTimeZone(LocalDateTime.now().toString(),request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
			}else {
				startDate = StringUtils.replace(request.getStartDate(), " ", DispatchControllerConstants.TIME_FORMAT_T);
				endDate = StringUtils.replace(request.getEndDate(), " ", DispatchControllerConstants.TIME_FORMAT_T);
				startDate = exportDataUtils.getDateTimeByTimeZone(startDate,request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
				endDate = exportDataUtils.getDateTimeByTimeZone(endDate,request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
			}
			log.info("{} Date range applied {} to {} with Timezone {} for business {}",logKey,startDate,endDate,request.getTimeZone(),businessId);
			request.setStartDate(startDate);
			request.setEndDate(endDate);
			request.setPageNumber(pageNumber);
			request.setPageSize(pageSize);
			String[] ticketExportColumns = DispatchControllerConstants.COCKPIT_TICKET_EXPORT_COLUMNS;

			Integer ticketCount = agentRepository.getCockpitTicketCountByCriteria(request);

			Integer maxPageCount = ticketCount / pageSize;




			if(ticketCount == null || ticketCount == 0) {
				response.setStatusCode(HttpStatus.OK.value());
				response.setStatusMessage(DispatchControllerConstants.NO_RECORD);
				response.setTotalElements(0L);
				response.setTotalPages(0);
				fileWriterResponse.setFilePath(null);
				fileWriterResponse.setFileSize(0L);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_NO_DATA_FOUND_CODE);

				FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
				threadPoolTaskExecutor.submit(()->{
					exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
				});
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			if(ticketCount > maxRecordFetchLimit) {
				response.setStatusCode(HttpStatus.OK.value());
				response.setStatusMessage(DispatchControllerConstants.FILE_TOO_LARGE);
				response.setTotalElements(0L);
				response.setTotalPages(0);
				fileWriterResponse.setFilePath(null);
				fileWriterResponse.setFileSize(0L);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_FILE_TOO_LARGE_CODE);

				FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
				threadPoolTaskExecutor.submit(()->{
					exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
				});
				return new ResponseEntity<>(response, HttpStatus.OK);
			}

			threadPoolTaskExecutor.submit(()->{

				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(businessToken);	

				Integer totalPageCount = ticketCount / pageSize;
				if(ticketCount % pageSize != 0) {
					totalPageCount++;
				}
				Integer numberOfRounds = totalPageCount / numberOfThreads ;
				if(totalPageCount % numberOfThreads != 0) {
					numberOfRounds++;
				}
				Integer totalPageCountLoop = totalPageCount ;
				FileWriterResponse fileWriterResponse2 = exportDataUtils.createCSVFileInVolume(request.getReportName(), ticketExportColumns, StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),logKey);
				if(fileWriterResponse2.getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE) {
					log.info("{} error occurred while creating csv file for business {} where request was {} ",logKey,businessId,request);
					response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
					response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
					response.setTotalElements(0L);
					response.setTotalPages(0);
					FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
					threadPoolTaskExecutor.submit(()->{
						exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
					});
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				ThreadPoolExecutor executor = new ThreadPoolExecutor(numberOfThreads,numberOfThreads,0,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());

				for(int r = 1; r <= numberOfRounds; r++) {
					Integer pageCount = 0;
					Integer startPageNo = (r-1) * numberOfThreads;
					Integer endPageNo = totalPageCountLoop;

					if( numberOfThreads > totalPageCountLoop) {
						pageCount = totalPageCountLoop;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = ((r-1) * numberOfThreads) + totalPageCountLoop -1;
					}  else { 
						pageCount = numberOfThreads;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = (r * numberOfThreads) - 1;
					}

					CountDownLatch countDownLatch = new CountDownLatch(pageCount);
					for(int i=startPageNo;i<=endPageNo ; i++ ) {
						Integer pageNumberLoop = i;

						request.setPageNumber(pageNumberLoop);

						executor.execute(()->{
							try {

								RequestContextHolder.setRequestAttributes(requestAttributes);
								BusinessContext.setTenantId(businessId);
								BusinessTokenContext.setBusinessToken(businessToken);

								List<Ticket> ticketListData = agentRepository.getTicketListData(request,pageNumberLoop);

								List<TicketDataResponse> techinicianList = getCockpitTicketData(ticketListData);

								List<Object[]> stringListArray = new ArrayList<>();

								techinicianList.forEach(data ->{
									List<String> strList = dataConverterUtils.convertCockpitTicketExportDataToList(data, request.getTimeZone(), dtf);
									stringListArray.add(strList.toArray());
								});

								FileWriterResponse	fileWriterResponseThread = exportDataUtils.exportToCSV(stringListArray, ticketExportColumns,request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),logKey);
								threadOperationStatus.put(pageNumberLoop, fileWriterResponseThread);
							}catch(Exception e) {
								log.info("{} error occurred in Thread, pageNumber {} for business {} where request was {} , exception: {}",logKey,pageNumberLoop,businessId,request,ExceptionUtils.getStackTrace(e));
								FileWriterResponse fileWriterExceptionResponse = new FileWriterResponse();
								fileWriterExceptionResponse.setFilePath(null);
								fileWriterExceptionResponse.setFileSize(0L);
								fileWriterExceptionResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
								threadOperationStatus.put(pageNumberLoop, fileWriterExceptionResponse);
							}
							countDownLatch.countDown();
						});				
					}			
					totalPageCountLoop = totalPageCountLoop - numberOfThreads;
					countDownLatch.await();
				}

				executor.shutdown();

				for(Integer threadOperationStatusKey : threadOperationStatus.keySet()) {

					if(threadOperationStatus.get(threadOperationStatusKey).getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE )  {
						response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
						response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
						response.setTotalElements(0L);
						response.setTotalPages(0);
						fileWriterResponse2.setFilePath(null);
						fileWriterResponse2.setFileSize(0L);
						fileWriterResponse2.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
						exportDataUtils.deleteFile(request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),DispatchControllerConstants.FILE_EXTENSION_CSV,logKey);
						FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
						threadPoolTaskExecutor.submit(()->{
							exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
						});
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}				
				}

				return new ResponseEntity<>(response, HttpStatus.OK);
			});
			response.setTotalElements(Long.valueOf(ticketCount));
			response.setTotalPages(maxPageCount);
			response.setStatusCode(HttpStatus.OK.value());
			response.setStatusMessage(DispatchControllerConstants.RESPONSE_OK);
			fileWriterResponse = exportDataUtils.getFileDetails(request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),DispatchControllerConstants.FILE_EXTENSION_CSV,logKey);
			FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
			threadPoolTaskExecutor.submit(()->{
				exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
			});

		}catch(Exception e) {
			log.info("{} error occurred for business {} where request was {} , exception: {}",logKey,businessId,request,ExceptionUtils.getStackTrace(e));
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			response.setTotalElements(0L);
			response.setTotalPages(0);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
			threadPoolTaskExecutor.submit(()->{
				exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
			});
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	private List<TicketDataResponse> getCockpitTicketData(List<Ticket> ticketListData) {

		List<TicketDataResponse> listData = new ArrayList<>();
		try {


			if(ticketListData.size()!=0) {
				for(Ticket ticketData: ticketListData) {

					TicketDataResponse technicianData =new TicketDataResponse();

					technicianData.setTicketId(ticketData.getTicketNumber());
					technicianData.setWorkType(ticketData.getWorkType());
					technicianData.setTicketScore(ticketData.getTicketScore());
					technicianData.setTimeEstimate(ticketData.getTicketETA());
					technicianData.setWorkAddress(ticketData.getWorkAddress());
					technicianData.setWorkZip(ticketData.getWorkZip());
					technicianData.setStreet(ticketData.getWorkStreet());
					technicianData.setWorkCity(ticketData.getWorkCity());
					technicianData.setWorkState(ticketData.getWorkState());
					technicianData.setTicketType(ticketData.getTicketType());
					technicianData.setStatus(ticketData.getGlobalStatus());
					technicianData.setTechnicianName(ticketData.getTechnicianFirstName());
					technicianData.setTechnicianId(ticketData.getTechnicianId());
					technicianData.setSupervisorName(ticketData.getSupervisorName());
					technicianData.setSupervisorId(ticketData.getSupervisorId());
					technicianData.setDueDate(ticketData.getTicketDueDateAndTime());
					technicianData.setCreatedDateTime(ticketData.getCreatedDateTime());
					technicianData.setTicketNumber811(ticketData.getTicketNumber811());
					technicianData.setAssignmentDateTime(ticketData.getAssignmentDateTime());

					listData.add(technicianData);
				}

			}

			return listData;

		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	private List<TicketTechnicianResponseData> getConsolidatedTicketData(List<Ticket> ticketListData) {

		List<TicketTechnicianResponseData> listData = new ArrayList<>();
		try {


			if(ticketListData.size()!=0) {
				for(Ticket ticketData: ticketListData) {

					TicketTechnicianResponseData technicianData =new TicketTechnicianResponseData();

					technicianData.setTicketId(ticketData.getTicketNumber());
					technicianData.setTicketType(ticketData.getTicketType());
					technicianData.setWorkType(ticketData.getWorkType());
					technicianData.setTicketScore(ticketData.getTicketScore());
					technicianData.setStatus(ticketData.getGlobalStatus());
					technicianData.setTechnicianScore(null);
					technicianData.setEstTicketDuration(ticketData.getTicketETA());
					technicianData.setEmergency(ticketData.getEmergencyFlag());
					technicianData.setCreatedDateTime(ticketData.getCreatedDateTime());
					technicianData.setAssignmentDateTime(ticketData.getAssignmentDateTime());
					technicianData.setDueDate(ticketData.getTicketDueDateAndTime());
					technicianData.setClosedDate(ticketData.getCompletionDateTime());
					technicianData.setReceivedDate(null);
					technicianData.setTechnicianId(ticketData.getTechnicianId());
					technicianData.setTechnicianName(ticketData.getTechnicianFirstName());
					technicianData.setUserId(null);
					technicianData.setEmployeeId(null);
					technicianData.setSupervisorId(ticketData.getSupervisorId());
					technicianData.setSupervisorName(ticketData.getSupervisorName());
					technicianData.setSubGroup(null);
					technicianData.setDistrict(null);
					technicianData.setWorkState(ticketData.getWorkState());
					technicianData.setWorkCity(ticketData.getWorkCity());
					technicianData.setRouteIndex(null);
					technicianData.setStartType(null);
					technicianData.setStartLat(ticketData.getLocation().getLatitude());
					technicianData.setStartLon(ticketData.getLocation().getLongitude());
					technicianData.setEvalMiles(null);
					technicianData.setAvailableCapacity(null);
					technicianData.setAssignmentEta(null);
					technicianData.setTicketNumber811(ticketData.getTicketNumber811());
					technicianData.setSkill(null);
					technicianData.setTicketPriority(ticketData.getTicketPriority());
					technicianData.setDriveTime(null);
					technicianData.setTotalDriveTime(null);
					technicianData.setDistance(null);

					//					technicianData.setWorkAddress(ticketData.getWorkAddress());
					//					technicianData.setWorkZip(ticketData.getWorkZip());
					//					technicianData.setStreet(ticketData.getWorkStreet());








					//					technicianData.setCreatedDateTime(ticketData.getCreatedDateTime());



					listData.add(technicianData);
				}

			}

			return listData;

		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public DataExportFieldResponse getDataExportCockpitAdvancedSearchFields() {
		DataExportFieldResponse responseData =new DataExportFieldResponse();
		try {

			AdvanceSearchFieldRequest request = new AdvanceSearchFieldRequest();

			request.setSearchFor(DispatchControllerConstants.SEARCH_COLLECTION_TICKET);
			responseData = ticketRepository.getCockpitTicketAdvancedSearchFields(request);

			return responseData;
		}catch(Exception e) {
			e.getMessage();
			log.info("Unable to find the data while getting getAdvancedSearchFields:{}", e.getMessage());
			responseData.setResult(DispatchControllerConstants.STATUS_FAILED);
			return responseData;
		}

	}


	@Override
	public ResponseEntity<ExportTicketResponseDTO> exportTicketTransactionData(TicketExportDataRequest request,
			String businessId) {
		String logKey = null;
		String businessToken = BusinessTokenContext.getBusinessToken();
		ExportTicketResponseDTO response = new ExportTicketResponseDTO();
		Integer pageNumber = DispatchControllerConstants.DEFAULT_PAGE_NUMBER;
		Integer pageSize = StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_PAGE_SIZE) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_PAGE_SIZE)) : (DispatchControllerConstants.DEFAULT_PAGE_SIZE) ;
		Integer numberOfThreads = StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_NUMBER_OF_THREADS) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.DEFAULT_NUMBER_OF_THREADS)) : (DispatchControllerConstants.DEFAULT_NUMBER_OF_THREADS) ;
		String startDate = request.getStartDate();
		String endDate = request.getEndDate();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT);
		FileWriterResponse fileWriterResponse = new FileWriterResponse();
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Integer maxRecordFetchLimit = StartupApplicationListener.getProperty(ServiceConstants.MAX_RECORD_FETCH_LIMIT) != null ? Integer.valueOf(StartupApplicationListener.getProperty(ServiceConstants.MAX_RECORD_FETCH_LIMIT)) : (DispatchControllerConstants.MAX_RECORD_FETCH_LIMIT) ;
		request.setTimeZone(DispatchControllerConstants.TIME_ZONE_CST);
		Map<Integer , FileWriterResponse> threadOperationStatus = new ConcurrentHashMap<>();

		try {

			if(StringUtils.isEmpty(request.getReportName())
					|| StringUtils.isEmpty(request.getUserId())
					|| StringUtils.isEmpty(request.getFileName())
					|| request.getId() == null
					|| request.getId() < 0
					) {
				log.info("{} Incomplete request ,for business {} where request was {} ",logKey,businessId,request);
				response.setStatusCode(HttpStatus.BAD_REQUEST.value());
				response.setStatusMessage(DispatchControllerConstants.RESPONSE_INCOMPLETE_REQUEST);
				return new ResponseEntity<>(response, HttpStatus.OK); 
			}


			if(StringUtils.isEmpty(request.getStartDate()) || StringUtils.isEmpty(request.getEndDate())) {
				log.info("{} Date field missing in request for business {} with request {}",logKey,businessId,request);
				startDate = exportDataUtils.getDateTimeByTimeZone(LocalDateTime.now().minusHours(DispatchControllerConstants.HOURS_INT).toString(),request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
				endDate = exportDataUtils.getDateTimeByTimeZone(LocalDateTime.now().toString(),request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
			}else {
				startDate = StringUtils.replace(request.getStartDate(), " ", DispatchControllerConstants.TIME_FORMAT_T);
				endDate = StringUtils.replace(request.getEndDate(), " ", DispatchControllerConstants.TIME_FORMAT_T);
				startDate = exportDataUtils.getDateTimeByTimeZone(startDate,request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
				endDate = exportDataUtils.getDateTimeByTimeZone(endDate,request.getTimeZone(),DispatchControllerConstants.TIME_ZONE_CST,DispatchControllerConstants.RESPONSE_DATETIME_FORMAT,DispatchControllerConstants.ELASTIC_OFFSET);
			}
			log.info("{} Date range applied {} to {} with Timezone {} for business {}",logKey,startDate,endDate,request.getTimeZone(),businessId);
			request.setStartDate(startDate);
			request.setEndDate(endDate);
			request.setPageNumber(pageNumber);
			request.setPageSize(pageSize);
			String[] ticketExportColumns = DispatchControllerConstants.TICKET_TECHNICIAN_EXPORT_COLUMNS;

			

			TicketExportDataRequest ticketRequest = new TicketExportDataRequest();

			ColumnFilters filter =new  ColumnFilters();

			filter.setColumn(DispatchControllerConstants.FILTER_TICKET_STATUS);
			filter.setValue(DispatchControllerConstants.STATUS_UNASSIGNED);
			filter.setOperator(DispatchControllerConstants.OPERATOR_IS);

			List<ColumnFilters> filterList =request.getFilters();

			filterList.add(filter);

			ticketRequest.setUserId(request.getUserId());
			ticketRequest.setReportName(request.getReportName());
			ticketRequest.setStartDate(request.getStartDate());
			ticketRequest.setEndDate(request.getEndDate());
			ticketRequest.setId(request.getId());
			ticketRequest.setPageSize(request.getPageSize());
			ticketRequest.setPageNumber(request.getPageNumber());
			ticketRequest.setFileName(request.getFileName());
			ticketRequest.setTimeZone(request.getTimeZone());
			ticketRequest.setFilters(filterList);

			
			Integer ticketCount = agentRepository.getCockpitTicketCountByCriteria(ticketRequest);
			Integer supervisorCount = agentRepository.getTicketCountByCriteria(request);

			Integer totalCount = ticketCount + supervisorCount;

			Integer maxPageCount = totalCount / pageSize;

			FileWriterResponse fileWriterResponse2 = exportDataUtils.createCSVFileInVolume(request.getReportName(), ticketExportColumns, StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),logKey);


			if(ticketCount == null || ticketCount == 0) {
				response.setStatusCode(HttpStatus.OK.value());
				response.setStatusMessage(DispatchControllerConstants.NO_RECORD);
				response.setTotalElements(0L);
				response.setTotalPages(0);
				fileWriterResponse.setFilePath(null);
				fileWriterResponse.setFileSize(0L);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_NO_DATA_FOUND_CODE);

				FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
				threadPoolTaskExecutor.submit(()->{
					exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
				});
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			if(ticketCount > maxRecordFetchLimit) {
				response.setStatusCode(HttpStatus.OK.value());
				response.setStatusMessage(DispatchControllerConstants.FILE_TOO_LARGE);
				response.setTotalElements(0L);
				response.setTotalPages(0);
				fileWriterResponse.setFilePath(null);
				fileWriterResponse.setFileSize(0L);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_FILE_TOO_LARGE_CODE);

				FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
				threadPoolTaskExecutor.submit(()->{
					exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
				});
				return new ResponseEntity<>(response, HttpStatus.OK);
			}

			threadPoolTaskExecutor.submit(()->{

				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(businessToken);	

				Integer totalPageCount = ticketCount / pageSize;
				if(ticketCount % pageSize != 0) {
					totalPageCount++;
				}
				Integer numberOfRounds = totalPageCount / numberOfThreads ;
				if(totalPageCount % numberOfThreads != 0) {
					numberOfRounds++;
				}
				Integer totalPageCountLoop = totalPageCount ;
				if(fileWriterResponse2.getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE) {
					log.info("{} error occurred while creating csv file for business {} where request was {} ",logKey,businessId,ticketRequest);
					response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
					response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
					response.setTotalElements(0L);
					response.setTotalPages(0);
					FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
					threadPoolTaskExecutor.submit(()->{
						exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(ticketRequest, fileDetails), businessToken);
					});
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				ThreadPoolExecutor executor = new ThreadPoolExecutor(numberOfThreads,numberOfThreads,0,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());

				for(int r = 1; r <= numberOfRounds; r++) {
					Integer pageCount = 0;
					Integer startPageNo = (r-1) * numberOfThreads;
					Integer endPageNo = totalPageCountLoop;

					if( numberOfThreads > totalPageCountLoop) {
						pageCount = totalPageCountLoop;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = ((r-1) * numberOfThreads) + totalPageCountLoop -1;
					}  else { 
						pageCount = numberOfThreads;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = (r * numberOfThreads) - 1;
					}

					CountDownLatch countDownLatch = new CountDownLatch(pageCount);
					for(int i=startPageNo;i<=endPageNo ; i++ ) {
						Integer pageNumberLoop = i;

						ticketRequest.setPageNumber(pageNumberLoop);

						executor.execute(()->{
							try {

								RequestContextHolder.setRequestAttributes(requestAttributes);
								BusinessContext.setTenantId(businessId);
								BusinessTokenContext.setBusinessToken(businessToken);

								List<Ticket> ticketListData = agentRepository.getTicketListData(ticketRequest,pageNumberLoop);

								List<TicketTechnicianResponseData> techinicianList = getConsolidatedTicketData(ticketListData);

								List<Object[]> stringListArray = new ArrayList<>();

								techinicianList.forEach(data ->{
									List<String> strList = dataConverterUtils.convertConsolidatedTicketExportDataToList(data, ticketRequest.getTimeZone(), dtf);
									stringListArray.add(strList.toArray());
								});

								FileWriterResponse	fileWriterResponseThread = exportDataUtils.exportToCSV(stringListArray, ticketExportColumns,ticketRequest.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",ticketRequest.getFileName(),businessId,ticketRequest.getUserId(),logKey);
								threadOperationStatus.put(pageNumberLoop, fileWriterResponseThread);
							}catch(Exception e) {
								log.info("{} error occurred in Thread, pageNumber {} for business {} where request was {} , exception: {}",logKey,pageNumberLoop,businessId,ticketRequest,ExceptionUtils.getStackTrace(e));
								FileWriterResponse fileWriterExceptionResponse = new FileWriterResponse();
								fileWriterExceptionResponse.setFilePath(null);
								fileWriterExceptionResponse.setFileSize(0L);
								fileWriterExceptionResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
								threadOperationStatus.put(pageNumberLoop, fileWriterExceptionResponse);
							}
							countDownLatch.countDown();
						});				
					}			
					totalPageCountLoop = totalPageCountLoop - numberOfThreads;
					countDownLatch.await();
				}

				executor.shutdown();

				for(Integer threadOperationStatusKey : threadOperationStatus.keySet()) {

					if(threadOperationStatus.get(threadOperationStatusKey).getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE )  {
						response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
						response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
						response.setTotalElements(0L);
						response.setTotalPages(0);
						fileWriterResponse2.setFilePath(null);
						fileWriterResponse2.setFileSize(0L);
						fileWriterResponse2.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
						exportDataUtils.deleteFile(ticketRequest.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",ticketRequest.getFileName(),businessId,ticketRequest.getUserId(),DispatchControllerConstants.FILE_EXTENSION_CSV,logKey);
						FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
						threadPoolTaskExecutor.submit(()->{
							exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(ticketRequest, fileDetails), businessToken);
						});
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}				
				}

				return new ResponseEntity<>(response, HttpStatus.OK);
			});


			//Supervisor Data --------------------------------------------------------------


			threadPoolTaskExecutor.submit(()->{

				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(businessToken);	

				Integer totalPageCount = supervisorCount / pageSize;
				if(supervisorCount % pageSize != 0) {
					totalPageCount++;
				}
				Integer numberOfRounds = totalPageCount / numberOfThreads ;
				if(totalPageCount % numberOfThreads != 0) {
					numberOfRounds++;
				}
				Integer totalPageCountLoop = totalPageCount ;
				if(fileWriterResponse2.getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE) {
					log.info("{} error occurred while creating csv file for business {} where request was {} ",logKey,businessId,request);
					response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
					response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
					response.setTotalElements(0L);
					response.setTotalPages(0);
					FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
					threadPoolTaskExecutor.submit(()->{
						exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
					});
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				ThreadPoolExecutor executor = new ThreadPoolExecutor(numberOfThreads,numberOfThreads,0,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());

				for(int r = 1; r <= numberOfRounds; r++) {
					Integer pageCount = 0;
					Integer startPageNo = (r-1) * numberOfThreads;
					Integer endPageNo = totalPageCountLoop;

					if( numberOfThreads > totalPageCountLoop) {
						pageCount = totalPageCountLoop;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = ((r-1) * numberOfThreads) + totalPageCountLoop -1;
					}  else { 
						pageCount = numberOfThreads;
						startPageNo = (r-1) * numberOfThreads;
						endPageNo = (r * numberOfThreads) - 1;
					}

					CountDownLatch countDownLatch = new CountDownLatch(pageCount);
					for(int i=startPageNo;i<=endPageNo ; i++ ) {
						Integer pageNumberLoop = i;

						request.setPageNumber(pageNumberLoop);

						executor.execute(()->{
							try {

								RequestContextHolder.setRequestAttributes(requestAttributes);
								BusinessContext.setTenantId(businessId);
								BusinessTokenContext.setBusinessToken(businessToken);

								List<AgentAssignmentSolutionModel> ticketListData = agentRepository.getTechnicianListData(request,pageNumberLoop);

								List<TicketTechnicianResponseData> techinicianList = getConsolidatedSupervisorData(ticketListData);

								List<Object[]> stringListArray = new ArrayList<>();

								techinicianList.forEach(data ->{
									List<String> strList = dataConverterUtils.convertConsolidatedSupervisorExportDataToList(data, request.getTimeZone(), dtf);
									stringListArray.add(strList.toArray());
								});

								FileWriterResponse	fileWriterResponseThread = exportDataUtils.exportToCSV(stringListArray, ticketExportColumns,request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),logKey);
								threadOperationStatus.put(pageNumberLoop, fileWriterResponseThread);
							}catch(Exception e) {
								log.info("{} error occurred in Thread, pageNumber {} for business {} where request was {} , exception: {}",logKey,pageNumberLoop,businessId,request,ExceptionUtils.getStackTrace(e));
								FileWriterResponse fileWriterExceptionResponse = new FileWriterResponse();
								fileWriterExceptionResponse.setFilePath(null);
								fileWriterExceptionResponse.setFileSize(0L);
								fileWriterExceptionResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
								threadOperationStatus.put(pageNumberLoop, fileWriterExceptionResponse);
							}
							countDownLatch.countDown();
						});				
					}			
					totalPageCountLoop = totalPageCountLoop - numberOfThreads;
					countDownLatch.await();
				}

				executor.shutdown();



				for(Integer threadOperationStatusKey : threadOperationStatus.keySet()) {

					if(threadOperationStatus.get(threadOperationStatusKey).getStatusCode() == DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE )  {
						response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
						response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
						response.setTotalElements(0L);
						response.setTotalPages(0);
						fileWriterResponse2.setFilePath(null);
						fileWriterResponse2.setFileSize(0L);
						fileWriterResponse2.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
						exportDataUtils.deleteFile(request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),DispatchControllerConstants.FILE_EXTENSION_CSV,logKey);
						FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse2);
						threadPoolTaskExecutor.submit(()->{
							exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
						});
						return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
					}				
				}

				return new ResponseEntity<>(response, HttpStatus.OK);
			});

			response.setTotalElements(Long.valueOf(totalCount));
			response.setTotalPages(maxPageCount);
			response.setStatusCode(HttpStatus.OK.value());
			response.setStatusMessage(DispatchControllerConstants.RESPONSE_OK);
			fileWriterResponse = exportDataUtils.getFileDetails(request.getReportName(), StartupApplicationListener.getProperty(ServiceConstants.DATA_EXPORT_VOLUME_DIRECTORY)+"/",request.getFileName(),businessId,request.getUserId(),DispatchControllerConstants.FILE_EXTENSION_CSV,logKey);
			FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
			threadPoolTaskExecutor.submit(()->{
				exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
			});

		}catch(Exception e) {
			log.info("{} error occurred for business {} where request was {} , exception: {}",logKey,businessId,request,ExceptionUtils.getStackTrace(e));
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			response.setTotalElements(0L);
			response.setTotalPages(0);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			FileWriterResponse fileDetails = dataConverterUtils.convertFileDetails(fileWriterResponse);
			threadPoolTaskExecutor.submit(()->{
				exportDataSupportUtils.callDataExportToUpdateFileDetails(dataConverterUtils.convertExportRequestToSaveMetaDataRequest(request, fileDetails), businessToken);
			});
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


	private List<TicketDataResponse> getSupervisorData(List<AgentAssignmentSolutionModel> ticketListData) {

		List<TicketDataResponse> listData = new ArrayList<>();
		try {

			for(AgentAssignmentSolutionModel agentData: ticketListData) {

				if(agentData.getAgent().getTicketList().size()!=0) {
					for(Ticket ticketData: agentData.getAgent().getTicketList()) {

						TicketDataResponse technicianData =new TicketDataResponse();

						technicianData.setTicketId(ticketData.getTicketNumber());
						technicianData.setWorkType(ticketData.getWorkType());
						technicianData.setTicketScore(ticketData.getTicketScore());
						technicianData.setTimeEstimate(ticketData.getTicketETA());
						technicianData.setWorkAddress(ticketData.getWorkAddress());
						technicianData.setWorkZip(ticketData.getWorkZip());
						technicianData.setStreet(ticketData.getWorkStreet());
						technicianData.setWorkCity(ticketData.getWorkCity());
						technicianData.setWorkState(ticketData.getWorkState());
						technicianData.setTicketType(ticketData.getTicketType());
						technicianData.setStatus(ticketData.getGlobalStatus());
						technicianData.setTechnicianName(ticketData.getTechnicianFirstName());
						technicianData.setTechnicianId(ticketData.getTechnicianId());
						technicianData.setSupervisorName(ticketData.getSupervisorName());
						technicianData.setSupervisorId(ticketData.getSupervisorId());
						technicianData.setDueDate(ticketData.getTicketDueDateAndTime());
						technicianData.setCreatedDateTime(ticketData.getCreatedDateTime());
						technicianData.setTicketNumber811(ticketData.getTicketNumber811());
						technicianData.setAssignmentDateTime(ticketData.getAssignmentDateTime());

						listData.add(technicianData);
					}


				}


			}

			return listData;

		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	private List<TicketTechnicianResponseData> getConsolidatedSupervisorData(List<AgentAssignmentSolutionModel> ticketListData) {

		List<TicketTechnicianResponseData> listData = new ArrayList<>();
		try {

			for(AgentAssignmentSolutionModel agentData: ticketListData) {
				
				long totalTravelTime = 0L;
				
				List<Double>  distanceList = new ArrayList<>();
				List<Long>  timeList = new ArrayList<>();

				if(agentData.getAgent().getTicketList().size()!=0) {

					for(int i=1;i<= agentData.getAgent().getTicketList().size();i++) {
						
						Location localtionFrom = agentData.getAgent().getTicketList().get(i-1).getLocation();
						Location localtionTo = agentData.getAgent().getTicketList().get(i).getLocation();
						
						RouteSolverPath routeResponse = dispatchControllerSupportUtils.getLocationDistanceDetails(localtionFrom, localtionTo);
					
						totalTravelTime = totalTravelTime + routeResponse.getTime();
						distanceList.add(routeResponse.getDistance());					
						timeList.add(routeResponse.getTime());			
						
					}
				}

				if(agentData.getAgent().getTicketList().size()!=0) {

					Integer count = 0;
					for(Ticket ticketData: agentData.getAgent().getTicketList()) {

						TicketTechnicianResponseData technicianData =new TicketTechnicianResponseData();

						technicianData.setTicketId(ticketData.getTicketNumber());
						technicianData.setTicketType(ticketData.getTicketType());
						technicianData.setWorkType(ticketData.getWorkType());
						technicianData.setTicketScore(ticketData.getTicketScore());
						technicianData.setStatus(ticketData.getGlobalStatus());
						technicianData.setTechnicianScore(null);
						technicianData.setEstTicketDuration(ticketData.getTicketETA());
						technicianData.setEmergency(ticketData.getEmergencyFlag());
						technicianData.setCreatedDateTime(ticketData.getCreatedDateTime());
						technicianData.setAssignmentDateTime(ticketData.getAssignmentDateTime());
						technicianData.setDueDate(ticketData.getTicketDueDateAndTime());
						technicianData.setClosedDate(ticketData.getCompletionDateTime());
						technicianData.setReceivedDate(null);
						technicianData.setTechnicianId(ticketData.getTechnicianId());
						technicianData.setTechnicianName(ticketData.getTechnicianFirstName());
						technicianData.setUserId(agentData.getAgent().getTechnicianId());
						technicianData.setEmployeeId(agentData.getAgent().getTechnicianId());
						technicianData.setSupervisorId(ticketData.getSupervisorId());
						technicianData.setSupervisorName(ticketData.getSupervisorName());
						technicianData.setSubGroup(null);
						technicianData.setDistrict(null);
						technicianData.setWorkState(ticketData.getWorkState());
						technicianData.setWorkCity(ticketData.getWorkCity());
						technicianData.setRouteIndex(count);
						technicianData.setStartType(null);
						technicianData.setStartLat(ticketData.getLocation().getLatitude());
						technicianData.setStartLon(ticketData.getLocation().getLongitude());
						technicianData.setEvalMiles(agentData.getAgent().getEvaluatedDistance() * 0.000621371);
						technicianData.setAvailableCapacity(agentData.getAgent().getAvailableTime());
						technicianData.setAssignmentEta(agentData.getAgent().getTotalWorkHourGlobal());
						technicianData.setTicketNumber811(ticketData.getTicketNumber811());
						technicianData.setSkill(agentData.getAgent().getSkills());
						technicianData.setTicketPriority(ticketData.getTicketPriority());
						technicianData.setDriveTime(timeList.get(count)/60000);
						technicianData.setTotalDriveTime(totalTravelTime/60000);
						technicianData.setDistance(distanceList.get(count) * 0.000621371);

						listData.add(technicianData);
						
						count++;
					}


				}


			}

			return listData;

		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


}
