package com.straviso.ns.dispatchcontrollercore.multitenancy.mongodb;

import java.net.UnknownHostException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

@Configuration
@Component
public class MongoDbConfiguration {

	@Value("${spring.data.mongodb.uri}")
	String mongodbUri;

	public static HashMap<String, MongoClient> mongoData = new HashMap<>();

	@Bean
	public SimpleMongoClientDatabaseFactory mongoDbFactory() throws UnknownHostException {
		MongoClient client = MongoClients.create(mongodbUri);
		mongoData.put("nexus", client);
		return new MultiTenantMongoDbFactory(client, "chat_data");
	}

	@Bean
	public MongoTemplate mongoTemplate() throws UnknownHostException {
		return new MongoTemplate(mongoDbFactory());
	}

	@Bean
	public GridFSBucket getGridFSBucket(MongoDatabaseFactory factory) {
		MongoDatabase database = factory.getMongoDatabase();
		GridFSBucket bucket = GridFSBuckets.create(database);
		return bucket;
	}
	
	@Bean
	@Qualifier("taskExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(4);
		executor.setThreadNamePrefix("default_task_executor_thread");
		executor.initialize();
		return executor;
	}

}
