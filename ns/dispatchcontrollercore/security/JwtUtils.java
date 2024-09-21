package com.straviso.ns.dispatchcontrollercore.security;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.straviso.ns.dispatchcontrollercore.dto.response.NorthstarTokenValidationResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class JwtUtils {
	
	@Value("${service.constants.token_validation_url}")
	private String tokenValidationURL;

	public NorthstarTokenValidationResponse validateToken(String token) {
		String validateTokenUrl = tokenValidationURL;
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + token);
		HttpEntity<?> request = new HttpEntity<>(headers);
		
		LocalDateTime startTime=LocalDateTime.now();
		
		NorthstarTokenValidationResponse response = restTemplate
				.exchange(validateTokenUrl, HttpMethod.GET, request, NorthstarTokenValidationResponse.class).getBody();
		
		long timeDiff = Duration.between(startTime,LocalDateTime.now()).toMillis();
		log.info("NorthStar token validation api return response in {} ",timeDiff);
		if (response != null && response.getData() != null && response.getData().isValidToken()) {
			log.info("Is token valid on northstar side? :" + response.getData().isValidToken());
			return response;
		}
		return response;
	}
}
