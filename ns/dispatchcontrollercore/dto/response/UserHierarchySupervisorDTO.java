package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.util.List;

import com.straviso.ns.dispatchcontrollercore.dto.DataDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHierarchySupervisorDTO {
	 private String label; 
     private DataDTO data;
     private List<TechnicianDTO> children;
}
