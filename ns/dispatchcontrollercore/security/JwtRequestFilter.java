package com.straviso.ns.dispatchcontrollercore.security;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.constants.MultiTenantConstants;
import com.straviso.ns.dispatchcontrollercore.dto.response.NorthstarTokenValidationResponse;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.DataSourceBasedMultiTenantConnectionProviderImpl;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUserDetailsService jwtUserDetailsService;

	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	@Qualifier("multitenantProvider")
	private DataSourceBasedMultiTenantConnectionProviderImpl dataImpl;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			if (StringUtils.isNotBlank((request.getHeader("Authorization")))) {
				String jwtToken = getBearerToken(request);
				NorthstarTokenValidationResponse northstarTokenValidationResult = jwtUtils.validateToken(jwtToken);
				if (northstarTokenValidationResult.getData().isValidToken()) {					
					log.info(
							"Request for TenantId:  " + northstarTokenValidationResult.getData().getBusinessId());
					request.setAttribute(MultiTenantConstants.CURRENT_TENANT_IDENTIFIER,
							northstarTokenValidationResult.getData().getBusinessId());
					request.setAttribute(MultiTenantConstants.JWT_TOKEN, jwtToken);
					request.setAttribute(MultiTenantConstants.USER_EMAIL, northstarTokenValidationResult.getData().getEmailId());
					String transactionId = UUID.randomUUID().toString();
					request.setAttribute(DispatchControllerConstants.TRANSCATIONID, transactionId);

					log.info("T-ID : {} , method :{}", transactionId, request.getMethod());
					log.info("T-ID : {} , Request URL: {} ", transactionId, request.getRequestURI());
					log.info("T-ID : {} , Request is https: {} ", transactionId, request.isSecure());
					
					BusinessContext
					.setTenantId(northstarTokenValidationResult.getData().getBusinessId());
					BusinessTokenContext.setBusinessToken(jwtToken);
					dataImpl.getDataSource(northstarTokenValidationResult.getData().getBusinessId());


					String username = northstarTokenValidationResult.getData().getFirstName();
					UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					/**
					 * After setting the Authentication in the context, we specify that the current
					 * user is authenticated. So it passes the Spring Security Configurations
					 * successfully.
					 */
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				}
			}
		} catch (ServletException e) {
			log.info("Missing or invalid Authorization header: " + e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (final Exception e) {
			log.info("Unable to logging in with security token: " + e.getMessage());
			response.setContentLength(0);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}

		chain.doFilter(request, response);
	}

	/**
	 * Get the bearer token from the HTTP request. The token is in the HTTP request
	 * "Authorization" header in the form of: "Bearer [token]"
	 */
	private String getBearerToken(HttpServletRequest request) throws ServletException {
		String authHeader = request.getHeader("Authorization");
		log.info("Authentication header: " + authHeader);

		String token = authHeader;
		if (authHeader != null && (authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer "))) {
			token = authHeader.substring(7);
		}
		return token;
	}
}
