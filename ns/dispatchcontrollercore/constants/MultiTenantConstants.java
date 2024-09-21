package com.straviso.ns.dispatchcontrollercore.constants;

import org.springframework.stereotype.Component;

@Component
public interface MultiTenantConstants {

	String DEFAULT_TENANT_ID = "default";
	String CURRENT_TENANT_IDENTIFIER = "CURRENT_TENANT_IDENTIFIER";
	String TENANT_KEY = "TENANT-KEY";
	String AUTHORIZATION = "Authorization";
	String BUSINESS_ID = "businessId";
	String JWT_TOKEN = "JWT_TOKEN";
	String BUSINESS_ID_SIGNATURE = "X-Business-Id";
	String USER_EMAIL = "USER_EMAIL";
	String USER_NAME = "USER_NAME";
	String KEYWORD_LUMEN = "Lumen_";
	String KEYWORD_CUSTOM = "_Custom";
	String DATABASE_TYPE_MONGO = "MongoDB";
	String KEYWORD_NEXUS = "Nexus";
	String KEYWORD_AZU = "AZU";
}
