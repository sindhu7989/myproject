package com.straviso.ns.dispatchcontrollercore.utils;

import java.util.ArrayList;
import java.util.*;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.apache.commons.lang3.StringUtils;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.LumenAdditionalInfo;
import com.straviso.ns.dispatchcontrollercore.dto.LumenOrigin;
import com.straviso.ns.dispatchcontrollercore.dto.LumenUser;
import com.straviso.ns.dispatchcontrollercore.dto.RouteSolverPath;
import com.straviso.ns.dispatchcontrollercore.dto.RouteSolverRequest;
import com.straviso.ns.dispatchcontrollercore.dto.RouteSolverResponse;
import com.straviso.ns.dispatchcontrollercore.dto.request.BuzzSendNotificationRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.FCMSendNotificationRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.LumenRoboCallerBotRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.NSAuditRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;
import com.straviso.ns.dispatchcontrollercore.repository.SupervisorRepository;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.Location;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;


import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DispatchControllerSupportUtils {
	
	@Value("${service.constants.dcs.route_solver_url}")
    String routeSolverUrl;
	
	@Value("${service.constants.lumen.bot.robo_caller_url}")
	private String lumenBotRoboCallerUrl;
	
	@Value("${service.constants.dcs.fcm.send_notification}")
    String fcmSendNotificationUrl;
	
	@Value("${service.constants.dcs.buzz.send_notification}")
    String buzzSendNotificationUrl;
	
	@Value("${service.constants.ns.save_audit_url}")
    String nsAuditSaveUrl;
	
	@Autowired(required = true)
    @Qualifier(value = "taskExecutor")
    private ThreadPoolTaskExecutor executor;
	
	@Autowired
	  SupervisorRepository supervisorPolygonMappingRepository;
	
	@Autowired
	DataConvertorUtils dataConvertorUtils;
	
	public ResponseEntity<ResponseDTO> generateResponse(Integer statusCode,	String statusMessage,Object responseData,HttpStatus httpStatus) {
		return new ResponseEntity<>(new ResponseDTO(statusCode, statusMessage,responseData), httpStatus);
	}
	
	public long calculateDistance(Location from, Location to) {
       // return DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME;
         try {
             RouteSolverRequest routeSolverRequest = new RouteSolverRequest();
             List<Double> fromPoint = new ArrayList<>();
             List<Double> toPoint = new ArrayList<>();

             if(from.getLongitude() == to.getLongitude() && from.getLatitude() == to.getLatitude()) {
                 return 0;
             }
             fromPoint.add(from.getLongitude());
             fromPoint.add(from.getLatitude());

             toPoint.add(to.getLongitude());
             toPoint.add(to.getLatitude());

             routeSolverRequest.getPoints().add(fromPoint);
             routeSolverRequest.getPoints().add(toPoint);

             return getRouteDetails(routeSolverRequest).getPaths().get(0).getTime();
         }catch(Exception e) {
             log.info("Unable to get Distance Time for location : {}  to {}, due to {}",from, to , e.getMessage());
             return DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME;
         }
     }
	
	public RouteSolverPath getLocationDistanceDetails(Location from, Location to) {
		try {
			RouteSolverRequest routeSolverRequest = new RouteSolverRequest();
			List<Double> fromPoint = new ArrayList<>();
			List<Double> toPoint = new ArrayList<>();

			if(from.getLongitude() == to.getLongitude() && from.getLatitude() == to.getLatitude()) {
				return new RouteSolverPath(0.0,0.0,0,null);
			}
			fromPoint.add(from.getLongitude());
			fromPoint.add(from.getLatitude());

			toPoint.add(to.getLongitude());
			toPoint.add(to.getLatitude());

			routeSolverRequest.getPoints().add(fromPoint);
			routeSolverRequest.getPoints().add(toPoint);

			return getRouteDetails(routeSolverRequest).getPaths().get(0);
		}catch(Exception e) {
			log.info("Unable to get Distance Time for location : {}  to {}, due to {}",from, to , e.getMessage());
			return new RouteSolverPath(0.0,0.0,DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME,null);
		}
	}


     public RouteSolverResponse getRouteDetails(RouteSolverRequest routeSolverRequest) {

         RouteSolverResponse routeSolverResponse = new RouteSolverResponse();
         log.info("Calling Route Solver API , url : {} , request : {}",routeSolverUrl,routeSolverRequest);
         try {
             RestTemplate rest = new RestTemplate();

             HttpHeaders headers = new HttpHeaders();
             HttpEntity<?> request = new HttpEntity<>(routeSolverRequest, headers);

             ResponseEntity<RouteSolverResponse> response = rest.exchange(routeSolverUrl, HttpMethod.POST, request, RouteSolverResponse.class);
             log.info("response of Route Solver API  : " + response);
             return response.getBody();
         }catch(Exception e) {
             log.info("Unable to Calling Route Solver API , url : {}, due to : {}",routeSolverUrl, e.getMessage());
             return routeSolverResponse;
         }
     }
     
     public void callRoboCallerBot(LumenRoboCallerBotRequest lumenRoboCallerBotRequest, String token, String businessId) {
 		log.info("Calling roboCaller Bot , url : {} , request : {}",lumenBotRoboCallerUrl,lumenRoboCallerBotRequest);
         try {
             RestTemplate rest = new RestTemplate();
             HttpHeaders headers = new HttpHeaders();
           
             headers.add("Authorization", "Bearer "+token+"");
             HttpEntity<?> request = new HttpEntity<>(lumenRoboCallerBotRequest, headers);
             ResponseEntity<Object> response = rest.exchange(lumenBotRoboCallerUrl, HttpMethod.POST, request, Object.class);
            
             log.info("response of roboCaller Bot : " + response.getBody());
            
         }catch(Exception e) {
             log.info("{} : Unable to call roboCaller Bot , url : {}, due to : {}",lumenBotRoboCallerUrl, e.getMessage());
            
         }
 	}

     public long getTenDigitRandomNumber(long min, long max) {
	        return (long) ((Math.random() * (max - min)) + min);
	}
     
     public void callFCMSendNotification(FCMSendNotificationRequest fcmSendNotificationRequest,String businessId , String businessToken) {

         

         log.info("Calling FCM Send Notification API , url : {} , request : {}",fcmSendNotificationUrl,fcmSendNotificationRequest);

         try {

             RestTemplate rest = new RestTemplate();

             HttpHeaders headers = new HttpHeaders();

             headers.add("Authorization", "Bearer "+businessToken);

             HttpEntity<?> request = new HttpEntity<>(fcmSendNotificationRequest, headers);

  

             ResponseEntity<String> response = rest.exchange(fcmSendNotificationUrl, HttpMethod.POST, request, String.class);

             log.info("response of FCM Send Notification API  : " + response);

         }catch(Exception e) {

             log.info("Unable to call FCM Send Notification API , url : {}, due to : {}",fcmSendNotificationUrl, e.getMessage());

         }

     }
     public void callBuzzSendNotification(BuzzSendNotificationRequest buzzSendNotificationRequest, String businessId,

             String businessToken) {

         

         log.info("Calling Buzz Send Notification API , url : {} , request : {}",buzzSendNotificationUrl,buzzSendNotificationRequest);

         try {

             RestTemplate rest = new RestTemplate();

             HttpHeaders headers = new HttpHeaders();

             headers.add("Authorization", "Bearer "+businessToken);

             HttpEntity<?> request = new HttpEntity<>(buzzSendNotificationRequest, headers);

  

             ResponseEntity<String> response = rest.exchange(buzzSendNotificationUrl, HttpMethod.POST, request, String.class);

             log.info("response of Buzz Send Notification API  : " + response);

         }catch(Exception e) {

             log.info("Unable to call Buzz Send Notification API , url : {}, due to : {}",buzzSendNotificationUrl, e.getMessage());

         }

     }
     
     public void callNSAuditSave(NSAuditRequest nsAuditRequest, String businessId,

             String businessToken) {

         

         log.info("Calling NS Audit Save API , url : {} , request : {}",nsAuditSaveUrl,nsAuditRequest);
         log.info("businessId : {} ",businessId);
         log.info("businessToken : {} ",businessToken);

         try {

             RestTemplate rest = new RestTemplate();

             HttpHeaders headers = new HttpHeaders();

             headers.add("Authorization", "Bearer "+businessToken);

             HttpEntity<?> request = new HttpEntity<>(nsAuditRequest, headers);

  

             ResponseEntity<String> response = rest.exchange(nsAuditSaveUrl, HttpMethod.POST, request, String.class);

             log.info("Response of NS Audit Save API  : " + response);

         }catch(Exception e) {

             log.info("Unable to call NS Audit Save API , url : {}, due to : {}",nsAuditSaveUrl, e.getMessage());

         }

     }
     public void notifySupervisorAndTech(Agent agent,Ticket ticket, String assignmentStatus,String businessId, String businessToken) {
    	 
    	//String ticketExternalIdModified = getCommaSeparateCharOfString(ticket.getTicketNumber());
    	 String ticketExternalIdModified = getCommaSeparateCharOfString(ticket.getTicketNumber811());
    	 if(StringUtils.equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED, assignmentStatus)) {

  

 			LumenRoboCallerBotRequest lumenRoboCallerBotRequest = new LumenRoboCallerBotRequest();

 			LumenUser lumenUser = new LumenUser();

 			LumenOrigin lumenOrigin = new LumenOrigin();

  

 			SupervisorPolygonMapping supervisor = supervisorPolygonMappingRepository.findBySupervisorId(ticket.getSupervisorId());

  

 			lumenUser.setMessage(DispatchControllerConstants.LUMEN_BOT_AFTER_HRS);

 			lumenUser.setPhoneNumber(String.valueOf(getTenDigitRandomNumber(1000000000l, 9999999999l)));

  

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER,ticketExternalIdModified));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TICKET_CONVERSATION_ID,ticket.getConversationId()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TECHNICIAN_ID,ticket.getTechnicianId()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TECHNICIAN_NAME,ticket.getTechnicianFirstName()+" "+ticket.getTechnicianLastName()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_SUPERVISOR_ID,ticket.getSupervisorId()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_SUPERVISOR_NAME,ticket.getSupervisorName()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TECHNICIAN_PHONE_NUMBER,agent.getPhoneNumber()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_SUPERVISOR_PHONE_NUMBER,supervisor.getPhoneNumber()));

 			lumenRoboCallerBotRequest.setUser(lumenUser);

 			lumenRoboCallerBotRequest.setOrigin(lumenOrigin);

  

 			try {

 				executor.execute(()->{

 					BusinessContext.setTenantId(businessId);

 					BusinessTokenContext.setBusinessToken(businessToken);

 					callRoboCallerBot(lumenRoboCallerBotRequest, businessToken,businessId);

 				});

 				executor.execute(()->{

 					BusinessContext.setTenantId(businessId);

 					BusinessTokenContext.setBusinessToken(businessToken);

 					BuzzSendNotificationRequest buzzSendNotificationRequest = dataConvertorUtils.getBuzzSendNotificationRequest(ticket, DispatchControllerConstants.JOB_TITLE_TECHNICIAN, assignmentStatus);

 					callBuzzSendNotification(buzzSendNotificationRequest,businessId,businessToken);

 				});

 				executor.execute(()->{

 					BusinessContext.setTenantId(businessId);

 					BusinessTokenContext.setBusinessToken(businessToken);

 					BuzzSendNotificationRequest buzzSendNotificationRequest = dataConvertorUtils.getBuzzSendNotificationRequest(ticket, DispatchControllerConstants.JOB_TITLE_SUPERVISOR, assignmentStatus);

 					callBuzzSendNotification(buzzSendNotificationRequest,businessId,businessToken);

 				});

 			}catch(Exception e) {

 				log.info("Unable to Send Notification API , due to : {}", e.getMessage());

 			}

  

 		}else {

 			LumenRoboCallerBotRequest lumenRoboCallerBotRequest = new LumenRoboCallerBotRequest();

 			LumenUser lumenUser = new LumenUser();

 			LumenOrigin lumenOrigin = new LumenOrigin();

  

 			SupervisorPolygonMapping supervisor = supervisorPolygonMappingRepository.findBySupervisorId(ticket.getSupervisorId());

  

 			lumenUser.setMessage(DispatchControllerConstants.LUMEN_BOT_AFTER_HRS);

 			lumenUser.setPhoneNumber(String.valueOf(getTenDigitRandomNumber(1000000000l, 9999999999l)));

  

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TICKET_EXTERNAL_ID,ticketExternalIdModified));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TICKET_CONVERSATION_ID,ticket.getConversationId()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TECHNICIAN_ID,""));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TECHNICIAN_NAME,""));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_SUPERVISOR_ID,ticket.getSupervisorId()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_SUPERVISOR_NAME,ticket.getSupervisorName()));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_TECHNICIAN_PHONE_NUMBER,""));

 			lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_BOT_PARAMETER_SUPERVISOR_PHONE_NUMBER,supervisor.getPhoneNumber()));

 			lumenRoboCallerBotRequest.setUser(lumenUser);

 			lumenRoboCallerBotRequest.setOrigin(lumenOrigin);

  

 			try {

 				executor.execute(()->{

 					BusinessContext.setTenantId(businessId);

 					BusinessTokenContext.setBusinessToken(businessToken);

 					callRoboCallerBot(lumenRoboCallerBotRequest, businessToken,businessId);

 				});

 				executor.execute(()->{

 					BusinessContext.setTenantId(businessId);

 					BusinessTokenContext.setBusinessToken(businessToken);

 					BuzzSendNotificationRequest buzzSendNotificationRequest = dataConvertorUtils.getBuzzSendNotificationRequest(ticket, DispatchControllerConstants.JOB_TITLE_SUPERVISOR, assignmentStatus);

 					callBuzzSendNotification(buzzSendNotificationRequest,businessId,businessToken);

 				});

 			}catch(Exception e) {

 				log.info("Unable to Send Notification API , due to : {}", e.getMessage());

 			}

  

 		}

  

 	}
     public String getCommaSeparateCharOfString(String input) {

		 try {

			 return input.chars()

					 .mapToObj(c -> (char) c + ", ")

					 .collect(Collectors.joining());

		 }catch(Exception e) {

			 return input;

		 }

	 }
	
}
