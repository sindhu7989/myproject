package com.straviso.ns.dispatchcontrollercore.dto.request;




import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianStatusCount {

	private String technicianId;
	@JsonProperty(value="Open")
	private int open;
	@JsonProperty(value="Complete")
	private int completed;

}
