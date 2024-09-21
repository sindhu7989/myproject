package com.straviso.ns.dispatchcontrollercore.repositoryImpl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout.Group;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.MyTeamWorkloadTechnicianDetails;
import com.straviso.ns.dispatchcontrollercore.dto.request.ColumnFilters;
import com.straviso.ns.dispatchcontrollercore.dto.request.TicketExportDataRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.AgentListResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianStatusCountResponse;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.repository.AgentRepository;

import lombok.extern.log4j.Log4j2;

@Repository
@Transactional
@Log4j2
public class AgentRepositoryImpl implements AgentRepository {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public Object getTopTechnicianDataByFieldName(ToptechnicianCountRequest request) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {


			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE)
							.is(DispatchControllerConstants.FLAG_Y)
							.andOperator(Criteria.where(DispatchControllerConstants.FIELD_AGENT_TYPE)
									.is(DispatchControllerConstants.AGENT_TYPE_ACTUAL))),
					Aggregation.match(Criteria.where(request.getFieldName()).exists(true).ne(null)
							.andOperator(Criteria.where(request.getFieldName()).ne(""))),
					Aggregation.group(request.getFieldName()).count().as(DispatchControllerConstants.WORD_COUNT),
					Aggregation.project(DispatchControllerConstants.WORD_COUNT).and(request.getFieldName()).previousOperation(),
					Aggregation.sort(Sort.by(DispatchControllerConstants.WORD_COUNT).descending()),
					Aggregation.limit(5)
					);
			
			AggregationResults<Object> results = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.TECHNICIAN_DC_SOLVER_COLLECTION, Object.class);

			return results.getMappedResults();

		}catch(Exception e) {
			log.info("{} Unable to get Ticket Count Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;
		}

	}

	@Override
	public Object getTechnicianCountByStatus() {

		try {
			Aggregation aggregation = Aggregation.newAggregation(
					Aggregation.match(Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE)
							.is(DispatchControllerConstants.STATUS_ACTIVE)
							.andOperator(Criteria.where(DispatchControllerConstants.FIELD_AGENT_TYPE)
									.is(DispatchControllerConstants.AGENT_TYPE_ACTUAL))),
					Aggregation.group(DispatchControllerConstants.FIELD_TECHINICAIN_AVAILABILITY_STATUS).count().as(DispatchControllerConstants.WORD_COUNT),
					Aggregation.project(DispatchControllerConstants.WORD_COUNT).and(DispatchControllerConstants.FIELD_AVAILABILITY_STATUS).previousOperation(),
					Aggregation.sort(Sort.by(DispatchControllerConstants.WORD_COUNT).descending())
					);

			AggregationResults<TechnicianStatusCountResponse> results = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.TECHNICIAN_DC_SOLVER_COLLECTION, 
					TechnicianStatusCountResponse.class);

			if(CollectionUtils.isEmpty(results.getMappedResults())) {
				return null;
			}

			Map<String, Integer> response = new LinkedHashMap<>();
			Integer count=0;
			for(TechnicianStatusCountResponse data :results) {
				
				
				if(!StringUtils.isEmpty(data.getAvailabilityStatus())) {
					count = count+ data.getCount();
					response.put(data.getAvailabilityStatus(), data.getCount());
				}
				
			}
			response.put(DispatchControllerConstants.WORD_TOTAL, count);
			return response;

		}catch(Exception e) {
			log.info("{} Unable to get Technician Count Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;
		}

	}
	@Override
	public ResponseEntity<ApiResponseDto> getTechnicianJsonView(String technicianId) {
	    MongoDatabase database = null;
	    ApiResponseDto apiResponseDto =new ApiResponseDto();
	    try {
	        log.info("Get Technician Data JSON Operation Started for TechnicianId: {}", technicianId);
	        /*if (mongoData.get(BusinessContext.getTenantId()) != null) {
	        	  log.info("IF block ");
	            database = mongoData.get(BusinessContext.getTenantId());
	        } else {
	        	log.info("else block ");
	            database = multiTenantMongoDbFactory.getMongoDatabase();
	            mongoData.put(BusinessContext.getTenantId(), database);
	        }*/
	        
	        MongoCollection<Document> collection =  mongoTemplate.getCollection(DispatchControllerConstants.TECHNICIAN_DC_SOLVER_COLLECTION);
	        Document document = new Document();
	        document.append(DispatchControllerConstants.TECHNICIANID, technicianId);
	        FindIterable<Document> find = collection.find(document);
	        
	        if(find!=null && find.first()  !=null)
	        {
	            // Convert to a JSON object
	            ObjectMapper objectMapper = new ObjectMapper();
	            JsonNode jsonNode = objectMapper.valueToTree(find.first().toJson());
	            
	            
	            if(jsonNode !=null) {
	            	apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
	        		apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
	        		apiResponseDto.setResponseData(jsonNode);
	        		}
	            
	            return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	        }
	        else
	        {
	        	apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
	        	apiResponseDto.setMessage("No Data Found for the given technicianId");
	        }
	    } catch (Exception e) {
	        log.info("{} Unable To Get technician Data Due to {}",DispatchControllerConstants.GET_TECHNICIAN_DATA_JSON_REQUEST, e.getMessage());
	    }
	    return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

	@Override
	public MyTeamWorkloadTechnicianDetails getTechnicianTicketCount(Agent technician, LocalDateTime startDate,
			LocalDateTime endDate) {



        log.info("Fetching getTechnicianStatusCounts data for {}" ,technician.getTechnicianId());
        MyTeamWorkloadTechnicianDetails myTeamWorkloadTechnicianDetails = new MyTeamWorkloadTechnicianDetails();

          
              Criteria criteria = Criteria.where(DispatchControllerConstants.TECHNICIANID).is(technician.getTechnicianId())
            		  .and(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO);
              if (startDate != null && endDate != null) {
                  criteria = criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDate).lte(endDate);
              }

              AggregationResults<Document> results = mongoTemplate.aggregate(
                  Aggregation.newAggregation(
                      Aggregation.match(criteria),
                      Aggregation.group(DispatchControllerConstants.FIELD_GLOBAL_STATUS)
                          .count().as("count")
                  ),
                  DispatchControllerConstants.TICKET_COLLECTION,
                  Document.class
              );

              Map<String, Integer> statusCountMap = new HashMap<>();
              results.getMappedResults().forEach(doc -> {
                  String globalStatus = doc.getString("_id");
                  Integer count = doc.getInteger("count");;
                  statusCountMap.put(globalStatus, count);
              });
              
            Query query = new Query();

  			query.addCriteria(Criteria.where(DispatchControllerConstants.TECHNICIANID).is(technician.getTechnicianId()));

  			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_COMPLETE));
  			
  			query.addCriteria(Criteria.where(DispatchControllerConstants.IS_ASSIST_TICKET).is(DispatchControllerConstants.NO));

  			query.addCriteria(Criteria.where(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDate).lte(endDate));
  			
  		    query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_COMPLETION_DATE_TIME)
  						.gte(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)));

  			long count = mongoTemplate.count(query, Ticket.class);

              myTeamWorkloadTechnicianDetails.setTechId(technician.getTechnicianId());
              myTeamWorkloadTechnicianDetails.setOpen(statusCountMap.getOrDefault(DispatchControllerConstants.STATUS_RESCHEDULE, 0) + statusCountMap.getOrDefault(DispatchControllerConstants.STATUS_ASSIGNED, 0));
              myTeamWorkloadTechnicianDetails.setComplete((int)count);
              myTeamWorkloadTechnicianDetails.setAvailableMins(technician.getRemainingWorkTime());
              myTeamWorkloadTechnicianDetails.setSupervisorId(technician.getSupervisorId());
              myTeamWorkloadTechnicianDetails.setTechName(technician.getFirstName()+" "+technician.getLastName());
             
          return myTeamWorkloadTechnicianDetails;
      
		
	}

	@Override
	public Integer getTicketCountByCriteria(TicketExportDataRequest request) {

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

			for(ColumnFilters filter:request.getFilters()) {
				for (JsonNode fieldNode : ticketSearchNode) {
					String fieldDisplayLabel = fieldNode.get("fieldDisplayLabel").asText();
					String fieldSearchName = fieldNode.get("fieldSearchName").asText();

					if(StringUtils.equalsIgnoreCase(fieldDisplayLabel, filter.getColumn())) {
						filter.setColumn(fieldSearchName);
					}

				}
			}
			
			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDateTimeDefault = LocalDateTime.parse(request.getStartDate(),inputFormatter);
			LocalDateTime endDateTimeDefault = LocalDateTime.parse(request.getEndDate(),inputFormatter);

			Query query = new Query();
			
			query.addCriteria(Criteria.where(DispatchControllerConstants.TIMESTAMP).gte(startDateTimeDefault).lt(endDateTimeDefault));
			
			for (ColumnFilters item : request.getFilters()) {

				if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						query.addCriteria(Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString())));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						query.addCriteria(Criteria.where(item.getColumn()).gte(startDateTime).lt(endDateTime));
					}
					else {

						query.addCriteria(Criteria.where(item.getColumn()).regex("^"+item.getValue().toString()+"$", "i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS_NOT)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						query.addCriteria(Criteria.where(item.getColumn()).ne(Integer.parseInt(item.getValue().toString())));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						Criteria dateCriteria = new Criteria().orOperator(
								Criteria.where(item.getColumn()).lt(startDateTime),
								Criteria.where(item.getColumn()).gt(endDateTime)
								);

						query.addCriteria(dateCriteria);
					}else {

						query.addCriteria(Criteria.where(item.getColumn()).regex("^(?!.*\\b" + item.getValue() + "\\b).*$", "i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_CONTAINS)){

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						query.addCriteria(Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString())));

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						query.addCriteria(Criteria.where(item.getColumn()).gte(startDateTime).lt(endDateTime));
					}else {

						query.addCriteria(Criteria.where(item.getColumn()).regex(".*"+item.getValue().toString()+".*","i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IN)){

					String values = item.getValue().toString();
					  String[] items = values.split(",");
					  List<String> parameterList = Arrays.asList(items);
					
					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						
						 List<Long> longList = new ArrayList<>();
						  for (String data : items) {
					                Long value = Long.parseLong(data.trim());
					                longList.add(value);
					        }
						  
						  query.addCriteria(Criteria.where(item.getColumn()).in(longList));
						

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						Criteria dateRangeCriteria = new Criteria();
						for(String dateValue:parameterList) {
							LocalDate date = LocalDate.parse(dateValue,inputFormatter);
							
							 dateRangeCriteria.orOperator(Criteria.where(item.getColumn()).
										gte(date.atStartOfDay()).lt(date.atTime(LocalTime.MAX)));
						}
						
						query.addCriteria(dateRangeCriteria);
					}else {
						
						query.addCriteria(Criteria.where(item.getColumn()).in(parameterList));
					}

				}

			}

			
			Long count = mongoTemplate.count(query, Long.class, DispatchControllerConstants.TECHNICIAN_ASSIGNMENT_SOLUTION);

			return count.intValue();

		}catch(Exception e) {
			e.getMessage();
			log.info("{} Unable to get Advance Search Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;
		}}

	@Override
	public List<AgentAssignmentSolutionModel> getTechnicianListData(TicketExportDataRequest request, Integer pageNumberLoop) {

		try {
			
			
			MatchOperation match=Aggregation.match(Criteria.where("TechnicianSearch").exists(true));

			ProjectionOperation project = Aggregation.project().andExclude("_id");
			Aggregation aggregationfilter = Aggregation.newAggregation(match, project);

			Object mappedResults = mongoTemplate.aggregate(aggregationfilter,DispatchControllerConstants.TECHNICIAN_SEARCH_COLLECTION, Object.class).getMappedResults().get(0);


			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(mappedResults);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);
			JsonNode ticketSearchNode = rootNode.get("TechnicianSearch");

			for(ColumnFilters filter:request.getFilters()) {
				for (JsonNode fieldNode : ticketSearchNode) {
					String fieldDisplayLabel = fieldNode.get("fieldDisplayLabel").asText();
					String fieldSearchName = fieldNode.get("fieldSearchName").asText();

					if(StringUtils.equalsIgnoreCase(fieldDisplayLabel, filter.getColumn())) {
						filter.setColumn(fieldSearchName);
					}

				}
			}
			
			List<AggregationOperation> stages = new ArrayList<>();


			Integer pageSize = request.getPageSize() ;
			Integer pageNumber = pageNumberLoop ;

			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDateTimeDefault = LocalDateTime.parse(request.getStartDate(),inputFormatter);
			LocalDateTime endDateTimeDefault = LocalDateTime.parse(request.getEndDate(),inputFormatter);
			
			stages.add(Aggregation.match(Criteria.where(DispatchControllerConstants.TIMESTAMP).
					gte(startDateTimeDefault).
					lt(endDateTimeDefault)));
			
			for (ColumnFilters item : request.getFilters()) {

				if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString()))
								));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						stages.add(Aggregation.match(Criteria.where(item.getColumn()).
								gte(startDateTime).lt(endDateTime)));
					}
					else {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).regex("^"+item.getValue().toString()+"$", "i")
								));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS_NOT)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).ne(Integer.parseInt(item.getValue().toString()))
								));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						Criteria dateCriteria = new Criteria().orOperator(
								Criteria.where(item.getColumn()).lt(startDateTime),
								Criteria.where(item.getColumn()).gt(endDateTime)
								);

						stages.add(Aggregation.match(dateCriteria));
					}else {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).regex("^(?!.*\\b" + item.getValue() + "\\b).*$", "i")
								));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_CONTAINS)){

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString()))
								));

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						stages.add(Aggregation.match(Criteria.where(item.getColumn()).
								gte(startDateTime).lt(endDateTime)));
					}else {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).regex(".*"+item.getValue().toString()+".*","i")
								));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IN)){

					String values = item.getValue().toString();
					  String[] items = values.split(",");
					  List<String> parameterList = Arrays.asList(items);
					
					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						
						 List<Long> longList = new ArrayList<>();
						  for (String data : items) {
					                Long value = Long.parseLong(data.trim());
					                longList.add(value);
					        }
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).in(longList)
								));
						
						

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						Criteria dateRangeCriteria = new Criteria();
						for(String dateValue:parameterList) {
							LocalDate date = LocalDate.parse(dateValue,inputFormatter);
							
							 dateRangeCriteria.orOperator(Criteria.where(item.getColumn()).
										gte(date.atStartOfDay()).lt(date.atTime(LocalTime.MAX)));
						}
						

						stages.add(Aggregation.match(dateRangeCriteria));
					}else {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).in(parameterList)
								));
					}

				}

			}
			
			stages.add(Aggregation.sort(Sort.by("_id").ascending()));
			stages.add(Aggregation.skip((long) pageNumber * pageSize));
			stages.add(Aggregation.limit(pageSize));
			Aggregation aggregation = Aggregation.newAggregation(stages);		
			
			AggregationResults<AgentAssignmentSolutionModel> results = mongoTemplate.aggregate(aggregation, DispatchControllerConstants.TECHNICIAN_ASSIGNMENT_SOLUTION, AgentAssignmentSolutionModel.class);

			return results.getMappedResults();

		}catch(Exception e) {
			
			log.info("{} Unable to get Advance Search Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;
		}

	}

	@Override
	public Integer getCockpitTicketCountByCriteria(TicketExportDataRequest request) {

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

			for(ColumnFilters filter:request.getFilters()) {
				for (JsonNode fieldNode : ticketSearchNode) {
					String fieldDisplayLabel = fieldNode.get("fieldDisplayLabel").asText();
					String fieldSearchName = fieldNode.get("fieldSearchName").asText();

					if(StringUtils.equalsIgnoreCase(fieldDisplayLabel, filter.getColumn())) {
						filter.setColumn(fieldSearchName);
					}

				}
			}
			
			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDateTimeDefault = LocalDateTime.parse(request.getStartDate(),inputFormatter);
			LocalDateTime endDateTimeDefault = LocalDateTime.parse(request.getEndDate(),inputFormatter);

			Query query = new Query();
			
			query.addCriteria(Criteria.where(DispatchControllerConstants.CREATEDDATETIME).gte(startDateTimeDefault).lt(endDateTimeDefault));
			
			for (ColumnFilters item : request.getFilters()) {

				if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						query.addCriteria(Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString())));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						query.addCriteria(Criteria.where(item.getColumn()).gte(startDateTime).lt(endDateTime));
					}
					else {

						query.addCriteria(Criteria.where(item.getColumn()).regex("^"+item.getValue().toString()+"$", "i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS_NOT)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						query.addCriteria(Criteria.where(item.getColumn()).ne(Integer.parseInt(item.getValue().toString())));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						Criteria dateCriteria = new Criteria().orOperator(
								Criteria.where(item.getColumn()).lt(startDateTime),
								Criteria.where(item.getColumn()).gt(endDateTime)
								);

						query.addCriteria(dateCriteria);
					}else {

						query.addCriteria(Criteria.where(item.getColumn()).regex("^(?!.*\\b" + item.getValue() + "\\b).*$", "i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_CONTAINS)){

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						query.addCriteria(Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString())));

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						query.addCriteria(Criteria.where(item.getColumn()).gte(startDateTime).lt(endDateTime));
					}else {

						query.addCriteria(Criteria.where(item.getColumn()).regex(".*"+item.getValue().toString()+".*","i"));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IN)){

					String values = item.getValue().toString();
					  String[] items = values.split(",");
					  List<String> parameterList = Arrays.asList(items);
					
					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						
						 List<Long> longList = new ArrayList<>();
						  for (String data : items) {
					                Long value = Long.parseLong(data.trim());
					                longList.add(value);
					        }
						  
						  query.addCriteria(Criteria.where(item.getColumn()).in(longList));
						

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						Criteria dateRangeCriteria = new Criteria();
						for(String dateValue:parameterList) {
							LocalDate date = LocalDate.parse(dateValue,inputFormatter);
							
							 dateRangeCriteria.orOperator(Criteria.where(item.getColumn()).
										gte(date.atStartOfDay()).lt(date.atTime(LocalTime.MAX)));
						}
						
						query.addCriteria(dateRangeCriteria);
					}else {
						
						query.addCriteria(Criteria.where(item.getColumn()).in(parameterList));
					}

				}

			}

			
			Long count = mongoTemplate.count(query, Long.class, DispatchControllerConstants.TICKET_COLLECTION);

			return count.intValue();

		}catch(Exception e) {
			e.getMessage();
			log.info("{} Unable to get Advance Search Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;
		}}

	@Override
	public List<Ticket> getTicketListData(TicketExportDataRequest request,
			Integer pageNumberLoop) {

		try {
			
			
			MatchOperation match=Aggregation.match(Criteria.where("TicketSearch").exists(true));

			ProjectionOperation project = Aggregation.project().andExclude("_id");
			Aggregation aggregationfilter = Aggregation.newAggregation(match, project);

			Object mappedResults = mongoTemplate.aggregate(aggregationfilter,DispatchControllerConstants.TICKET_SEARCH_COLLECTION, Object.class).getMappedResults().get(0);


			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(mappedResults);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(json);
			JsonNode ticketSearchNode = rootNode.get("TicketSearch");

			for(ColumnFilters filter:request.getFilters()) {
				for (JsonNode fieldNode : ticketSearchNode) {
					String fieldDisplayLabel = fieldNode.get("fieldDisplayLabel").asText();
					String fieldSearchName = fieldNode.get("fieldSearchName").asText();

					if(StringUtils.equalsIgnoreCase(fieldDisplayLabel, filter.getColumn())) {
						filter.setColumn(fieldSearchName);
					}

				}
			}
			
			List<AggregationOperation> stages = new ArrayList<>();


			Integer pageSize = request.getPageSize() ;
			Integer pageNumber = pageNumberLoop ;

			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			LocalDateTime startDateTimeDefault = LocalDateTime.parse(request.getStartDate(),inputFormatter);
			LocalDateTime endDateTimeDefault = LocalDateTime.parse(request.getEndDate(),inputFormatter);
			
			stages.add(Aggregation.match(Criteria.where(DispatchControllerConstants.CREATEDDATETIME).
					gte(startDateTimeDefault).
					lt(endDateTimeDefault)));
			
			for (ColumnFilters item : request.getFilters()) {

				if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString()))
								));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						stages.add(Aggregation.match(Criteria.where(item.getColumn()).
								gte(startDateTime).lt(endDateTime)));
					}
					else {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).regex("^"+item.getValue().toString()+"$", "i")
								));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IS_NOT)) {

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).ne(Integer.parseInt(item.getValue().toString()))
								));
					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						Criteria dateCriteria = new Criteria().orOperator(
								Criteria.where(item.getColumn()).lt(startDateTime),
								Criteria.where(item.getColumn()).gt(endDateTime)
								);

						stages.add(Aggregation.match(dateCriteria));
					}else {

						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).regex("^(?!.*\\b" + item.getValue() + "\\b).*$", "i")
								));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_CONTAINS)){

					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).is(Long.valueOf(item.getValue().toString()))
								));

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						LocalDate date = LocalDate.parse(item.getValue().toString(),inputFormatter);
						LocalDateTime startDateTime = date.atStartOfDay();
						LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

						stages.add(Aggregation.match(Criteria.where(item.getColumn()).
								gte(startDateTime).lt(endDateTime)));
					}else {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).regex(".*"+item.getValue().toString()+".*","i")
								));
					}

				}else if(StringUtils.equalsIgnoreCase(item.getOperator(), DispatchControllerConstants.OPERATOR_IN)){

					String values = item.getValue().toString();
					  String[] items = values.split(",");
					  List<String> parameterList = Arrays.asList(items);
					
					if(DispatchControllerConstants.longFieldList.contains(item.getColumn())) {
						
						 List<Long> longList = new ArrayList<>();
						  for (String data : items) {
					                Long value = Long.parseLong(data.trim());
					                longList.add(value);
					        }
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).in(longList)
								));
						
						

					}else if(DispatchControllerConstants.dateFieldList.contains(item.getColumn())) {

						Criteria dateRangeCriteria = new Criteria();
						for(String dateValue:parameterList) {
							LocalDate date = LocalDate.parse(dateValue,inputFormatter);
							
							 dateRangeCriteria.orOperator(Criteria.where(item.getColumn()).
										gte(date.atStartOfDay()).lt(date.atTime(LocalTime.MAX)));
						}
						

						stages.add(Aggregation.match(dateRangeCriteria));
					}else {
						stages.add(Aggregation.match(
								Criteria.where(item.getColumn()).in(parameterList)
								));
					}

				}

			}
			
			stages.add(Aggregation.sort(Sort.by("_id").ascending()));
			stages.add(Aggregation.skip((long) pageNumber * pageSize));
			stages.add(Aggregation.limit(pageSize));
			Aggregation aggregation = Aggregation.newAggregation(stages);		
			
			AggregationResults<Ticket> results = mongoTemplate.aggregate(aggregation, DispatchControllerConstants.TICKET_COLLECTION, Ticket.class);

			return results.getMappedResults();

		}catch(Exception e) {
			
			log.info("{} Unable to get Advance Search Details, for businessId : {} , due to {}", BusinessContext.getTenantId(),e.getMessage());
			return null;
		}

	}

	public AgentListResponse findAllData(Pageable pageable, String isActive) {
		
		Query query = new Query();
		
		List<AggregationOperation> stages = new ArrayList<>();
		ProjectionOperation project = Aggregation.project().andExclude("ticketList").andExclude("_id");
		
		
		if (!StringUtils.equalsIgnoreCase(isActive, "All")) {
			stages.add(Aggregation.match(
					Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE).is(DispatchControllerConstants.FLAG_Y)
					));
			
			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE).is(DispatchControllerConstants.FLAG_Y));
		}
		
		stages.add(Aggregation.sort(Sort.by("_id").ascending()));
		stages.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
		stages.add(Aggregation.limit(pageable.getPageSize()));
		stages.add(project);
		Aggregation aggregation = Aggregation.newAggregation(stages);
		
		query.fields().exclude("ticketList").exclude("_id");
		
		Long count = mongoTemplate.count(query, Long.class, DispatchControllerConstants.TECHNICIAN_DC_SOLVER_COLLECTION);

		
		List<Object> mappedResults = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.TECHNICIAN_DC_SOLVER_COLLECTION, Object.class).getMappedResults();
		
		AgentListResponse  response= new AgentListResponse();
		response.setCount(Integer.valueOf(count.toString()));
		response.setResponseList(mappedResults);
		
		return response;
	}
	
	
