package com.straviso.ns.dispatchcontrollercore.serviceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.request.NSAuditRequest;
import com.straviso.ns.dispatchcontrollercore.entity.ConstraintConfig;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.DataSourceBasedMultiTenantConnectionProviderImpl;
import com.straviso.ns.dispatchcontrollercore.repository.ConstraintConfigRepo;
import com.straviso.ns.dispatchcontrollercore.repositoryImpl.ConstraintConfigRepoImpl;
import com.straviso.ns.dispatchcontrollercore.service.ConstraintConfigService;
import com.straviso.ns.dispatchcontrollercore.utils.DispatchControllerSupportUtils;

@Service
public class ConstraintConfigServiceImpl implements ConstraintConfigService {

	private static final Logger log = LoggerFactory.getLogger(ConstraintConfigService.class);

	@Autowired
	ConstraintConfigRepo constraintConfigRepo;

	@Autowired
	ConstraintConfigRepoImpl constraintConfigRepoImpl;
	
	@Autowired
	DispatchControllerSupportUtils dispatchControllerSupportUtils;
	
	@Autowired
	DataSourceBasedMultiTenantConnectionProviderImpl dataSourceBasedMultiTenantConnectionProviderImpl;

	@Override
	public ResponseEntity<?> getAllConstraintConfigDetails() {
		try {
			List<ConstraintConfig> result = new ArrayList<>();
			MongoCollection<Document> createChatDataConnection = dataSourceBasedMultiTenantConnectionProviderImpl
					.createChatDataConnection();
			FindIterable<Document> find = createChatDataConnection.find();
			MongoCursor<Document> iterator = find.iterator();
			while (iterator.hasNext()) {
				Document next = iterator.next();
				ConstraintConfig constraintConfig = new ConstraintConfig();
				constraintConfig.set_id(next.getObjectId(DispatchControllerConstants.FIELD_CONFIG_ID).toString());
				constraintConfig.setConstraintId(next.getString(DispatchControllerConstants.FIELD_CONSTRAINT_ID));
				constraintConfig.setConstraintName(next.getString(DispatchControllerConstants.FIELD_CONSTRAINT_NAME));
				constraintConfig.setConstraintScore(next.getString(DispatchControllerConstants.FIELD_CONSTRAINT_SCORE));
				constraintConfig.setConstraintType(next.getString(DispatchControllerConstants.FIELD_CONSTRAINT_TYPE));
				constraintConfig.setIsActive(next.getString(DispatchControllerConstants.FIELD_ISACTIVE));
				constraintConfig.setConstraintTitle(next.getString(DispatchControllerConstants.FIELD_CONSTRAINT_TITLE));
				if (next.getDate(DispatchControllerConstants.FIELD_CREATED_DATE) != null) {
					constraintConfig.setCreatedDate(convertToLocalDateTimeViaInstant(
							next.getDate(DispatchControllerConstants.FIELD_CREATED_DATE)));
				}
				if (next.getDate(DispatchControllerConstants.FIELD_UPDATED_DATE) != null) {
					constraintConfig.setUpdatedDate(convertToLocalDateTimeViaInstant(
							next.getDate(DispatchControllerConstants.FIELD_UPDATED_DATE)));
				}
				constraintConfig.setCreatedBy(next.getString(DispatchControllerConstants.FIELD_CREATED_BY));
				constraintConfig.setUpdatedBy(next.getString(DispatchControllerConstants.FIELD_UPDATED_BY));
				result.add(constraintConfig);
			}
			// Sort the result list by constraintId in ascending order
			 result.sort(Comparator.comparing(ConstraintConfig::getConstraintId));
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			log.info("{} Unable to get all constraint config details due to : {}", BusinessContext.getTenantId(),
					e.getMessage());
			return null;
		}
	}

