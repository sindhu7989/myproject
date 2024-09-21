package com.straviso.ns.dispatchcontrollercore.multitenancy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Defines the multi-tenant datasource configuration.
 */
@Entity
@Table(name = "business_context_details_t")
public class BusinessContextDetails {

	@Id
	@GeneratedValue
	private Long id;
	private String businessId;
	private String productId;
	private String hostName;
	private String hostIP;
	private String databaseType;
	private String databaseUrl;
	private String databaseUser;
	private String databasePassword;
	private String appBaseURL;
	private String regionId;
	private String schemaName;
	private String databasePort;
	private String createdDate;
	private String updatedDate;
	private String createdBy;
	private String updatedBy;
	private Boolean isCluster; 
    private String dbServiceType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public String getAppBaseURL() {
		return appBaseURL;
	}

	public void setAppBaseURL(String appBaseURL) {
		this.appBaseURL = appBaseURL;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getDatabasePort() {
		return databasePort;
	}

	public void setDatabasePort(String databasePort) {
		this.databasePort = databasePort;
	}

	public Boolean getIsCluster() {
		return isCluster;
	}

	public void setIsCluster(Boolean isCluster) {
		this.isCluster = isCluster;
	}

	public String getDbServiceType() {
		return dbServiceType;
	}

	public void setDbServiceType(String dbServiceType) {
		this.dbServiceType = dbServiceType;
	}

	@Override
	public String toString() {
		return "BusinessContextDetails [id=" + id + ", businessId=" + businessId + ", productId=" + productId
				+ ", hostName=" + hostName + ", hostIP=" + hostIP + ", databaseType=" + databaseType + ", databaseUrl="
				+ databaseUrl + ", databaseUser=" + databaseUser + ", databasePassword=" + databasePassword
				+ ", appBaseURL=" + appBaseURL + ", regionId=" + regionId + ", schemaName=" + schemaName
				+ ", databasePort=" + databasePort + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate
				+ ", createdBy=" + createdBy + ", updatedBy=" + updatedBy + ", isCluster=" + isCluster
				+ ", dbServiceType=" + dbServiceType + "]";
	}

}