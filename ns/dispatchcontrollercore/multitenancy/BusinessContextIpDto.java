package com.straviso.ns.dispatchcontrollercore.multitenancy;

public class BusinessContextIpDto {

	private String businessId;
	private String databaseType;
	private String productId;

	public BusinessContextIpDto() {
		super();
	}

	public BusinessContextIpDto(String businessId, String databaseType, String productId) {
		super();
		this.businessId = businessId;
		this.databaseType = databaseType;
		this.productId = productId;
	}

	public BusinessContextIpDto(String databaseType, String productId) {
		super();
		this.databaseType = databaseType;
		this.productId = productId;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	@Override
	public String toString() {
		return "BusinessContextIpDto [businessId=" + businessId + ", databaseType=" + databaseType + ", productId="
				+ productId + "]";
	}
}
