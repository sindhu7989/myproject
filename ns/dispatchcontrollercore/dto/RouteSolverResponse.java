package com.straviso.ns.dispatchcontrollercore.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class RouteSolverResponse {

    public List<RouteSolverPath> paths = new ArrayList<>();
}