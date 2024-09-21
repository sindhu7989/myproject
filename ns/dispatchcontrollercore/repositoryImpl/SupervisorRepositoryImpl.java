package com.straviso.ns.dispatchcontrollercore.repositoryImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.GroupLayout.Group;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.TicketCountReportsIpDto;
import com.straviso.ns.dispatchcontrollercore.dto.request.ToptechnicianCountRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.FieldCountResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.StatusByMonth;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianStatusCountResponse;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.mongodb.MultiTenantMongoDbFactory;
import com.straviso.ns.dispatchcontrollercore.repository.AgentRepository;

import lombok.extern.log4j.Log4j2;

@Repository
@Transactional
@Log4j2
public class SupervisorRepositoryImpl  {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public static ConcurrentMap<String, MongoDatabase> mongoData = new ConcurrentHashMap<>();
	
	@Autowired
	private MultiTenantMongoDbFactory multiTenantMongoDbFactory;

	

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

	public ResponseEntity<ApiResponseDto> getSupervisorJsonView(String supervisorId) {
		 MongoDatabase database = null;
		    ApiResponseDto apiResponseDto =new ApiResponseDto();
		    try {
		        log.info("Get supervisor Data JSON Operation Started for supervisorId: {}", supervisorId);
		        /*if (mongoData.get(BusinessContext.getTenantId()) != null) {
		        	  log.info("IF block ");
		            database = mongoData.get(BusinessContext.getTenantId());
		        } else {
		        	log.info("else block ");
		            database = multiTenantMongoDbFactory.getMongoDatabase();
		            mongoData.put(BusinessContext.getTenantId(), database);
		        }*/
		        
		        MongoCollection<Document> collection =  mongoTemplate.getCollection(DispatchControllerConstants.SUPERVISOR_POLYGONMAPPING_DCSOLVER);
		        Document document = new Document();
		        document.append(DispatchControllerConstants.SUPERVISORID, supervisorId);
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
		        	apiResponseDto.setMessage("No Data Found for the given supervisorId");
		        }
		    } catch (Exception e) {
		        log.info("{} Unable To Get supervisor Data Due to {}",DispatchControllerConstants.GET_SUPERVISOR_DATA_JSON_REQUEST, e.getMessage());
		    }
		    return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
	}

}
