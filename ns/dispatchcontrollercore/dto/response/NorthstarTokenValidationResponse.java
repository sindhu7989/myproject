package com.straviso.ns.dispatchcontrollercore.dto.response;

import com.straviso.ns.dispatchcontrollercore.dto.NorthstarTokenValidationData;
import com.straviso.ns.dispatchcontrollercore.dto.NorthstarTokenValidationStatus;

public class NorthstarTokenValidationResponse {

	private NorthstarTokenValidationStatus status;
	private NorthstarTokenValidationData data;
	public NorthstarTokenValidationStatus getStatus() {
		return status;
	}
	public void setStatus(NorthstarTokenValidationStatus status) {
		this.status = status;
	}
	public NorthstarTokenValidationData getData() {
		return data;
	}
	public void setData(NorthstarTokenValidationData data) {
		this.data = data;
	}
}
