package com.straviso.ns.dispatchcontrollercore.multitenancy;

import java.util.List;

public class BusinessContextDetailOpDto {

	private List<BusinessContextDetails> contextDetails;

	public List<BusinessContextDetails> getContextDetails() {
		return contextDetails;
	}

	public void setContextDetails(List<BusinessContextDetails> contextDetails) {
		this.contextDetails = contextDetails;
	}

	@Override
	public String toString() {
		return "BusinessContextDetailOpDto [contextDetails=" + contextDetails + "]";
	}

}
