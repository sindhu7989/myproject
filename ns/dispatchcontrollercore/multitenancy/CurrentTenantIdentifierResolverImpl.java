package com.straviso.ns.dispatchcontrollercore.multitenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.straviso.ns.dispatchcontrollercore.constants.MultiTenantConstants;

@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver {

	private static String defaultTenantId = "CLINK";

	@Override
	public String resolveCurrentTenantIdentifier() {
	
//		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
//		if (requestAttributes != null) {
//			String tenantId = (String) requestAttributes.getAttribute(MultiTenantConstants.CURRENT_TENANT_IDENTIFIER,
//					RequestAttributes.SCOPE_REQUEST);
//			if (tenantId != null) {
//				return tenantId;				
//			}
//		}
		if(BusinessContext.getTenantId()!=null) {
			return BusinessContext.getTenantId();
		}else{
			return defaultTenantId;
		}
	}

	public void forceCurrentTenantIndetifier(String tenant) {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes != null) {
			requestAttributes.setAttribute(MultiTenantConstants.CURRENT_TENANT_IDENTIFIER, tenant,
					RequestAttributes.SCOPE_REQUEST);
		}
	}

	@Override
	public boolean validateExistingCurrentSessions() {

		return true;
	}

	public static void setDefaultTenantForScheduledTasks(String tenantId) {
		defaultTenantId = tenantId;
	}

}
