package com.straviso.ns.dispatchcontrollercore.dto;

 

import java.util.ArrayList;
import java.util.List;

 

import com.fasterxml.jackson.annotation.JsonProperty;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;

 

import lombok.Data;

 

@Data
public class RouteSolverRequest {

 

    private String profile = DispatchControllerConstants.PROFILE_CAR;
    private boolean elevation = true;
    private boolean debug = false;
    private boolean instructions = false; 
    private String locale = DispatchControllerConstants.LOCALE_EN_US;
    private String optimize = DispatchControllerConstants.FLAG_FALSE;

    @JsonProperty(value = "points_encoded")
    private boolean pointsEncoded = true;

    private List<String> details = DispatchControllerConstants.getRSDetailsList();
    private List<List<Double>> points = new ArrayList<>();

    @JsonProperty(value = "snap_preventions")
    private List<String> snapPreventions = DispatchControllerConstants.getRSSnapPreventionsList();
}