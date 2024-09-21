package com.straviso.ns.dispatchcontrollercore.multitenancy;

public class BusinessTokenContext {

	private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

	private BusinessTokenContext() {
		// No-operation; won't be called
	}

	public static void setBusinessToken(String businessToken) {
		CONTEXT.set(businessToken);
	}

	public static String getBusinessToken() {
		return CONTEXT.get();
	}

	public static void clear() {
		CONTEXT.remove();
	}
}
