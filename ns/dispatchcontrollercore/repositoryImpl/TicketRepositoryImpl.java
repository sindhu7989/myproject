package com.straviso.ns.dispatchcontrollercore.repositoryImpl;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.GetTicketTrailsCollectionDTO;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.TicketTrailsCollectionDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchDataColumn;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchFieldRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.AdvanceSearchRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.CockpitBubbleCountStatRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ColumnFilters;
import com.straviso.ns.dispatchcontrollercore.dto.request.DashboardRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.NSAuditRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ActionByMonth;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.BubbleCountStatResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.StatusByMonth;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketSequence;
import com.straviso.ns.dispatchcontrollercore.entity.ConstraintConfig;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.mongodb.MultiTenantMongoDbFactory;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.dto.response.CountResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.DataExportFieldResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.LumenStartColumn;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import lombok.extern.log4j.Log4j2;

@Repository
@Transactional
@Log4j2
public class TicketRepositoryImpl implements TicketRepository {

	public static ConcurrentMap<String, MongoDatabase> mongoData = new ConcurrentHashMap<>();

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private MultiTenantMongoDbFactory multiTenantMongoDbFactory;

	
	@Override
	public List<Map<String, Map<String, Integer>>> getTicketStatusCountByMonth(DashboardRequest request) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {
			LocalDateTime startDateTime = LocalDateTime.parse(request.getStartDate(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(request.getEndDate(), formatter);

			Query query = new Query();

			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_CREATED_DATETIME)
					.gte(ZonedDateTime.of(startDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())
					.lte(ZonedDateTime.of(endDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime()));
			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).in(DispatchControllerConstants.TICKETSTATUSLIST));
			query.fields().include(DispatchControllerConstants.FIELD_TICKET_NO,DispatchControllerConstants.FIELD_GLOBAL_STATUS,
					DispatchControllerConstants.FIELD_CREATED_DATETIME);

			List<StatusByMonth> results = mongoTemplate.find(query, StatusByMonth.class,DispatchControllerConstants.TICKET_COLLECTION);

			List<Map<String, Map<String, Integer>>> dataList = new ArrayList<>();

			Map<String, Map<String, Integer>> dataByMonthAndStatus = new HashMap<>();
			LocalDate currentDate = LocalDate.now();
			for (int i = 0; i < 5; i++) {
				LocalDate sixMonthsBefore = currentDate.minusMonths(i + 1);
				Month month = sixMonthsBefore.getMonth();
				dataByMonthAndStatus.put(month.toString(), new HashMap<>());
			}

			for(StatusByMonth data : results) {

				String monthName = data.getCreatedDateTime().getMonth().name();

				Map<String, Integer> statusCounts = dataByMonthAndStatus.computeIfAbsent(monthName, k -> new HashMap<>());
				String globalStatus = data.getGlobalStatus();

				if (globalStatus != null) {
					statusCounts.put(globalStatus, statusCounts.getOrDefault(globalStatus, 0) + 1);
				}
			}
			Map<String, Map<String, Integer>> sortedMap = sortMonthMap(dataByMonthAndStatus);

			dataList.add(sortedMap);

			return dataList;

		}catch(Exception e) {

			log.info("{} Unable to get Ticket Count Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;

		}
	}

	@Override
	public List<Map<String, Map<String, Integer>>> getTicketActionCountByMonth(DashboardRequest request) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {
			LocalDateTime startDateTime = LocalDateTime.parse(request.getStartDate(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(request.getEndDate(), formatter);

			Query query = new Query();

			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_CREATED_DATETIME)
					.gte(ZonedDateTime.of(startDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())
					.lte(ZonedDateTime.of(endDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime()));
			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_ACTION_STATUS).in(DispatchControllerConstants.TICKETACTIONLIST));
			query.fields().include(DispatchControllerConstants.FIELD_TICKET_NO,DispatchControllerConstants.FIELD_ACTION_STATUS,
					DispatchControllerConstants.FIELD_CREATED_DATETIME);

			List<ActionByMonth> results = mongoTemplate.find(query, ActionByMonth.class,DispatchControllerConstants.TICKET_COLLECTION);

			List<Map<String, Map<String, Integer>>> dataList = new ArrayList<>();

			Map<String, Map<String, Integer>> monthDataMap = new HashMap<>();

			LocalDate currentDate = LocalDate.now();

			for (int i = 0; i < 5; i++) {
				LocalDate sixMonthsBefore = currentDate.minusMonths(i + 1);
				Month month = sixMonthsBefore.getMonth();
				monthDataMap.put(month.toString(), new HashMap<>());
			}
			//Adding current month if current month dont have transactions
			Month month = currentDate.getMonth();
			monthDataMap.put(month.toString(), new HashMap<>());

			for(ActionByMonth data : results) {

				String monthName = data.getCreatedDateTime().getMonth().name();

				Map<String, Integer> statusCounts = monthDataMap.computeIfAbsent(monthName, k -> new HashMap<>());
				String actionOnTicket = data.getActionOnTicket();

				if (actionOnTicket != null) {
					statusCounts.put(actionOnTicket, statusCounts.getOrDefault(actionOnTicket, 0) + 1);
				}
			}

			Map<String, Map<String, Integer>> sortedMap = sortMonthMap(monthDataMap);

			dataList.add(sortedMap);

			return dataList;

		}catch(Exception e) {

			log.info("{} Unable to get Ticket Count Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;

		}
	}

