package com.straviso.ns.dispatchcontrollercore.utils;

import java.io.Serializable;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.straviso.ns.dispatchcontrollercore.constants.MultiTenantConstants;

@Component
public class CommonUtils implements Serializable {

	private static final long serialVersionUID = 1L;

	public String getJWTTokenFromContext() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		return ((String) requestAttributes.getAttribute(MultiTenantConstants.JWT_TOKEN,
				RequestAttributes.SCOPE_REQUEST));
	}
	
}