public AgentListResponse findAllSupervisorData(Pageable pageable, String isActive) {
		
		Query query = new Query();
		
		List<AggregationOperation> stages = new ArrayList<>();
		
		
		
		if (!StringUtils.equalsIgnoreCase(isActive, "All")) {
			stages.add(Aggregation.match(
					Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE).is(DispatchControllerConstants.FLAG_Y)
					));
			
			query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_ISACTIVE).is(DispatchControllerConstants.FLAG_Y));
		}
		
		ProjectionOperation project = Aggregation.project().andExclude("ticketList");
		stages.add(project);
		stages.add(Aggregation.sort(Sort.by("_id").ascending()));
		stages.add(Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()));
		stages.add(Aggregation.limit(pageable.getPageSize()));
		
		Aggregation aggregation = Aggregation.newAggregation(stages);
		
		Long count = mongoTemplate.count(query, Long.class, DispatchControllerConstants.SUPERVISOR_POLYGONMAPPING_DCSOLVER);

		
		List<Object> mappedResults = mongoTemplate.aggregate(aggregation,DispatchControllerConstants.SUPERVISOR_POLYGONMAPPING_DCSOLVER, Object.class).getMappedResults();
		
		AgentListResponse  response= new AgentListResponse();
		response.setCount(Integer.valueOf(count.toString()));
		response.setResponseList(mappedResults);
		
		return response;
	}




}
