package com.straviso.ns.dispatchcontrollercore.dto.request;
import java.util.List;
import lombok.Data;


@Data
public class TicketExportDataRequest {

	private String startDate;

	private String endDate;

	private List<ColumnFilters> filters;

	private String timeZone;
	
	private String userId;
	
	private String reportName;
	
	private Integer id;
	
	private String componentName;
	
	private String fileName;	
	
	private boolean advancedSearchExists;

	private boolean isGlobalSearch;

	private Integer pageSize;	

	private Integer pageNumber;

	private String status;
	
	private List<String> columns;
	
	private String partnerId;
}
