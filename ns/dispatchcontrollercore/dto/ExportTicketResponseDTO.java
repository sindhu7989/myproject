package com.straviso.ns.dispatchcontrollercore.dto;
import lombok.Data;

@Data
public class ExportTicketResponseDTO {

	private Integer statusCode;
	private String statusMessage;
	private Long totalElements;
	private Integer totalPages ;
}
