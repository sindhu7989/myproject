package com.straviso.ns.dispatchcontrollercore.multitenancy;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.straviso.ns.dispatchcontrollercore.constants.ServiceConstants;
import com.straviso.ns.dispatchcontrollercore.utils.CommonUtils;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DataSourceBasedMultiTenantConnectionProviderImpl
extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

	@Autowired
	private BCSCaller caller;

	@Autowired
	private CommonUtils commonUtils;

	private static final long serialVersionUID = 1L;

	private String defaultTenant;
	
	@Value("${spring.data.mongodb.database}")
	private String database;
	
	@Value("${service.constants.collection.name}")
	private String collectionName;
	
	@Value("${spring.data.mongodb.uri}")
	private ConnectionString uri;

	private transient Map<String, DataSource> map;

	public DataSourceBasedMultiTenantConnectionProviderImpl(String defaultTenant, Map<String, DataSource> map) {
		super();
		this.defaultTenant = defaultTenant;
		this.map = map;
	}

	@Override
	protected DataSource selectAnyDataSource() {
		return map.get(defaultTenant);
	}

	@Override
	public DataSource selectDataSource(String tenantIdentifier) {
		if (map.containsKey(tenantIdentifier)) {
			return map.get(tenantIdentifier);
		} else {
			createDataSource(tenantIdentifier);
			return map.get(tenantIdentifier);
		}
	}


	public void getDataSource(String tenantIdentifier) {
		if (!map.containsKey(tenantIdentifier)) 
			createDataSource(tenantIdentifier);
	}

	/*public void createDataSource(String tenantIdentifier) {
		log.info("createDataSource() : ");
		BusinessContextIpDto contextIpDto = new BusinessContextIpDto(tenantIdentifier, "",ServiceConstants.NEXUS_PRODUCT_ID);
		BusinessContextDetailOpDto contextDetails = caller.fetchContextDetailsByCriteria(contextIpDto,
				commonUtils.getJWTTokenFromContext());
		contextDetails.getContextDetails().stream().forEach(tc -> {
			if (!StringUtils.isEmpty(tc.getSchemaName()) && tc.getSchemaName().contains(ServiceConstants.NEXUS_SCHEMA_CHAT_DATA) && tc.getDatabaseType().equalsIgnoreCase(ServiceConstants.DATABASE_TYPE_MYSQL)) {
				HikariDataSource newDataSource = new HikariDataSource();
				newDataSource.setPoolName(tc.getBusinessId());
				newDataSource.setUsername(tc.getDatabaseUser());
				newDataSource.setPassword(tc.getDatabasePassword());
				newDataSource.setJdbcUrl(tc.getDatabaseUrl() + tc.getSchemaName()
				+ "?autoReconnect=true&useSSL=false&useLocalSessionState=true&allowPublicKeyRetrieval=true&useUnicode=yes&characterEncoding=UTF-8");
				newDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
				newDataSource.setMinimumIdle(0);
				newDataSource.setMaximumPoolSize(50);
				newDataSource.setIdleTimeout(30001);
				newDataSource.setConnectionTimeout(20000);
				newDataSource.setMaxLifetime(400000);
				newDataSource.setLeakDetectionThreshold(400000);
				map.put(tc.getBusinessId(), newDataSource);

			} 
		});
	}*/
	
	public void createDataSource(String tenantIdentifier) {

        log.info("createDataSource() : ");

        BusinessContextIpDto contextIpDto = new BusinessContextIpDto(tenantIdentifier, "",ServiceConstants.FC_PRODUCT_ID);

        BusinessContextDetailOpDto contextDetails = caller.fetchContextDetailsByCriteria(contextIpDto,

                commonUtils.getJWTTokenFromContext());

        contextDetails.getContextDetails().stream().forEach(tc -> {

            if (!StringUtils.isEmpty(tc.getSchemaName()) && tc.getSchemaName().contains(ServiceConstants.FC_SCHEMA_FIELDSERVE) && tc.getDatabaseType().equalsIgnoreCase(ServiceConstants.DATABASE_TYPE_MYSQL)) {

                HikariDataSource newDataSource = new HikariDataSource();

                newDataSource.setPoolName(tc.getBusinessId());

                newDataSource.setUsername(tc.getDatabaseUser());

                newDataSource.setPassword(tc.getDatabasePassword());

                newDataSource.setJdbcUrl(tc.getDatabaseUrl() + tc.getSchemaName()

                + "?autoReconnect=true&useSSL=false&useLocalSessionState=true&allowPublicKeyRetrieval=true&useUnicode=yes&characterEncoding=UTF-8");

                newDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

                newDataSource.setMinimumIdle(0);

                newDataSource.setMaximumPoolSize(50);

                newDataSource.setIdleTimeout(30001);

                newDataSource.setConnectionTimeout(20000);

                newDataSource.setMaxLifetime(400000);

                newDataSource.setLeakDetectionThreshold(400000);

                map.put(tc.getBusinessId(), newDataSource);

 

            }

        });

    }
	
	public MongoCollection<Document> createChatDataConnection() {
	    MongoClientSettings.Builder settings = MongoClientSettings.builder();
	    settings.applyConnectionString(uri);
	    settings.readPreference(ReadPreference.primaryPreferred());
	    try {
	    MongoClient mongoClient = MongoClients.create(settings.build());
	    MongoDatabase db = mongoClient.getDatabase(database);
	    MongoCollection<Document> collection = db.getCollection(collectionName);
	    return collection;
	    }
	    catch(MongoException e)
	    {
	    	log.info("unable to perform create mongo client due to : {}",  e.getMessage());
	    	return null;
			
	    }
	    
	}

	public DataSource getDefaultDataSource() {
		return map.get(defaultTenant);
	}
}
