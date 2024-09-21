package com.straviso.ns.dispatchcontrollercore.repositoryImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.straviso.ns.dispatchcontrollercore.entity.ConstraintConfig;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;

@Repository
public class ConstraintConfigRepoImpl {

	@Autowired
	private MongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(ConstraintConfig.class);

	public ConstraintConfig updateByConstraintName(ConstraintConfig constraintConfig) {
		try {
			Query query = Query.query(Criteria.where("constraintName").is(constraintConfig.getConstraintName()));
			Update update = new Update().set("constraintId", constraintConfig.getConstraintId())
					.set("constraintType", constraintConfig.getConstraintType())
					.set("constraintScore", constraintConfig.getConstraintScore())
					.set("isActive", constraintConfig.getIsActive());

			mongoTemplate.updateMulti(query, update, ConstraintConfig.class);
			return constraintConfig;
		} catch (Exception e) {
			log.info("{} Unable to updateByConstraintName for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			return null;
		}
	}

	public ConstraintConfig getByConstraintName(String constraintName) {
		try {
			Query query = Query.query(Criteria.where("constraintName").is(constraintName));
			ConstraintConfig constraintConfig = mongoTemplate.findOne(query, ConstraintConfig.class);
			return constraintConfig;
		} catch (Exception e) {
			log.info("{} Unable to get the updated result for businessId : {} , due to {}",
					BusinessContext.getTenantId(), e.getMessage());
			return null;
		}
	}

}
