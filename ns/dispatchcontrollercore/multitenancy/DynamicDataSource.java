package com.straviso.ns.dispatchcontrollercore.multitenancy;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import com.zaxxer.hikari.HikariDataSource;

@Component
public class DynamicDataSource {

	@Value("${spring.datasource.jdbc-url}")
	String url;

	@Value("${spring.datasource.username}")
	String username;

	@Value("${spring.datasource.password}")
	String password;

	@Value("${spring.datasource.dbcp2.driver-class-name}")
	String driver;
	
	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		HikariDataSource newDataSource=null;	
		
		newDataSource = new HikariDataSource();
		newDataSource.setPoolName("NexusChatAuditAPP");
		newDataSource.setUsername(username);
		newDataSource.setPassword(password);
		newDataSource.setJdbcUrl(url);
		newDataSource.setDriverClassName(driver);
		newDataSource.setMinimumIdle(10);
		newDataSource.setMaximumPoolSize(50);
		newDataSource.setIdleTimeout(10001);
		newDataSource.addDataSourceProperty("useSSL", false);
		newDataSource.addDataSourceProperty("allowPublicKeyRetrieval", true);
		
		return newDataSource;

	}
}
