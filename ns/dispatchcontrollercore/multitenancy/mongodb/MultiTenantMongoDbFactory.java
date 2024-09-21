package com.straviso.ns.dispatchcontrollercore.multitenancy.mongodb;

import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.straviso.ns.dispatchcontrollercore.constants.MultiTenantConstants;
import com.straviso.ns.dispatchcontrollercore.constants.ServiceConstants;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BCSCaller;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContextDetailOpDto;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContextDetails;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContextIpDto;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.CurrentTenantIdentifierResolverImpl;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MultiTenantMongoDbFactory extends SimpleMongoClientDatabaseFactory {

	public String DEFAULT_DB = "chat_data";
	@Value("${service.constants.bcs.api_url}")
	private String bcsAPIURL;

	@Autowired
	CurrentTenantIdentifierResolverImpl identifierResolver;
	
	@Autowired
	private BCSCaller caller;

	public static ConcurrentHashMap<String, String> mongoSchemaConfig = new ConcurrentHashMap<>();

	public MultiTenantMongoDbFactory(MongoClient mongoClient, String dbName) throws UnknownHostException {
		super(mongoClient, dbName);
	}

	@Override
	public MongoDatabase getMongoDatabase() throws DataAccessException {

		String tenant = identifierResolver.resolveCurrentTenantIdentifier();

		if (tenant != null) {

			MongoClient mongoClient = null;

			if (MongoDbConfiguration.mongoData.containsKey(tenant)) {

				mongoClient = MongoDbConfiguration.mongoData.get(tenant);
				if (mongoSchemaConfig.containsKey(tenant)) {
					return mongoClient.getDatabase(mongoSchemaConfig.get(tenant));
				}

			} else {

				BusinessContextIpDto contextIpDto = new BusinessContextIpDto(tenant, "MongoDB",
						ServiceConstants.NEXUS_PRODUCT_ID);

				BusinessContextDetailOpDto contextDetails = caller.fetchContextDetailsByCriteria(contextIpDto, BusinessTokenContext.getBusinessToken());

				if (contextDetails != null && contextDetails.getContextDetails().size() > 0) {
					Optional<BusinessContextDetails> optionalContextDetails = contextDetails.getContextDetails()
							.stream().filter(cd -> cd.getSchemaName().contains("Nexus")).findFirst();
					if (optionalContextDetails.isPresent()) {
						BusinessContextDetails businessContextDetails = optionalContextDetails.get();
						MongoClientSettings.Builder settings = MongoClientSettings.builder();					

						MongoCredential credentials = MongoCredential.createCredential(
								businessContextDetails.getDatabaseUser(), businessContextDetails.getSchemaName(),
								// "admin",
								businessContextDetails.getDatabasePassword().toCharArray());
						settings.credential(credentials);
						ConnectionString uri ;
						if(businessContextDetails.getIsCluster() || "AZU".equalsIgnoreCase(businessContextDetails.getDbServiceType())) {
							uri = new ConnectionString(businessContextDetails.getDatabaseUrl()); 
						}else {
							StringBuilder connectionString = new StringBuilder(
									String.format("mongodb://%s:%d", businessContextDetails.getHostIP(), 27017));
							uri = new ConnectionString(connectionString.toString());
						}
						settings.applyConnectionString(uri);

						settings.readPreference(ReadPreference.primaryPreferred());

						mongoClient = MongoClients.create(settings.build());						
						MongoDbConfiguration.mongoData.put(tenant, mongoClient);
						mongoSchemaConfig.put(tenant, businessContextDetails.getSchemaName());
						return mongoClient.getDatabase(businessContextDetails.getSchemaName());
					} else {
						log.info("Data not found in BCS ");
						return super.getMongoDatabase(DEFAULT_DB);
					}
				} else {
					log.info("Data not found in BCS ");
					return super.getMongoDatabase(DEFAULT_DB);
				}

			}
		}

		return super.getMongoDatabase(DEFAULT_DB);

	}
	
	public MongoDatabase getConnnectionWithCustomSchema() {

		String tenant = identifierResolver.resolveCurrentTenantIdentifier();

		String lumenSchema = tenant + MultiTenantConstants.KEYWORD_CUSTOM  ;

		if (tenant != null) {

			MongoClient mongoClient = null;

			BusinessContextIpDto contextIpDto = new BusinessContextIpDto(tenant, MultiTenantConstants.DATABASE_TYPE_MONGO,

					ServiceConstants.NEXUS_PRODUCT_ID);

			BusinessContextDetailOpDto contextDetails = caller.fetchContextDetailsByCriteria(contextIpDto, BusinessTokenContext.getBusinessToken());

			if (contextDetails != null && contextDetails.getContextDetails().size() > 0) {
				Optional<BusinessContextDetails> optionalContextDetails = contextDetails.getContextDetails()
						.stream().filter(cd -> cd.getSchemaName().contains(MultiTenantConstants.KEYWORD_NEXUS)).findFirst();

				if (optionalContextDetails.isPresent()) {

					BusinessContextDetails businessContextDetails = optionalContextDetails.get();

					MongoClientSettings.Builder settings = MongoClientSettings.builder();                    

					MongoCredential credentials = MongoCredential.createCredential(

							businessContextDetails.getDatabaseUser(), lumenSchema,

							// "admin",

							businessContextDetails.getDatabasePassword().toCharArray());

					settings.credential(credentials);

					ConnectionString uri ;

					if(businessContextDetails.getIsCluster() || MultiTenantConstants.KEYWORD_AZU.equalsIgnoreCase(businessContextDetails.getDbServiceType())) {

						uri = new ConnectionString(businessContextDetails.getDatabaseUrl());

					}else {

						StringBuilder connectionString = new StringBuilder(

								String.format("mongodb://%s:%d", businessContextDetails.getHostIP(), 27017));

						uri = new ConnectionString(connectionString.toString());

					}

					settings.applyConnectionString(uri);

					settings.readPreference(ReadPreference.primaryPreferred());

					mongoClient = MongoClients.create(settings.build());                        

					return mongoClient.getDatabase(lumenSchema);

				} else {

					log.info("Data not found in BCS ");

					return super.getMongoDatabase(DEFAULT_DB);
				}

			} else {

				log.info("Data not found in BCS ");

				return super.getMongoDatabase(DEFAULT_DB);
			}

		}
		return super.getMongoDatabase(DEFAULT_DB);

	}
}
