package com.straviso.ns.dispatchcontrollercore.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.straviso.ns.dispatchcontrollercore.dto.response.UserHierarchySupervisorDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "userHierarchy")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserHierarchyModel {
	
	@Id
	private String _id;
	private  List<UserHierarchySupervisorDTO> userHierarchy = new ArrayList<>();

}
