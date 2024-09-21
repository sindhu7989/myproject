package com.straviso.ns.dispatchcontrollercore.dto.response;

import com.straviso.ns.dispatchcontrollercore.dto.DataDTO;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicianDTO {

	 private String label;
     private DataDTO data;
     
}
