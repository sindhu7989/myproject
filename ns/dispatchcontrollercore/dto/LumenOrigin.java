package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class LumenOrigin {

	private String originatingApp = "SelfTriggered";
    private String channel = "SM";
}
