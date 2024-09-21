package com.straviso.ns.dispatchcontrollercore.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;

import com.straviso.ns.dispatchcontrollercore.dto.ENVProperties;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
	protected static final ConcurrentMap<String, String> propertiesList = new ConcurrentHashMap<>();

	@Value("${spring.datasource.jdbc-url}")
	String url;

	@Value("${spring.datasource.username}")
	String username;

	@Value("${spring.datasource.password}")
	String password;

	@Value("${spring.datasource.dbcp2.driver-class-name}")
	String driver;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		log.info("Inside Fetch Database properties from db: "+url);
		List<ENVProperties> properties = new ArrayList<>();

		try {
			Class.forName(driver);
			connectDBAndFetchProperties(url, username, password, properties);
		} catch (Exception e) {
			log.info("Unable to connect to the database while fetching DB properties: " + e);
		}
	}

	public void connectDBAndFetchProperties(String url, String username, String password,
			List<ENVProperties> properties) {
		try (Connection con = DriverManager.getConnection(url, username, password);
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery("select * from environmentpropeties;")) {
			log.info("Connected to DB and Fetching Database properties");
			while (rs.next()) {
				ENVProperties envproperties = new ENVProperties();
				envproperties.setId(rs.getInt(1));
				if (rs.getString(2) != null) {
					envproperties.setProperties(rs.getString(2));
				}
				if (rs.getString(3) != null) {
					envproperties.setValue(rs.getString(3));
				}
				properties.add(envproperties);
			}
			convertToMap(properties);
		} catch (Exception e) {
			log.info("Unable to connect to the database while fetching DB properties: " + e);
		}
	}

	public ConcurrentMap<String, String> convertToMap(List<ENVProperties> properties) {
		if (!CollectionUtils.isEmpty(properties)) {
			for (ENVProperties envProperties : properties) {
				propertiesList.put(envProperties.getProperties(), envProperties.getValue());
			}
		}
		return propertiesList;
	}

	public static String getProperty(String propertyName) {
		return StartupApplicationListener.propertiesList.get(propertyName);
	}

}
