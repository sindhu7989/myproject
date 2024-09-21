package com.straviso.ns.dispatchcontrollercore.multitenancy;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.straviso.ns.dispatchcontrollercore.constants.ServiceConstants;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@SuppressWarnings("squid:S00112")
public class BCSCaller implements Serializable {
	
	@Value("${service.constants.bcs.api_url}")
	private String bcsAPIURL;

	private static final long serialVersionUID = 1L;

	public BusinessContextDetailOpDto fetchContextDetailsByCriteria(BusinessContextIpDto contextIpDto, String token) {
		try {
			String bcsurl = bcsAPIURL;
			//contextIpDto.setProductId(ServiceConstants.NEXUS_PRODUCT_ID);
			//contextIpDto.setProductId(ServiceConstants.FC_PRODUCT_ID);
			
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);
			log.info("Request in fetchContextDetailsByCriteria " + contextIpDto.toString());
			headers.setContentType(MediaType.APPLICATION_JSON);
			restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());

			HttpEntity<BusinessContextIpDto> entity = new HttpEntity<>(contextIpDto, headers);

			ResponseEntity<BusinessContextDetailOpDto> response = restTemplate.postForEntity(bcsurl, entity,
					BusinessContextDetailOpDto.class);

			log.info("Response code for BCS context details: " + response.getStatusCode());

			if (response.getStatusCode() != HttpStatus.OK) {
				throw new RuntimeException(
						"Unable to call BCS context details API due to HTTP status code : " + response.getStatusCode());
			}
			//Unable to call BCS context details API due to HTTP status code : 
//			log.info("BCS API Response is : " + response.getBody().toString());
			return response.getBody();
		} catch (Exception e) {
			log.info("Unable to call BCS context details API due to : " + e);
		}
		return null;
	}
}
