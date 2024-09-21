package com.straviso.ns.dispatchcontrollercore.multitenancy;

import java.util.HashMap;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import com.straviso.ns.dispatchcontrollercore.constants.MultiTenantConstants;


@Configuration
public class MultitenancyConfiguration {

	@Bean(name = "multitenantProvider")
	public DataSourceBasedMultiTenantConnectionProviderImpl dataSourceBasedMultiTenantConnectionProvider() {
		HashMap<String, DataSource> dataSources = new HashMap<String, DataSource>();

		dataSources.put(MultiTenantConstants.DEFAULT_TENANT_ID, dataSource());

		return new DataSourceBasedMultiTenantConnectionProviderImpl(MultiTenantConstants.DEFAULT_TENANT_ID, dataSources);
	}

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}



	//    @Bean(name = "multitenantProvider")
	//    public DataSourceBasedMultiTenantConnectionProviderImpl dataSourceBasedMultiTenantConnectionProvider() {
	//        HashMap<String, DataSource> dataSources = new HashMap<String, DataSource>();
	//        BusinessContextIpDto contextIpDto=new BusinessContextIpDto("MySQL", "BUZZ");
	//     
	//        BusinessContextDetailOpDto contextDetails = MultiTenantConstants.fetchContextDetailsByCriteria(contextIpDto);
	//        
	//        contextDetails.getContextDetails().stream().forEach(tc -> dataSources.put(tc.getBusinessId(), DataSourceBuilder
	//                .create()
	//                .driverClassName("com.mysql.jdbc.Driver")
	//                .username(tc.getDatabaseUser())
	//                .password(tc.getDatabasePassword())
	//                .url(tc.getDatabaseUrl()).build()));
	//        
	//        return new DataSourceBasedMultiTenantConnectionProviderImpl(MultiTenantConstants.DEFAULT_TENANT_ID, dataSources);
	//    }

	@Bean
	@DependsOn("multitenantProvider")
	public DataSource defaultDataSource() {
		return dataSourceBasedMultiTenantConnectionProvider().getDefaultDataSource();
	}

}
