package com.straviso.ns.dispatchcontrollercore.multitenancy;

@SuppressWarnings("common-java:DuplicatedBlocks")
public final class BusinessContext {

	private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

	private BusinessContext() {
		// No-operation; won't be called
	}
	
	public static void setTenantId(String tenantId) {
		CONTEXT.set(tenantId);
	}

	public static String getTenantId() {
		return CONTEXT.get();
	}

	public static void clear() {
		CONTEXT.remove();
	}
}