	public LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

	}
	

	@Override
	public List<Document> updateByConstraintName(List<ConstraintConfig> configDetailsList,String businessId,String businessToken) {
		List<Document> updatedConfigs = new ArrayList<>();
		MongoCollection<Document> createChatDataConnection = dataSourceBasedMultiTenantConnectionProviderImpl
				.createChatDataConnection();
		FindIterable<Document> find = createChatDataConnection.find();
		try {
			for (Document document : find) {
				ObjectId id = document.getObjectId(DispatchControllerConstants.FIELD_CONFIG_ID);
				String constraintName = document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_NAME);
				Optional<ConstraintConfig> matchingDocument = configDetailsList.stream()
						.filter(d -> constraintName.equals(d.getConstraintName())).findFirst();
				if (matchingDocument.isPresent()) {
					StringJoiner preAction = new StringJoiner(",");
					StringJoiner postAction = new StringJoiner(",");
					
					ConstraintConfig match = matchingDocument.get();
					if(!match.getConstraintType().equals(document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_TYPE))) {
						preAction.add(DispatchControllerConstants.FIELD_CONSTRAINT_TYPE+":"+document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_TYPE));
						postAction.add(DispatchControllerConstants.FIELD_CONSTRAINT_TYPE+":"+match.getConstraintType());
					}
					if(!match.getConstraintScore().equals(document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_SCORE))) {
						preAction.add(DispatchControllerConstants.FIELD_CONSTRAINT_SCORE+":"+document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_SCORE));
						postAction.add(DispatchControllerConstants.FIELD_CONSTRAINT_SCORE+":"+match.getConstraintScore());
					}
					if(!match.getIsActive().equals(document.getString(DispatchControllerConstants.FIELD_ISACTIVE))) {
						preAction.add(DispatchControllerConstants.FIELD_ISACTIVE+":"+document.getString(DispatchControllerConstants.FIELD_ISACTIVE));
						postAction.add(DispatchControllerConstants.FIELD_ISACTIVE+":"+match.getIsActive());
					}
					if(!match.getCreatedBy().equals(document.getString(DispatchControllerConstants.FIELD_CREATED_BY))) {
						preAction.add(DispatchControllerConstants.FIELD_CREATED_BY+":"+document.getString(DispatchControllerConstants.FIELD_CREATED_BY));
						postAction.add(DispatchControllerConstants.FIELD_CREATED_BY+":"+match.getCreatedBy());
					}
					if(!match.getUpdatedBy().equals(document.getString(DispatchControllerConstants.FIELD_UPDATED_BY))) {
						preAction.add(DispatchControllerConstants.FIELD_UPDATED_BY+":"+document.getString(DispatchControllerConstants.FIELD_UPDATED_BY));
						postAction.add(DispatchControllerConstants.FIELD_UPDATED_BY+":"+match.getUpdatedBy());
					}
					if(!match.getConstraintTitle().equals(document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_TITLE))) {
						preAction.add(DispatchControllerConstants.FIELD_CONSTRAINT_TITLE+":"+document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_TITLE));
						postAction.add(DispatchControllerConstants.FIELD_CONSTRAINT_TITLE+":"+match.getConstraintTitle());
					}
					Document updateDoc = new Document()
							.append(DispatchControllerConstants.FIELD_CONFIG_ID,document.get(DispatchControllerConstants.FIELD_CONFIG_ID))
							.append(DispatchControllerConstants.FIELD_CONSTRAINT_ID,document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_ID))
							.append(DispatchControllerConstants.FIELD_CONSTRAINT_NAME,document.getString(DispatchControllerConstants.FIELD_CONSTRAINT_NAME))
							.append(DispatchControllerConstants.FIELD_CONSTRAINT_TYPE, match.getConstraintType())
							.append(DispatchControllerConstants.FIELD_CONSTRAINT_SCORE, match.getConstraintScore())
							.append(DispatchControllerConstants.FIELD_ISACTIVE, match.getIsActive())
							.append(DispatchControllerConstants.FIELD_UPDATED_DATE, new Date())
							.append(DispatchControllerConstants.FIELD_CREATED_DATE,document.getDate(DispatchControllerConstants.FIELD_CREATED_DATE))
							.append(DispatchControllerConstants.FIELD_CREATED_BY, match.getCreatedBy())
	                        .append(DispatchControllerConstants.FIELD_UPDATED_BY, match.getUpdatedBy())
							.append(DispatchControllerConstants.FIELD_CONSTRAINT_TITLE,match.getConstraintTitle());
					
					createChatDataConnection.findOneAndReplace(new Document(DispatchControllerConstants.FIELD_CONSTRAINT_NAME,
							document.get(DispatchControllerConstants.FIELD_CONSTRAINT_NAME)),updateDoc);
									
					updateDoc.append(DispatchControllerConstants.FIELD_CONFIG_ID,document.get(DispatchControllerConstants.FIELD_CONFIG_ID).toString());
					updatedConfigs.add(updateDoc);

					log.info("Updated ConstraintConfig with constraintName: {}", id.toString());
					
					//Audit Table
					log.info("Calling NsAudit Api....");
					
					String remarks=postAction.toString()+" "+"is updated By"+" "+match.getUpdatedBy();
					
					NSAuditRequest nsAuditRequest=new NSAuditRequest(null,null,"DcSolverConfig",updateDoc.getString(DispatchControllerConstants.FIELD_CONSTRAINT_ID),
							null,"SolverConfigUpdate",null,"SystemDC",preAction.toString(),postAction.toString(),match.getUpdatedBy(),0.0,0.0,remarks,"");

					dispatchControllerSupportUtils.callNSAuditSave(nsAuditRequest,businessId,businessToken);
					
				} else {
					log.warn("ConstraintConfig with constraintName {} not found in the ConstraintConfigDetailsList",constraintName);
				}
			}
		} catch (Exception e) {
			log.info("Unable to updating ConstraintConfig due to : {}", e.getMessage());
			return Collections.emptyList();
		}
		return updatedConfigs;
	}
}