	@Override
	public Object getTopTicketCount(ToptechnicianCountRequest request) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {

			LocalDateTime startDateTime = LocalDateTime.parse(request.getStartDate(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(request.getEndDate(), formatter);
			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where(DispatchControllerConstants.FIELD_CREATED_DATETIME)
							.gte(ZonedDateTime.of(startDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())
							.lte(ZonedDateTime.of(endDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())),
					Aggregation.group(request.getFieldName()).count().as(DispatchControllerConstants.WORD_COUNT),
					Aggregation.project(DispatchControllerConstants.WORD_COUNT).and(request.getFieldName()).previousOperation(),
					Aggregation.sort(Sort.by(DispatchControllerConstants.WORD_COUNT).descending()),
					Aggregation.limit(5)
					);

			AggregationResults<Object> results = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.TICKET_COLLECTION, Object.class);

			return results.getMappedResults();

		}catch(Exception e) {
			log.info("{} Unable to get Ticket Count Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;

		}
	}

	public CountResponseDTO getAdvanceSearchData(AdvanceSearchRequest request) {

		CountResponseDTO response = new CountResponseDTO();
		String collectionName=null;
		try {

			if(StringUtils.equalsAnyIgnoreCase(request.getSearchFor(), DispatchControllerConstants.SEARCH_COLLECTION_TICKET)) {
				collectionName = DispatchControllerConstants.TICKET_COLLECTION;
			}else if(StringUtils.equalsAnyIgnoreCase(request.getSearchFor(), DispatchControllerConstants.SEARCH_COLLECTION_TECHNICIAN)) {
				collectionName = DispatchControllerConstants.TECHNICIAN_ASSIGNMENT_SOLUTION;
			}else {
				response.setResponseText(DispatchControllerConstants.INVALID_SEARCH_STRING);
				return response;
			}

			List<AggregationOperation> stages = new ArrayList<>();

			Query query = new Query();

			Integer pageSize = (request.getPageSize() != null
					&& request.getPageSize() > 0) ? request.getPageSize()
							: DispatchControllerConstants.DEFAULT_PAGE_SIZE;
			Integer pageNumber = (request.getPageNumber() != null
					&& request.getPageNumber() > 0) ? (request.getPageNumber()-1)
							: DispatchControllerConstants.DEFAULT_PAGE_NUMBER;

			for (AdvanceSearchDataColumn item : request.getSearchData()) {

				if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_EQUALS)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumnName())) {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName()).is(Long.valueOf(item.getValue().toString()))
								));
						query.addCriteria(Criteria.where(item.getColumnName()).is(Long.valueOf(item.getValue().toString())));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumnName())) {

						DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATE_FORMAT);
						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						stages.add(Aggregation.match(Criteria.where(item.getColumnName()).
								gte(startDateTime).lt(endDateTime)));
						query.addCriteria(Criteria.where(item.getColumnName()).gte(startDateTime).lt(endDateTime));
					}
					else {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName()).regex("^"+item.getValue().toString()+"$", "i")
								));
						query.addCriteria(Criteria.where(item.getColumnName()).regex("^"+item.getValue().toString()+"$", "i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_NOT_EQUALS)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumnName())) {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName()).ne(Integer.parseInt(item.getValue().toString()))
								));
						query.addCriteria(Criteria.where(item.getColumnName()).ne(Integer.parseInt(item.getValue().toString())));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumnName())) {

						DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATE_FORMAT);
						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						Criteria dateCriteria = new Criteria().orOperator(
								Criteria.where(item.getColumnName()).lt(startDateTime),
								Criteria.where(item.getColumnName()).gt(endDateTime)
								);

						stages.add(Aggregation.match(dateCriteria));
						query.addCriteria(dateCriteria);
					}else {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName()).regex("^(?!.*\\b" + item.getValue() + "\\b).*$", "i")
								));
						query.addCriteria(Criteria.where(item.getColumnName()).regex("^(?!.*\\b" + item.getValue() + "\\b).*$", "i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_CONTAINS)){

					if(DispatchControllerConstants.longFieldList.contains(item.getColumnName())) {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName()).is(Long.valueOf(item.getValue().toString()))
								));
						query.addCriteria(Criteria.where(item.getColumnName()).is(Long.valueOf(item.getValue().toString())));

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumnName())) {

						DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATE_FORMAT);
						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						stages.add(Aggregation.match(Criteria.where(item.getColumnName()).
								gte(startDateTime).lt(endDateTime)));
						query.addCriteria(Criteria.where(item.getColumnName()).gte(startDateTime).lt(endDateTime));
					}else {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName()).regex(".*"+item.getValue().toString()+".*","i")
								));

						query.addCriteria(Criteria.where(item.getColumnName()).regex(".*"+item.getValue().toString()+".*","i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IN)){

					String values = item.getValue().toString();
					  String[] items = values.split(",");
					  List<String> parameterList = Arrays.asList(items);
					
					if(DispatchControllerConstants.longFieldList.contains(item.getValue())) {
						
						 List<Long> longList = new ArrayList<>();
						  for (String data : items) {
					                Long value = Long.parseLong(data.trim());
					                longList.add(value);
					        }
						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName().toString()).in(longList)
								));
						
						query.addCriteria(Criteria.where(item.getColumnName().toString()).in(longList));

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getValue())) {

						DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATE_FORMAT);
						Criteria dateRangeCriteria = new Criteria();
						for(String dateValue:parameterList) {
							LocalDate date = LocalDate.parse(dateValue,inputFormatter);
							
							 dateRangeCriteria.orOperator(Criteria.where(item.getColumnName().toString()).
										gte(date.atStartOfDay()).lt(date.atTime(LocalTime.MAX)));
						}
						

						stages.add(Aggregation.match(dateRangeCriteria));
						
						query.addCriteria(dateRangeCriteria);
					}else {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumnName().toString()).in(parameterList)
								));
						query.addCriteria(Criteria.where(item.getColumnName().toString()).in(parameterList)
								);
					}

				}

			}

			Long count = mongoTemplate.count(query, Long.class, collectionName);

			response.setTotalElements(count);

			stages.add(Aggregation.sort(Sort.by("_id").ascending()));
			stages.add(Aggregation.skip((long) pageNumber * pageSize));
			stages.add(Aggregation.limit(pageSize));
			Aggregation aggregation = Aggregation.newAggregation(stages);

			AggregationResults<Object> results = mongoTemplate.aggregate(aggregation,collectionName, Object.class);

			response.setResponseData(results.getMappedResults());
			return response;

		}catch(Exception e) {
			
			log.info("{} Unable to get Advance Search Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;
		}
	}


	private Date addDays(Date date, int days) {
		return new Date(date.getTime() + days * 24 * 60 * 60 * 1000);
	}

	public Map<String, Map<String, Integer>> sortMonthMap(Map<String, Map<String, Integer>> monthMap) {



		Map<String, Map<String, Integer>> sortedMonthMap = new TreeMap<>(Comparator.comparing(monthOrder::valueOf));
		sortedMonthMap.putAll(monthMap);
		return sortedMonthMap;
	}


	public enum monthOrder {
		JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE,
		JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER;
	}


	@Override
	public Object getAdvancedSearchFields(AdvanceSearchFieldRequest request) {

		try {
			String collectionName =null;

			if(StringUtils.equalsIgnoreCase(request.getSearchFor(), DispatchControllerConstants.SEARCH_COLLECTION_TICKET)) {
				collectionName = DispatchControllerConstants.TICKET_SEARCH_COLLECTION;
			}else if(StringUtils.equalsIgnoreCase(request.getSearchFor(), DispatchControllerConstants.SEARCH_COLLECTION_TECHNICIAN)) {
				collectionName = DispatchControllerConstants.TECHNICIAN_SEARCH_COLLECTION;
			}

			List<Object> response = mongoTemplate.findAll(Object.class, collectionName);

			return response;
		}catch(Exception e) {
			return null;
		}
	}

	@Override
	public List<Ticket> findAll() {
		return mongoTemplate.findAll(Ticket.class);
	}



	@Override
	public void updateLumenTicketCollection(LumenCollectionUpdateDTO lumenCollectionUpdateDTO) {
		MongoDatabase database = null;
		try {
			log.info("Master Collection Update Operation Started for Ticket Number :{}",lumenCollectionUpdateDTO.getTicketNumber());
			if(mongoData.get(BusinessContext.getTenantId())!=null) {
				database = mongoData.get(BusinessContext.getTenantId());
			}else {
				database = multiTenantMongoDbFactory.getConnnectionWithCustomSchema();
				mongoData.put(BusinessContext.getTenantId(), database);
			}

			MongoCollection<Document> collection = database.getCollection(DispatchControllerConstants.LUMEN_COLLECTION_NAME_ENTITY_DATA);
			Document query = new Document();
			query.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER, String.valueOf(lumenCollectionUpdateDTO.getTicketNumber()));
			query.put(DispatchControllerConstants.LUMEN_COLLECTION_CONVERSATION_ID, lumenCollectionUpdateDTO.getConversationId());

			Document newDocument = new Document();
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_SUPERVISOR_ID, lumenCollectionUpdateDTO.getSupervisorId());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_ACTION_ON_TICKET, lumenCollectionUpdateDTO.getActionOnTicket());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_ASSIGNMENT_DATE_TIME, lumenCollectionUpdateDTO.getAssignmentDateTime());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_GLOBAL_STATUS, lumenCollectionUpdateDTO.getGlobalStatus());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_SUPERVISOR_ID, lumenCollectionUpdateDTO.getSupervisorId());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_SUPERVISOR_NAME, lumenCollectionUpdateDTO.getSupervisorName());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TECHNICIAN_EMAIL_ID, lumenCollectionUpdateDTO.getTechnicianEmailId());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TECHNICIAN_FIRST_NAME, lumenCollectionUpdateDTO.getTechnicianFirstName());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TECHNICIAN_ID, lumenCollectionUpdateDTO.getTechnicianId());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TECHNICIAN_LAST_NAME, lumenCollectionUpdateDTO.getTechnicianLastName());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ASSIGNED_TECHNICIAN_ID, lumenCollectionUpdateDTO.getTechnicianId());
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_STATUS, lumenCollectionUpdateDTO.getGlobalStatus());
			// 27-Sep-2023: Commented Updating Ticket Stage, As per request from Ashwin & Akash , so that enrichment can run after assessment on transmit ticket (Update on ticket) 
			// newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_STAGE, DispatchControllerConstants.TICKET_STAGE_ASSIGNMENT_COMPLETED);
			newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_SUPERVISORPOLYGON_ID, lumenCollectionUpdateDTO.getSupervisorId());

			// Finding the first document based on the query to check if ticketStage:reschedule.if reschedule then change to enrichment completed.
			Document result = collection.find(query).projection(Projections.include(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_STAGE)).first();
			log.info("result: {}",result);
			
			if (result != null) {
				String ticketStage=(String) result.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_STAGE);
				if(StringUtils.equalsIgnoreCase(ticketStage,DispatchControllerConstants.STATUS_RESCHEDULE))
				{
					newDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_STAGE, DispatchControllerConstants.TICKET_STAGE_ASSIGNMENT_COMPLETED);
					log.info("TicketStage is changed from Reschedule to AssignmentCompleted.TicketNumber: {}",lumenCollectionUpdateDTO.getTicketNumber());
					
				}
			}
			
			Document updateObject = new Document();
			updateObject.put("$set", newDocument);

			collection.updateOne(query, updateObject);

			log.info("Master Collection Update Operation Completed for Ticket Number : {}",lumenCollectionUpdateDTO.getTicketNumber());
			
			//STM : SubTicketMaster :start
			log.info("Sub-Master Collection Update Operation Started for Ticket Number : {}",lumenCollectionUpdateDTO.getTicketNumber());
			
			MongoCollection<Document> collectionSTM = database.getCollection(DispatchControllerConstants.LUMEN_COLLECTION_NAME_SUBTICKETMASTER);
			Document querySTM = new Document();
			querySTM.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER, String.valueOf(lumenCollectionUpdateDTO.getTicketNumber()));
			
			Document newDocumentSTM = new Document();
			newDocumentSTM.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_SUPERVISOR_ID, lumenCollectionUpdateDTO.getSupervisorId());
			newDocumentSTM.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ASSIGNED_TECHNICIAN_ID, lumenCollectionUpdateDTO.getTechnicianId());
			
			Document updateObjectSTM = new Document();
			updateObjectSTM.put("$set", newDocumentSTM);

			collectionSTM.updateOne(querySTM, updateObjectSTM);
			log.info("Sub-Master Collection Update Operation Completed for Ticket Number : {}",lumenCollectionUpdateDTO.getTicketNumber());
			
			//STM : SubTicketMaster :end
			
		}catch(Exception e) {
			log.info("Unable To Update Lumen Collection Due to : {}",e.getMessage());
		}

	}

	

	@Override
	public List<Map<String, String>> getTicketData(String ticketNumber, String conversationId) {
		MongoDatabase database = null;
		List<Map<String, String>> convertedResponse = new ArrayList<>();
		Map<String, Object> result1 = new LinkedHashMap<>();
		try {
			log.info("Get Ticket Data Operation Started for Ticket Number: {}", ticketNumber);
			if (mongoData.get(BusinessContext.getTenantId()) != null) {
				database = mongoData.get(BusinessContext.getTenantId());
			} else {
				database = multiTenantMongoDbFactory.getConnnectionWithCustomSchema();
				mongoData.put(BusinessContext.getTenantId(), database);
			}

			MongoCollection<Document> collection = database.getCollection(DispatchControllerConstants.LUMEN_COLLECTION_NAME_ENTITY_DATA);
			Document document = new Document();
			document.append(DispatchControllerConstants.LUMEN_COLLECTION_CONVERSATION_ID, conversationId);
			document.append(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER, String.valueOf(ticketNumber));
			FindIterable<Document> find = collection.find(document);
			long countDocuments = collection.countDocuments(document);
			if (find != null && countDocuments>0) {
				for (Document input : find) {
					for (Map.Entry<String, Object> entry : input.entrySet()) {
						String key = entry.getKey();
						Object value = entry.getValue();
						result1.put(key, value);
					}
				}
				result1.replace(DispatchControllerConstants.LUMEN_COLLECTION_ID,
						result1.get(DispatchControllerConstants.LUMEN_COLLECTION_ID).toString());
				// Convert the MongoDB response to the desired format
				convertResponseData(result1, convertedResponse, "");
				convertedResponse.sort(Comparator.comparing(entry -> entry.get("key")));
			}

		} catch (Exception e) {
			log.info("{} Unable To Get Ticket Data Due to  {}", DispatchControllerConstants.GET_TICKET_DATA_REQUEST,
					e.getMessage());
		}
		return convertedResponse;
	}

	private static void convertResponseData(Object data, List<Map<String, String>> convertedResponse, String prefix) {
		if (data instanceof Map) {
			Map<?, ?> mapData = (Map<?, ?>) data;
			for (Map.Entry<?, ?> entry : mapData.entrySet()) {
				String key = entry.getKey().toString();
				Object value = entry.getValue();
				String newPrefix = prefix + (prefix.isEmpty() ? "" : ".") + key;
				if (value != null && !(key.equals(DispatchControllerConstants.LUMEN_COLLECTION_ADDITIONAL_INFO)
						|| key.equals(DispatchControllerConstants.LUMEN_COLLECTION_ASSIGNMENTDETAILS)
						|| key.equals(DispatchControllerConstants.LUMEN_COLLECTION_ORIGINAL_TICKET_CONTENT))) {
					if (value instanceof Map) {
						convertResponseData(value, convertedResponse, newPrefix);
					} else {
						ObjectMapper objectMapper = new ObjectMapper();
						String json = null;

						try {
							json = objectMapper.writeValueAsString(convertValueToString(value));
							json = json.replace("Document", "").replace("\\", "").replace("\"", "").trim();
						} catch (JsonProcessingException e) {
							log.info("Unable To convertResponce Data Due to  {}",e.getMessage());
						}

						convertedResponse.add(convertToKeyValueMap(newPrefix, json));
					}
				}
			}
		} else {
			String stringValue = convertValueToString(data);
			convertedResponse.add(convertToKeyValueMap(prefix, stringValue));
		}
	}

	private static Map<String, String> convertToKeyValueMap(String key, String value) {
		Map<String, String> keyValueMap = new TreeMap<>();
		keyValueMap.put("key", key);
		keyValueMap.put("value", value);
		return keyValueMap;
	}

	private static String convertValueToString(Object value) {
		if (value instanceof List) {
			List<?> listValue = (List<?>) value;
			return listValue.stream().map(Object::toString).sorted().collect(Collectors.joining(", "));
		} else if (value instanceof Date) {
			Date date = (Date) value;
			return convertDateToUST(date);
		} else {
			return safeToString(value);
		}
	}

	private static String convertDateToUST(Date date) {
		Instant instant = date.toInstant();
		ZoneId istZone = ZoneId.of("Asia/Kolkata");
		ZoneId ustZone = ZoneId.of("UTC");
		ZonedDateTime istDateTime = instant.atZone(istZone);
		ZonedDateTime ustDateTime = istDateTime.withZoneSameLocal(ustZone);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		return ustDateTime.format(formatter);
	}

	private static String safeToString(Object obj) {
		try {
			return Objects.toString(obj, "");
		} catch (Exception e) {
			return "";
		}
	}
	@Override
	public JsonNode getTicketJsonView(String ticketNumber, String conversationId) {
		MongoDatabase database = null;
		try {
			log.info("Get Ticket Data JSON Operation Started for Ticket Number: {}", ticketNumber);
			if (mongoData.get(BusinessContext.getTenantId()) != null) {
				database = mongoData.get(BusinessContext.getTenantId());
			} else {
				database = multiTenantMongoDbFactory.getConnnectionWithCustomSchema();
				mongoData.put(BusinessContext.getTenantId(), database);
			}
			MongoCollection<Document> collection = database.getCollection(DispatchControllerConstants.LUMEN_COLLECTION_NAME_ENTITY_DATA);
			Document document = new Document();
			document.append(DispatchControllerConstants.LUMEN_COLLECTION_CONVERSATION_ID, conversationId);
			document.append(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER, String.valueOf(ticketNumber));
			FindIterable<Document> find = collection.find(document);

			if(find!=null && find.first()  !=null)
			{
				// Convert to a JSON object
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode jsonNode = objectMapper.valueToTree(find.first().toJson());
				return jsonNode;
			}
		} catch (Exception e) {
			log.info("{} Unable To Get Ticket Data Due to {}",DispatchControllerConstants.GET_TICKET_DATA_JSON_REQUEST, e.getMessage());
		}
		return null;
	}
	@Override
	public ResponseEntity<ApiResponseDto> saveTicketAuditTrails(TicketTrailsCollectionDTO lumenCollectionUpdateDTO) {
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		MongoDatabase database = null;
		try {
			log.info("Collection Update Operation Started for Ticket Number :{}",lumenCollectionUpdateDTO.getTicketExternalId());
			if(mongoData.get(BusinessContext.getTenantId())!=null) {
				database = mongoData.get(BusinessContext.getTenantId());
			}else {
				database = multiTenantMongoDbFactory.getConnnectionWithCustomSchema();
				mongoData.put(BusinessContext.getTenantId(), database);
			}


			MongoCollection<Document> auditCollection = database.getCollection(DispatchControllerConstants.COLLECTION_NAME_TICKET_AUDIT_TRAILS);
			Document trailDocument = new Document();

			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_CONVERSATION_ID, lumenCollectionUpdateDTO.getConversationId());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER, String.valueOf(lumenCollectionUpdateDTO.getTicketExternalId()));
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_TICKET_STAGE,lumenCollectionUpdateDTO.getTicketStage());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ACTION,lumenCollectionUpdateDTO.getAction());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_PRE_ACTION,lumenCollectionUpdateDTO.getPreAction());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_POST_ACTION,lumenCollectionUpdateDTO.getPostAction());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ACTION_BY,lumenCollectionUpdateDTO.getActionBy());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_AUDIT_DATETIME,LocalDateTime.now());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_REMARKS,lumenCollectionUpdateDTO.getRemarks());
			trailDocument.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ERROR,lumenCollectionUpdateDTO.getError());

			auditCollection.insertOne(trailDocument);

			log.info("Audit Collection Insert Operation Completed for Ticket Number : {}",lumenCollectionUpdateDTO.getTicketExternalId());

			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			apiResponseDto.setResponseData("");


			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);

		}catch(Exception e) {
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
			apiResponseDto.setResponseData("");
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		}


	}

	@Override
	public ResponseEntity<ApiResponseDto> getTicketAuditTrails(GetTicketTrailsCollectionDTO lumenCollectionUpdateDTO) {
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		MongoDatabase database = null;

		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);


		try {
			log.info("Master Collection Update Operation Started for Ticket Number :{}",lumenCollectionUpdateDTO.getTicketExternalId());
			if(mongoData.get(BusinessContext.getTenantId())!=null) {
				database = mongoData.get(BusinessContext.getTenantId());
			}else {
				database = multiTenantMongoDbFactory.getConnnectionWithCustomSchema();
				mongoData.put(BusinessContext.getTenantId(), database);
			}
			MongoCollection<Document> auditCollection = database.getCollection(DispatchControllerConstants.COLLECTION_NAME_TICKET_AUDIT_TRAILS);

			Document query = new Document();
			query.put(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER, String.valueOf(lumenCollectionUpdateDTO.getTicketExternalId()));
			//query.put(DispatchControllerConstants.LUMEN_COLLECTION_CONVERSATION_ID, lumenCollectionUpdateDTO.getConversationId());
			
			
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);

			Sort sort = Sort.by(Sort.Order.asc( DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_AUDIT_DATETIME));
					
			FindIterable<Document> find = auditCollection.find(query);
			MongoCursor<Document> iterator = find.iterator();
			
			List<GetTicketTrailsCollectionDTO> LisOfTicketTrailsCollectionDTO =new ArrayList<>();

			while(iterator.hasNext()) {

				GetTicketTrailsCollectionDTO ticketTrailsCollectionDTO = new GetTicketTrailsCollectionDTO();
				Document auditTrailDoc = iterator.next();
				ticketTrailsCollectionDTO.set_id(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.FIELD_ID)));
				ticketTrailsCollectionDTO.setConversationId(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_CONVERSATION_ID)));
				ticketTrailsCollectionDTO.setTicketExternalId(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER)));
				ticketTrailsCollectionDTO.setTicketStage(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_TICKET_STAGE)));
				ticketTrailsCollectionDTO.setAction(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ACTION)));
				ticketTrailsCollectionDTO.setPreAction(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_PRE_ACTION)));
				ticketTrailsCollectionDTO.setPostAction(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_POST_ACTION)));
				ticketTrailsCollectionDTO.setActionBy(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ACTION_BY)));
				ticketTrailsCollectionDTO.setRemarks(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_REMARKS)));
				ticketTrailsCollectionDTO.setError(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ERROR)));

				LocalDateTime auditTrailTime = LocalDateTime.parse(String.valueOf(auditTrailDoc.get(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_ACTION_TRAIL_AUDIT_DATETIME)),formatter);
				ticketTrailsCollectionDTO.setAuditDateTime(auditTrailTime.format(DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT)));

				LisOfTicketTrailsCollectionDTO.add(ticketTrailsCollectionDTO);
			}
			LisOfTicketTrailsCollectionDTO.sort(Comparator.comparing(GetTicketTrailsCollectionDTO::getAuditDateTime));
			apiResponseDto.setResponseData(LisOfTicketTrailsCollectionDTO);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		}
		catch(Exception e) {
			log.info("Unable To getTicketAuditTrails Due to  {}",e.getMessage());
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
			apiResponseDto.setResponseData("");
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		}
	}

	@Override
	public Object getCockpitBubbleStatCount(CockpitBubbleCountStatRequest request) {



		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {

			LocalDateTime startDateTime = LocalDateTime.parse(request.getStartDate(), formatter);
			LocalDateTime endDateTime = LocalDateTime.parse(request.getEndDate(), formatter);

			Aggregation aggregation =null;

			Query query = new Query();
			Query queryStatusComplete = new Query();

			if(!StringUtils.isEmpty(request.getSupervisorId())) {
				aggregation = Aggregation.newAggregation(
						Aggregation.match(Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME)
								.gte(ZonedDateTime.of(startDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())
								.lte(ZonedDateTime.of(endDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())),
						Aggregation.match(Criteria.where(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_SUPERVISOR_ID).is(request.getSupervisorId())),
						Aggregation.match(Criteria.where(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO)),
						Aggregation.group(DispatchControllerConstants.FIELD_GLOBAL_STATUS).count().as(DispatchControllerConstants.WORD_COUNT),
						Aggregation.project(DispatchControllerConstants.WORD_COUNT).and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).previousOperation(),
						Aggregation.sort(Sort.by(DispatchControllerConstants.WORD_COUNT).descending())
						);
				query.addCriteria(Criteria.where(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_SUPERVISOR_ID).is(request.getSupervisorId()));
				query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_PAST_DUE));
				queryStatusComplete.addCriteria(Criteria.where(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_SUPERVISOR_ID).is(request.getSupervisorId()));

			}else {
				aggregation = Aggregation.newAggregation(
						Aggregation.match(Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME)
								.gte(ZonedDateTime.of(startDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())
								.lte(ZonedDateTime.of(endDateTime,ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE)).toLocalDateTime())),
						Aggregation.match(Criteria.where(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO)),
						Aggregation.group(DispatchControllerConstants.FIELD_GLOBAL_STATUS).count().as(DispatchControllerConstants.WORD_COUNT),
						Aggregation.project(DispatchControllerConstants.WORD_COUNT).and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).previousOperation(),
						Aggregation.sort(Sort.by(DispatchControllerConstants.WORD_COUNT).descending())
						);

				query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_PAST_DUE));
			}

			AggregationResults<BubbleCountStatResponse> results = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.TICKET_COLLECTION, BubbleCountStatResponse.class);

			Map<String,Integer> countMap = new HashMap<String, Integer>();

			Long pastDueCount = mongoTemplate.count(query, Long.class, DispatchControllerConstants.TICKET_COLLECTION);

			countMap.put(DispatchControllerConstants.STATUS_OPEN, 0);
			countMap.put(DispatchControllerConstants.STATUS_UNASSIGNED, 0);
			countMap.put(DispatchControllerConstants.STATUS_CANCELLED, 0);
			countMap.put(DispatchControllerConstants.STATUS_COMPLETE, 0);
			countMap.put(DispatchControllerConstants.STATUS_PAST_DUE, pastDueCount.intValue());

			Integer totalCount = 0;
			Integer openCount = 0;
			for(BubbleCountStatResponse data :results.getMappedResults()) {
				if(StringUtils.equalsIgnoreCase(data.getGlobalStatus(), DispatchControllerConstants.STATUS_RESCHEDULE)
						|| StringUtils.equalsIgnoreCase(data.getGlobalStatus(), DispatchControllerConstants.STATUS_ASSIGNED)) {
					openCount = openCount + data.getCount();

				}else if(StringUtils.equalsIgnoreCase(data.getGlobalStatus(), DispatchControllerConstants.STATUS_UNASSIGNED)) {
					countMap.put( DispatchControllerConstants.STATUS_UNASSIGNED, data.getCount());
					totalCount = totalCount + data.getCount();
				}else if(StringUtils.equalsIgnoreCase(data.getGlobalStatus(), DispatchControllerConstants.STATUS_CANCELLED)) {
					countMap.put( DispatchControllerConstants.STATUS_CANCELLED, data.getCount());
					totalCount = totalCount + data.getCount();
//				}else if(StringUtils.equalsIgnoreCase(data.getGlobalStatus(), DispatchControllerConstants.STATUS_COMPLETE)) {
//					countMap.put( DispatchControllerConstants.STATUS_COMPLETE, data.getCount());
//					totalCount = totalCount + data.getCount();
				}else if(StringUtils.equalsIgnoreCase(data.getGlobalStatus(), DispatchControllerConstants.STATUS_MISSING_INFO)) {
					countMap.put( DispatchControllerConstants.STATUS_MISSINGINFO, data.getCount());
					totalCount = totalCount + data.getCount();
				}

			}
			
			

			queryStatusComplete.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_COMPLETE));
			
			queryStatusComplete.addCriteria(Criteria.where(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO));

			queryStatusComplete.addCriteria(Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME)
					.gte(ZonedDateTime.of(startDateTime, ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE))
							.toLocalDateTime())
					.lte(ZonedDateTime.of(endDateTime, ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE))
							.toLocalDateTime()));
			
			queryStatusComplete.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_COMPLETION_DATE_TIME)
						.gte(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));

			long count = mongoTemplate.count(queryStatusComplete, Ticket.class);
			countMap.put( DispatchControllerConstants.STATUS_COMPLETE,(int)count);
			totalCount = totalCount + (int)count;
			totalCount= totalCount + openCount;
			countMap.put(DispatchControllerConstants.STATUS_OPEN, openCount);
			countMap.put(DispatchControllerConstants.WORD_TOTAL, totalCount);

			return countMap;


		}catch(Exception e) {
			log.info("Unable to get getCockpitBubbleStatCount due to : {}",e.getMessage());
			return null;
		}
	}

	@Override
	public long getSupervisorCountByStatus(String supervisorId, String statusAssigned, LocalDateTime startDate,
			LocalDateTime endDate) {
		try {



			Query query = new Query();



			query.addCriteria(Criteria.where(DispatchControllerConstants.SUPERVISORID).is(supervisorId));

			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(statusAssigned));
			
			query.addCriteria(Criteria.where(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO));

			query.addCriteria(Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDate).lte(endDate));
			
			if (StringUtils.equalsIgnoreCase(DispatchControllerConstants.STATUS_COMPLETE, statusAssigned)) {

				query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_COMPLETION_DATE_TIME)
						.gte(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));

			}


			long count = mongoTemplate.count(query, Ticket.class);
			return count;


		}catch(Exception e) {

			log.info("Unable To get Supervisor Count By Status Due to : {}",e.getMessage());
			return 0;

		}
	}

	@Override
	public DataExportFieldResponse getTicketAdvancedSearchFields(AdvanceSearchFieldRequest request) {
		DataExportFieldResponse responseData = new DataExportFieldResponse();
		List<LumenStartColumn> lstdata =new ArrayList<>();
		try {

			MatchOperation match=Aggregation.match(Criteria.where("TechnicianSearch").exists(true));

			ProjectionOperation project = Aggregation.project().andExclude("_id");
			Aggregation aggregation = Aggregation.newAggregation(match, project);

			Object mappedResults = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.TECHNICIAN_SEARCH_COLLECTION, Object.class).getMappedResults().get(0);



			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(mappedResults);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);
			JsonNode ticketSearchNode = rootNode.get("TechnicianSearch");

			for (JsonNode fieldNode : ticketSearchNode) {
				String fieldDisplayLabel = fieldNode.get("fieldDisplayLabel").asText();
				LumenStartColumn column = new LumenStartColumn();
				column.setColumnName(fieldDisplayLabel);
				lstdata.add(column);
			}

			responseData.setLstdata(lstdata);
			responseData.setResult(DispatchControllerConstants.STATUS_SUCCESS);

			return responseData;
		}catch(Exception e) {
			e.getMessage();
			return responseData;
		}
	}

	@Override
	public DataExportFieldResponse getCockpitTicketAdvancedSearchFields(AdvanceSearchFieldRequest request) {
		DataExportFieldResponse responseData = new DataExportFieldResponse();
		List<LumenStartColumn> lstdata =new ArrayList<>();
		try {

			MatchOperation match=Aggregation.match(Criteria.where("TicketSearch").exists(true));

			ProjectionOperation project = Aggregation.project().andExclude("_id");
			Aggregation aggregation = Aggregation.newAggregation(match, project);

			Object mappedResults = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.TICKET_SEARCH_COLLECTION, Object.class).getMappedResults().get(0);



			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(mappedResults);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);
			JsonNode ticketSearchNode = rootNode.get("TicketSearch");

			for (JsonNode fieldNode : ticketSearchNode) {
				String fieldDisplayLabel = fieldNode.get("fieldDisplayLabel").asText();
				LumenStartColumn column = new LumenStartColumn();
				column.setColumnName(fieldDisplayLabel);
				lstdata.add(column);
			}

			responseData.setLstdata(lstdata);
			responseData.setResult(DispatchControllerConstants.STATUS_SUCCESS);

			return responseData;
		}catch(Exception e) {
			e.getMessage();
			return responseData;
		}
	}

	@Override
	public Page<Ticket> globalSearch(String supervisorId, String globalStatus, String searchText,
			LocalDateTime startDateTime, LocalDateTime endDateTime ,Pageable pageable) {
		
	try {
		
		List<Object> response = mongoTemplate.findAll(Object.class, DispatchControllerConstants.TICKET_SEARCH_COLLECTION);
		
		Object ticketSearchObject =  response.get(0);
		
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(ticketSearchObject);

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(json);
		JsonNode ticketSearchNode = rootNode.get("TicketSearch");

		List<Criteria> searchList = new ArrayList<>();

		LocalDateTime todayDate = ZonedDateTime.now(ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE))
				.toLocalDateTime();
		Query query = new Query();
		query.addCriteria(Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME)
				.gte(ZonedDateTime.of(startDateTime, ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE))
						.toLocalDateTime())
				.lte(ZonedDateTime.of(endDateTime, ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE))
						.toLocalDateTime()));
		query.addCriteria(
				Criteria.where(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO));

		if (!StringUtils.isEmpty(supervisorId)) {
			query.addCriteria(Criteria.where(DispatchControllerConstants.SUPERVISORID).is(supervisorId));
		}
		if (!StringUtils.isEmpty(globalStatus)) {

			if (StringUtils.equalsAnyIgnoreCase(DispatchControllerConstants.STATUS_COMPLETE, globalStatus)) {
				// Add criteria for STATUS_COMPLETE with todayDate check
				query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS)
						.is(DispatchControllerConstants.STATUS_COMPLETE)
						.and(DispatchControllerConstants.FIELD_COMPLETION_DATE_TIME).gte(todayDate));
			} else if (StringUtils.equalsAnyIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED, globalStatus)) {
				// Add criteria for STATUS_ASSIGNED
				query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS)
						.in(DispatchControllerConstants.getIncludeOpenGlobalStatusList()));
			} else {
				// Add criteria for other globalStatus values
				query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(globalStatus));

			}

			for (JsonNode fieldNode : ticketSearchNode) {

				String fieldSearchName = fieldNode.get("fieldSearchName").asText();
				
					searchList.add(Criteria.where(fieldSearchName).regex(".*" + searchText + ".*", "i"));
			}
			if (!CollectionUtils.isEmpty(searchList)) {
				Criteria searchCriteria = new Criteria().orOperator(searchList);
				query.addCriteria(searchCriteria);

			}
		} else {
			// Add criteria for STATUS_COMPLETE with todayDate check or other globalStatus
			// values
			Criteria statusComplete = Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS)
					.is(DispatchControllerConstants.STATUS_COMPLETE)
					.and(DispatchControllerConstants.FIELD_COMPLETION_DATE_TIME)
					.gte(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
			Criteria otherStatusCriteria = Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS)
					.in(DispatchControllerConstants.getIncludeGlobalStatusList());
			Criteria finalCriteria = new Criteria().orOperator(statusComplete, otherStatusCriteria);
//			query.addCriteria(finalCriteria);
			
		
			for (JsonNode fieldNode : ticketSearchNode) {

				String fieldSearchName = fieldNode.get("fieldSearchName").asText();
					searchList.add(Criteria.where(fieldSearchName).regex(".*" + searchText + ".*", "i"));
			}
			
				Criteria searchCriteria = new Criteria().orOperator(searchList);
//				query.addCriteria(searchCriteria);

			
			Criteria searchTextCriteria = new Criteria().andOperator(finalCriteria,searchCriteria);
			query.addCriteria(searchTextCriteria);
		}

		Long searchCount = mongoTemplate.count(query, Ticket.class);

		query.with(pageable);

		List<Ticket> ticketList = mongoTemplate.find(query, Ticket.class);

		return new PageImpl<>(ticketList, pageable, searchCount);

	} catch (Exception e) {
		log.info("Unable to excute GlobalSearch where searchText {} Due to {}", searchText, e.getMessage());
		return new PageImpl<>(new ArrayList<>(), pageable, 0);

	}

}
}
