package com.straviso.ns.dispatchcontrollercore.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSolverPath {

    private double distance;  // in Meters
    private double weight;
    private long time;  // in milliSeconds
    private List<Object> instructions;

}