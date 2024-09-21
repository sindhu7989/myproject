package com.straviso.ns.dispatchcontrollercore.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.constants.ServiceConstants;
import com.straviso.ns.dispatchcontrollercore.dto.request.ExportMetaDataUpdateRequest;
import com.straviso.ns.wholesale.core.dto.response.ExportMetaDataUpdateResponse;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Component
public class ExportDataSupportUtils {

	private transient RestTemplate restTemplate;

	public ExportDataSupportUtils() {
		restTemplate = new RestTemplate();
	}
	
	public ExportMetaDataUpdateResponse callDataExportToUpdateFileDetails(ExportMetaDataUpdateRequest request, String businessToken) {
		ExportMetaDataUpdateResponse output = new ExportMetaDataUpdateResponse();
		try {
			String dataExportHttpurl = StartupApplicationListener.getProperty(ServiceConstants.NORTHSTAR_DATA_EXPORT_BASE_URL)
					+ StartupApplicationListener.getProperty(ServiceConstants.NS_UPDATEFILE_DETAILS_API);
			log.info("Call Data Export To Update File Details, Url : {} and Request {}", dataExportHttpurl, request);
			HttpHeaders headers = new HttpHeaders();
			headers.add(DispatchControllerConstants.AUTHORIZATION_CONSTANT, DispatchControllerConstants.BEARER_CONSTANT + businessToken);
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dataExportHttpurl);
			HttpEntity<ExportMetaDataUpdateRequest> entity = new HttpEntity<>(request, headers);

			ResponseEntity<ExportMetaDataUpdateResponse> response = restTemplate.exchange(builder.toUriString(),
					HttpMethod.POST, entity, ExportMetaDataUpdateResponse.class);
			output = response.getBody();
			if (response.getStatusCodeValue() != 200) {
				log.info("Unable to update File details for request : {} Calling Data Export with output: {}",request,output);
			}
			log.info("Output from Data Export To Update File Details, where request : {} , statusCode: {} is : {} ",request, 
					response.getStatusCodeValue(), output);
			return output;
		} catch (Exception e) {
			log.info("Unable to calling Data Export To Update File Details due to : {} " + e.getMessage());
			output.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			output.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			return output;
		}
		
		
	}
}
