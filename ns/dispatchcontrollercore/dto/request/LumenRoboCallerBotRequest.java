package com.straviso.ns.dispatchcontrollercore.dto.request;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.straviso.ns.dispatchcontrollercore.dto.LumenAdditionalInfo;
import com.straviso.ns.dispatchcontrollercore.dto.LumenOrigin;
import com.straviso.ns.dispatchcontrollercore.dto.LumenUser;

import lombok.Data;

@Data
public class LumenRoboCallerBotRequest {
	
	private LumenUser user;
	private LumenOrigin origin;
	
	@JsonProperty(value = "AdditionalInfoValues")
	private List<LumenAdditionalInfo> additionalInfoValues = new ArrayList<>();

}
