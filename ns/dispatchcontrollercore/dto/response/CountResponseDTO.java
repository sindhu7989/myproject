package com.straviso.ns.dispatchcontrollercore.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings({ "squid:S1068", "squid:S1948" })

public class CountResponseDTO  implements Serializable {

	private static final long serialVersionUID = 1L;
	private String responseText = "OK";
	private Object responseData;
	private Integer totalPages;
	private Long totalElements;
	
	@Override
	public String toString() {
	    return new com.google.gson.Gson().toJson(this);
	}
}

