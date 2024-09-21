package com.straviso.ns.dispatchcontrollercore.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.BuzzSendNotificationRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ExternalTicketRequestDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.FCMSendNotificationRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.NSAuditRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechnicianAvailabilityDTO;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.entity.TicketActionTrail;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.entity.TechnicianAvailability;
import com.straviso.ns.dispatchcontrollercore.serviceImpl.ExternalServiceImpl;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DataConvertorUtils {
	
	@Autowired
	DispatchControllerSupportUtils dispatchControllerSupportUtils;
	
	public void convertExternalTicketDetailsToDcTicketModel(ExternalTicketRequestDTO request, Ticket dcTicketModel, boolean isTicketDetailsMissing,Map<String, Object> missingTicketDetails) {
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		
		dcTicketModel.setConversationId(request.getConversationId());
		dcTicketModel.setTicketNumber(request.getTicketNumber());
		dcTicketModel.setTicketNumber811(request.getTicketNumber811());
		dcTicketModel.setMasterTicketExternalId(request.getMasterTicketExternalId());
		if(StringUtils.isEmpty(request.getPolygonId()) ) {
			dcTicketModel.setPolygonId("");
		}else {
			dcTicketModel.setPolygonId(request.getPolygonId());
		}	
		//dcTicketModel.setPolygonId(request.getPolygonId());
		dcTicketModel.setTicketETA(request.getTicketETA());
		dcTicketModel.setCertificationRequired(request.getCertificationRequired());
		
		dcTicketModel.setTicketScore(request.getTicketScore());
		
		
		dcTicketModel.setSupervisorId(request.getSupervisorId());
		dcTicketModel.setSupervisorName(StringUtils.capitalize(request.getSupervisorName()));
		dcTicketModel.setLocation(request.getLocation());
		dcTicketModel.setTicketType(request.getTicketType());
		dcTicketModel.setTicketPriority(request.getTicketPriority());
		try {
			dcTicketModel.setTicketDueDateAndTime(LocalDateTime.parse(request.getTicketDueDateAndTime(), dtf));
			dcTicketModel.setCreatedDateTime(LocalDateTime.parse(request.getCreatedDateTime(), dtf));
			}
			catch(Exception e)
			{
				log.info("Unable to parse datetime"+e.getMessage());	
				}
		if(isTicketDetailsMissing)
		{	
		dcTicketModel.setGlobalStatus(DispatchControllerConstants.STATUS_MISSING_INFO);
		dcTicketModel.setIntialTicketStatus(DispatchControllerConstants.STATUS_MISSING_INFO);
		LocalDateTime localDateTimeNow = LocalDateTime.now();
		TicketActionTrail ticketActionTrail = new TicketActionTrail();
		ticketActionTrail.setAction(DispatchControllerConstants.TICKET_UPDATED);
		ticketActionTrail.setActionBy(DispatchControllerConstants.ENRICHMENT_BOT);
		ticketActionTrail.setActionOn(localDateTimeNow);
		ticketActionTrail.setPreAction(missingTicketDetails.toString());
		ticketActionTrail.setPostAction(DispatchControllerConstants.STATUS_MISSING_INFO);
		dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
		}
		else
		{
			dcTicketModel.setGlobalStatus(request.getTicketStatus());
			dcTicketModel.setIntialTicketStatus(DispatchControllerConstants.WORD_ACTIVE);
		}
		
		dcTicketModel.setActionOnTicket(request.getTicketStatus());
		
		dcTicketModel.setWorkType(request.getWorkType());
		dcTicketModel.setWorkCity(StringUtils.capitalize(request.getWorkCity()));
		dcTicketModel.setWorkState(StringUtils.upperCase(request.getWorkState()));
		dcTicketModel.setWorkCounty(StringUtils.capitalize(request.getWorkCounty()));
		dcTicketModel.setWorkStreet(request.getWorkStreet());
		dcTicketModel.setWorkZip(request.getWorkZip());
		dcTicketModel.setWorkAddress(request.getWorkAddress());
				
		dcTicketModel.setTechnicianEmailId(request.getTechnicianEmailId());
		dcTicketModel.setTechnicianFirstName(StringUtils.capitalize(request.getTechnicianFirstName()));
		dcTicketModel.setTechnicianId(request.getTechnicianId());
		dcTicketModel.setTechnicianLastName(StringUtils.capitalize(request.getTechnicianLastName()));
		dcTicketModel.setSupervisorPolygonId(request.getSupervisorPolygonId());
		
		if(StringUtils.isEmpty(request.getEmergencyFlag()) ) {
				dcTicketModel.setEmergencyFlag(DispatchControllerConstants.NO);
		}else {
			dcTicketModel.setEmergencyFlag(request.getEmergencyFlag());
		}
		
		if(StringUtils.isEmpty(request.getAfterHours()) ) {
			dcTicketModel.setAfterHours(DispatchControllerConstants.NO);
		}else {
			dcTicketModel.setAfterHours(request.getAfterHours());
		}
		
		if(StringUtils.isEmpty(request.getMultiTechnicianTicket()) ){
			dcTicketModel.setMultiTechnicianTicket(DispatchControllerConstants.NO);
		}else {
			dcTicketModel.setMultiTechnicianTicket(request.getMultiTechnicianTicket());
		}
		
		if(StringUtils.isEmpty(request.getIsAssistTicket()) ) {
			dcTicketModel.setIsAssistTicket(DispatchControllerConstants.NO);
		}else {
			dcTicketModel.setIsAssistTicket(request.getIsAssistTicket());
		}
		
		if(StringUtils.isEmpty(request.getIsFirstTicket()) ) {
			dcTicketModel.setIsFirstTicket(DispatchControllerConstants.NO);
		}else {
			dcTicketModel.setIsFirstTicket(request.getIsAssistTicket());
		}
			 
		
	}

	public FCMSendNotificationRequest getFCMSendNotificationRequest(Ticket ticket, String jobTitle) {

	        

	        FCMSendNotificationRequest fcmSendNotificationRequest = new FCMSendNotificationRequest();

	        

	        if(StringUtils.equalsIgnoreCase(jobTitle, DispatchControllerConstants.JOB_TITLE_TECHNICIAN)) {

	            String title = StringUtils.replace(DispatchControllerConstants.FCM_TECHNICIAN_TITLE, DispatchControllerConstants.FCM_PLACEHOLDER_TICKETID, ticket.getTicketNumber());

	            String message = StringUtils.replace(DispatchControllerConstants.FCM_TECHNICIAN_MESSAGE, DispatchControllerConstants.FCM_PLACEHOLDER_TICKETID, ticket.getTicketNumber());

	            

	            fcmSendNotificationRequest.setUserId(ticket.getTechnicianId());

	            fcmSendNotificationRequest.setFcmFlag("1");

	            fcmSendNotificationRequest.setTitle(title);

	            fcmSendNotificationRequest.setMessage(message);

	            

	        }else {

	            String title = StringUtils.replace(DispatchControllerConstants.FCM_SUPERVISOR_TITLE, DispatchControllerConstants.FCM_PLACEHOLDER_TICKETID, ticket.getTicketNumber());

	            String message = StringUtils.replace(DispatchControllerConstants.FCM_SUPERVISOR_MESSAGE, DispatchControllerConstants.FCM_PLACEHOLDER_TICKETID, ticket.getTicketNumber());

	            message = StringUtils.replace(message, DispatchControllerConstants.FCM_PLACEHOLDER_TECHNICIANFULLNAME, ticket.getTechnicianFirstName()+" "+ticket.getTechnicianLastName());

	            

	            fcmSendNotificationRequest.setUserId(ticket.getTechnicianId());

	            fcmSendNotificationRequest.setFcmFlag("1");

	            fcmSendNotificationRequest.setTitle(title);

	            fcmSendNotificationRequest.setMessage(message);

	        }

	        

	        return fcmSendNotificationRequest;

	        

	    }
	
		/*
		 * public BuzzSendNotificationRequest getBuzzSendNotificationRequest(Ticket
		 * ticket, String jobTitle) {
		 * 
		 * 
		 * 
		 * BuzzSendNotificationRequest buzzSendNotificationRequest = new
		 * BuzzSendNotificationRequest();
		 * 
		 * 
		 * 
		 * if(StringUtils.equalsIgnoreCase(jobTitle,
		 * DispatchControllerConstants.JOB_TITLE_TECHNICIAN)) {
		 * 
		 * 
		 * 
		 * Map<String,String> tokens = new HashMap<>();
		 * 
		 * tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TICKETID,
		 * ticket.getTicketNumber());
		 * 
		 * 
		 * 
		 * buzzSendNotificationRequest.setReceiverId(ticket.getTechnicianId()) ;
		 * 
		 * buzzSendNotificationRequest.setProcessName(DispatchControllerConstants.
		 * BUZZ_PN_TECHNICIAN_PROCESS_NAME);
		 * 
		 * buzzSendNotificationRequest.setProcessCode(DispatchControllerConstants.
		 * BUZZ_PN_TECHNICIAN_PROCESS_CODE);
		 * 
		 * buzzSendNotificationRequest.setTemplateName(DispatchControllerConstants.
		 * BUZZ_PN_TECHNICIAN_TEMPLATE_NAME);
		 * 
		 * buzzSendNotificationRequest.setTokens(tokens);
		 * 
		 * 
		 * 
		 * }else {
		 * 
		 * Map<String,String> tokens = new HashMap<>();
		 * 
		 * tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TICKETID,
		 * ticket.getTicketNumber());
		 * 
		 * tokens.put(DispatchControllerConstants.
		 * BUZZ_PN_PLACEHOLDER_TECHNICIANFULLNAME,
		 * ticket.getTechnicianFirstName()+" "+ticket.getTechnicianLastName());
		 * 
		 * 
		 * 
		 * buzzSendNotificationRequest.setReceiverId(ticket.getTechnicianId()) ;
		 * 
		 * buzzSendNotificationRequest.setProcessName(DispatchControllerConstants.
		 * BUZZ_PN_SUPERVISOR_PROCESS_NAME);
		 * 
		 * buzzSendNotificationRequest.setProcessCode(DispatchControllerConstants.
		 * BUZZ_PN_SUPERVISOR_PROCESS_CODE);
		 * 
		 * buzzSendNotificationRequest.setTemplateName(DispatchControllerConstants.
		 * BUZZ_PN_SUPERVISOR_TEMPLATE_NAME);
		 * 
		 * buzzSendNotificationRequest.setTokens(tokens);
		 * 
		 * }
		 * 
		 * 
		 * 
		 * return buzzSendNotificationRequest;
		 * 
		 * 
		 * 
		 * }
		 */
	
public BuzzSendNotificationRequest getBuzzSendNotificationRequest(Ticket ticket, String jobTitle, String assignmentStatus) {

		

		BuzzSendNotificationRequest buzzSendNotificationRequest = new BuzzSendNotificationRequest();

		

		if(StringUtils.equalsIgnoreCase(jobTitle, DispatchControllerConstants.JOB_TITLE_TECHNICIAN)) {

			

			Map<String,String> tokens = new HashMap<>();

			tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TICKETID, ticket.getTicketNumber811());

			

			buzzSendNotificationRequest.setReceiverId(ticket.getTechnicianId()) ;

			buzzSendNotificationRequest.setProcessName(DispatchControllerConstants.BUZZ_PN_TECHNICIAN_PROCESS_NAME);

			buzzSendNotificationRequest.setProcessCode(DispatchControllerConstants.BUZZ_PN_TECHNICIAN_PROCESS_CODE);

			buzzSendNotificationRequest.setTemplateName(DispatchControllerConstants.BUZZ_PN_TECHNICIAN_TEMPLATE_NAME);

			buzzSendNotificationRequest.setTokens(tokens);

			

		}else if(StringUtils.equalsIgnoreCase(jobTitle, DispatchControllerConstants.JOB_TITLE_SUPERVISOR)) {

			

			if(StringUtils.equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED, assignmentStatus)) {

 

				Map<String,String> tokens = new HashMap<>();

				tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TICKETID, ticket.getTicketNumber811());

				tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TECHNICIANFULLNAME, ticket.getTechnicianFirstName()+" "+ticket.getTechnicianLastName());

				

				buzzSendNotificationRequest.setReceiverId(ticket.getSupervisorId()) ;

				buzzSendNotificationRequest.setProcessName(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_PROCESS_NAME);

				buzzSendNotificationRequest.setProcessCode(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_PROCESS_CODE);

				buzzSendNotificationRequest.setTemplateName(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_TEMPLATE_NAME);

				buzzSendNotificationRequest.setTokens(tokens);

			}else {

 

				Map<String,String> tokens = new HashMap<>();

				tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TICKETID, ticket.getTicketNumber811());

				

				buzzSendNotificationRequest.setReceiverId(ticket.getSupervisorId()) ;

				buzzSendNotificationRequest.setProcessName(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_PROCESS_NAME_UNASSIGNED);

				buzzSendNotificationRequest.setProcessCode(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_PROCESS_CODE_UNASSIGNED);

				buzzSendNotificationRequest.setTemplateName(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_TEMPLATE_NAME_UNASSIGNED);

				buzzSendNotificationRequest.setTokens(tokens);

			}

			

			

			

		}else {

			Map<String,String> tokens = new HashMap<>();

			tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TICKETID, ticket.getTicketNumber811());

			tokens.put(DispatchControllerConstants.BUZZ_PN_PLACEHOLDER_TECHNICIANFULLNAME, ticket.getTechnicianFirstName()+" "+ticket.getTechnicianLastName());

			

			buzzSendNotificationRequest.setReceiverId(ticket.getSupervisorId()) ;

			buzzSendNotificationRequest.setProcessName(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_PROCESS_NAME);

			buzzSendNotificationRequest.setProcessCode(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_PROCESS_CODE);

			buzzSendNotificationRequest.setTemplateName(DispatchControllerConstants.BUZZ_PN_SUPERVISOR_TEMPLATE_NAME);

			buzzSendNotificationRequest.setTokens(tokens);

		}

		

		return buzzSendNotificationRequest;

		

	}
	public void convertTechModel(TechnicianAvailabilityDTO request, TechnicianAvailability techModel) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DATE_FORMAT);
		DateTimeFormatter dtf_time = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		
		techModel.setTechnicianId(request.getTechnicianId());
		techModel.setAvailabilityStatus(request.getAvailabilityStatus());
		techModel.setAvailableTime(request.getAvailableTime());
		techModel.setCalenderDate(LocalDateTime.parse(request.getCalenderDate()+DispatchControllerConstants.START_DATETIME_APPEND_PTO, dtf_time));
		//techModel.setCalenderDate(LocalDateTime.parse(request.getCalenderDate()+" 12:00:02", dtf_time));
		
		techModel.setIsOnCall(request.getIsOnCall());
		techModel.setJobTitle(request.getJobTitle());
		String onCallStartDateTime=request.getCalenderDate()+ " "+request.getOnCallStartDateTime()+ ":00" ;
		String onCallEndDateTime=request.getCalenderDate()+ " "+request.getOnCallEndDateTime()+ ":00" ;
		
		LocalDateTime startDT=LocalDateTime.parse(onCallStartDateTime, dtf_time);
		LocalDateTime endDT=LocalDateTime.parse(onCallEndDateTime, dtf_time);
		 System.out.println("startDT" +startDT);
		 System.out.println("endDT" +endDT);
		if(startDT.isAfter(endDT))
		{
			 System.out.println("IF loop");
			 endDT=endDT.plusDays(1);
			
			 System.out.println("endDT" +endDT);
		}
		
		techModel.setOnCallEndDateTime(endDT);
		techModel.setOnCallStartDateTime(startDT);
		techModel.setProjectTime(request.getProjectTime());
		techModel.setTechnicianName(request.getTechnicianName());
		techModel.setSupervisorId(request.getSupervisorId());
		techModel.setTimestamp(LocalDateTime.now());
		 
		  System.out.println("Is weekend : " + isWeekend(techModel.getCalenderDate().toLocalDate()));
		  if(isWeekend(techModel.getCalenderDate().toLocalDate()))
		  {
			  techModel.setIsWeekend(DispatchControllerConstants.YES);
		  }
		  else
		  {
			  techModel.setIsWeekend(DispatchControllerConstants.NO); 
		  }
		}
	 public static boolean isWeekend(final LocalDate ld)
	    {
	        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
	        return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;
	    }
	 
	 public boolean isWeekendZoned(final ZonedDateTime zonedDateTime)

	    {

	      DayOfWeek day = DayOfWeek.of(zonedDateTime.get(ChronoField.DAY_OF_WEEK));

	      return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;

	    }

	    

	    public boolean pastDueTicketCheckZoned(final ZonedDateTime zonedDateTime)

	    {

	      DayOfWeek day = DayOfWeek.of(zonedDateTime.get(ChronoField.DAY_OF_WEEK));

	      return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY ;

	    }

	    

	    public DayOfWeek getZonedWeekDay(final ZonedDateTime zonedDateTime)

	    {

	      DayOfWeek day = DayOfWeek.of(zonedDateTime.get(ChronoField.DAY_OF_WEEK));

	      return day;

	    }
	 
	    public void callNsAuditSave(LumenCollectionUpdateDTO lumenCollectionUpdateDTO, String businessId, String businessToken) {
	    	

	    	
	    	try {

	    		NSAuditRequest nsAuditRequest = new NSAuditRequest();

	            nsAuditRequest.setConversationId(lumenCollectionUpdateDTO.getConversationId());

	            nsAuditRequest.setTopic("Ticket");

	            nsAuditRequest.setTopicIdentifier(lumenCollectionUpdateDTO.getTicketNumber());

	            nsAuditRequest.setTransactionId("");

	            nsAuditRequest.setProcess(DispatchControllerConstants.STAGE_ASSIGNMENT);

	            nsAuditRequest.setAction(lumenCollectionUpdateDTO.getTicketActionTrails().getAction());

	            nsAuditRequest.setActionBySystem(DispatchControllerConstants.SYSTEM_DC);

	            nsAuditRequest.setPreAction(lumenCollectionUpdateDTO.getTicketActionTrails().getPreAction());

	            nsAuditRequest.setPostAction(lumenCollectionUpdateDTO.getTicketActionTrails().getPostAction());

	            nsAuditRequest.setActionByUser(lumenCollectionUpdateDTO.getTicketActionTrails().getActionBy());

	            nsAuditRequest.setRemarks(lumenCollectionUpdateDTO.getRemarks());

	            nsAuditRequest.setError("");

	            

	            dispatchControllerSupportUtils.callNSAuditSave(nsAuditRequest, businessId, businessToken);

	            

	        }catch(Exception e) {

	            log.info("Unable to call NsAudit Save API due to {}",e.getMessage());

	        }

	        

	    }
     

}
