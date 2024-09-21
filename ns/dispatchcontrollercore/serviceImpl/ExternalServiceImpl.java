package com.straviso.ns.dispatchcontrollercore.serviceImpl;


import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.AgentAndSupervisorRequestDTO;
import com.straviso.ns.dispatchcontrollercore.dto.AgentTicketRiskScore;
import com.straviso.ns.dispatchcontrollercore.dto.CallTechnicianDto;
import com.straviso.ns.dispatchcontrollercore.dto.GroupByTransferList;
import com.straviso.ns.dispatchcontrollercore.dto.LumenAdditionalInfo;
import com.straviso.ns.dispatchcontrollercore.dto.LumenCollectionUpdateDTO;
import com.straviso.ns.dispatchcontrollercore.dto.LumenOrigin;
import com.straviso.ns.dispatchcontrollercore.dto.LumenUser;
import com.straviso.ns.dispatchcontrollercore.dto.MyTeamWorkloadSupervisorDetails;
import com.straviso.ns.dispatchcontrollercore.dto.MyTeamWorkloadTechnicianDetails;
import com.straviso.ns.dispatchcontrollercore.dto.RouteSolverPath;
import com.straviso.ns.dispatchcontrollercore.dto.TicketNumbersDto;
import com.straviso.ns.dispatchcontrollercore.dto.TransferTicketDTO;
import com.straviso.ns.dispatchcontrollercore.dto.UpdateTicketNumbersDto;
import com.straviso.ns.dispatchcontrollercore.dto.UpdatedTicketScoreData;
import com.straviso.ns.dispatchcontrollercore.dto.firstTicketNumberDto;
import com.straviso.ns.dispatchcontrollercore.dto.request.EmergencyTicketDetailsDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.EmergencyTicketRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.ExternalTicketRequestDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.LumenRoboCallerBotRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.MyTeamWorkloadRequest;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechIdDto;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechnicianAvailabilityDTO;
import com.straviso.ns.dispatchcontrollercore.dto.request.TechnicianAvailabilityRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.AgentASTicketResponce;
import com.straviso.ns.dispatchcontrollercore.dto.response.AgentTS;
import com.straviso.ns.dispatchcontrollercore.dto.response.AgentTSMain;
import com.straviso.ns.dispatchcontrollercore.dto.response.ApiResponseDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.ExternalAppResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.GroupByActionResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.MyTeamWorkloadResponse;
import com.straviso.ns.dispatchcontrollercore.dto.response.ResponseDTO;
import com.straviso.ns.dispatchcontrollercore.dto.response.TechnicianIdDto;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketSequence;
import com.straviso.ns.dispatchcontrollercore.dto.response.TicketSequenceMain;
import com.straviso.ns.dispatchcontrollercore.entity.AfterHrsAgentModel;
import com.straviso.ns.dispatchcontrollercore.entity.Agent;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAssignmentSolutionModel;
import com.straviso.ns.dispatchcontrollercore.entity.AgentAvailabilityLookUp;
import com.straviso.ns.dispatchcontrollercore.entity.Location;
import com.straviso.ns.dispatchcontrollercore.entity.SupervisorPolygonMapping;
import com.straviso.ns.dispatchcontrollercore.entity.SystemConfigDC;
import com.straviso.ns.dispatchcontrollercore.entity.Ticket;
import com.straviso.ns.dispatchcontrollercore.entity.TicketActionTrail;
import com.straviso.ns.dispatchcontrollercore.entity.TechnicianAvailability;
import com.straviso.ns.dispatchcontrollercore.entity.TechnicianWorkHour;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.repository.AfterHrsAgentRepository;
import com.straviso.ns.dispatchcontrollercore.repository.AgentAvailabilityLookUpRepo;
import com.straviso.ns.dispatchcontrollercore.repository.AgentRepository;
import com.straviso.ns.dispatchcontrollercore.repository.AgentTicketRiskScoreRepo;
import com.straviso.ns.dispatchcontrollercore.repository.SupervisorRepository;
import com.straviso.ns.dispatchcontrollercore.repository.SystemConfigDCRepository;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianAssignmentSolutionRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianDataRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TechnicianWorkHourRepository;
import com.straviso.ns.dispatchcontrollercore.repository.TicketDataRepo;
import com.straviso.ns.dispatchcontrollercore.repository.TicketRepository;
import com.straviso.ns.dispatchcontrollercore.repository.technicianAvailabilityRepo;
import com.straviso.ns.dispatchcontrollercore.repositoryImpl.AssignBackToQueueRepoImpl;
import com.straviso.ns.dispatchcontrollercore.repositoryImpl.TicketRepositoryImpl;
import com.straviso.ns.dispatchcontrollercore.service.CockpitService;
import com.straviso.ns.dispatchcontrollercore.service.ExternalService;
import com.straviso.ns.dispatchcontrollercore.service.GroupByActionService;
import com.straviso.ns.dispatchcontrollercore.utils.CommonUtils;
import com.straviso.ns.dispatchcontrollercore.utils.DataConvertorUtils;
import com.straviso.ns.dispatchcontrollercore.utils.DispatchControllerSupportUtils;

import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class ExternalServiceImpl implements ExternalService{

	@Autowired
	private DataConvertorUtils dataConvertorUtils;
	
	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	private DispatchControllerSupportUtils dispatchControllerSupportUtils;

	@Autowired
	private TicketDataRepo ticketDataRepo;

	@Autowired
	private TechnicianDataRepo technicianDataRepo;
	
	@Autowired
	TechnicianAssignmentSolutionRepo assignmentRepository;
	
	@Autowired
	AssignBackToQueueRepoImpl assignBackToQueueRepoImpl;
	
	@Autowired
	private SupervisorRepository supervisorDataRepo;
	
//	@Autowired
//	private AfterHrsAgentRepository afterHrsAgentRepository;
	
	@Autowired
	private TicketRepositoryImpl ticketRepositoryImpl;
	
	@Autowired
	GroupByActionService groupByActionService ;
	
	@Autowired
	technicianAvailabilityRepo techAvailabilityRepo ;
	
	@Autowired(required = true)
	@Qualifier(value = "taskExecutor")
	private ThreadPoolTaskExecutor executor;
	
	@Autowired
	private CommonUtils commonUtils;
	
	@Autowired
	private AgentRepository agentRepository;
	
	 @Autowired
	 private AgentTicketRiskScoreRepo agentTicketRiskScoreRepo;
	
	 @Autowired
	 private SystemConfigDCRepository systemConfigDCRepository;
	@Value("${service.constants.dcsa.solver.emergencyTicketAssignment_url}")
    String emergencyTicketAssignment_url;
	
	@Value("${service11.call.api.url}")
	private String service11callAPIUrl;
	
	@Autowired
	private AgentAvailabilityLookUpRepo agentAvailabilityLookUpRepo;
	
	@Autowired
	CockpitServiceImpl cockpitServiceImpl;
	
	@Autowired
	CockpitService getCockpitService;
	
	@Autowired
    private TechnicianWorkHourRepository technicianWorkHourRepository;

	@Autowired
	TicketRepository ticketRepository;
	
	@Autowired
	SFTPFileService sftpFileService;
	
	@Override
	public ResponseEntity<ResponseDTO> sendTicketDetailsToDC(ExternalTicketRequestDTO request) {
		
		
		String logKey = DispatchControllerConstants.SEND_TICKET_DETAILS_TO_DC;
		String businessId = BusinessContext.getTenantId();
		String token = BusinessTokenContext.getBusinessToken();
		
		

		try {
			Map<String, Object> missingTicketDetails = MissingInfoTicket(request);

			boolean isTicketDetailsMissing =false;

			if (!CollectionUtils.isEmpty(missingTicketDetails))
			{
				log.info("Missingticket details  ");
				isTicketDetailsMissing =true;

			}

			Ticket dcTicketModel = new Ticket();
			
			boolean isTicketPresent = ticketDataRepo.existsByTicketNumberAndConversationId(request.getTicketNumber(),request.getConversationId());
			log.info("isTicketPresent{}",isTicketPresent);
			//Update ticket details if ticket present
			if(isTicketPresent) {
				
				
				if(isTicketDetailsMissing)
				{
				
					log.info("return Missingticket details  ");
					missingInfoNsAudit(request,missingTicketDetails);
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.STATUS_MISSING_INFO,missingTicketDetails, HttpStatus.OK);
				}

				dcTicketModel = ticketDataRepo.findByTicketNumberAndConversationId(request.getTicketNumber(),request.getConversationId());
			
				if(StringUtils.equalsIgnoreCase(dcTicketModel.getGlobalStatus(),DispatchControllerConstants.STATUS_CANCELLED) || StringUtils.equalsIgnoreCase(dcTicketModel.getGlobalStatus(),DispatchControllerConstants.STATUS_COMPLETE) )
				{
					log.info("ticket already completed or cancelled {}",dcTicketModel.getTicketNumber());
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.STATUS_TICKET_ALREADY_COMPLTED_CANCELLED,new HashMap<>(), HttpStatus.OK);
						
				}
				if(request.getTicketStatus().equals(DispatchControllerConstants.STATUS_CANCELLED))
				{	
					//Remove ticket from technician and assignment solution.Update ticket in ticketcollection 
					updateTechnicianAndAssignmentSolutionIfCancelled(dcTicketModel.getTicketNumber(),dcTicketModel.getTechnicianId());	
				}
				else if(request.getTicketStatus().equals(DispatchControllerConstants.STATUS_PASTDUE))
				{	
					
					 	if(dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED))
						{
					 		updateTicketDetailsToDcTicketDetails(request,dcTicketModel,isTicketDetailsMissing);	
					 		//Not updating agent bucket.DC Assignment will handle this. 
					 		/*updateAssignedPastDueTicket(dcTicketModel);*/
						}
						else if(dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED))
						{
							updateTicketDetailsToDcTicketDetails(request,dcTicketModel,isTicketDetailsMissing);		
						}
					
				}
				else if(StringUtils.equalsIgnoreCase(request.getAssignmentType(),DispatchControllerConstants.STATUS_RESCHEDULE))
				{
					log.info("TicketStatus received from Enrichment : {} TicketNumber : {}",request.getTicketStatus(),request.getTicketNumber());
					
			
					if(request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED) && 
							dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED))
					{
						log.info("Enrichment TicketStatus {} ,DC TicketStatus {} . TicketNumber : {}",request.getTicketStatus(),dcTicketModel.getGlobalStatus(),request.getTicketNumber());
						String action = DispatchControllerConstants.STATUS_RESCHEDULE;
						String ticketStatus = DispatchControllerConstants.STATUS_ASSIGNED;
						updateTicket(request,dcTicketModel,isTicketDetailsMissing,action,ticketStatus);
						UpdateTechnicianAndSolution(dcTicketModel);
						log.info("Updated Ticket,Technician,Solution. TicketNumber : {}",request.getTicketNumber());
						
					}
					else if(request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED) && 
							dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED))
					{
						log.info("Enrichment TicketStatus {} ,DC TicketStatus {} . TicketNumber : {}",request.getTicketStatus(),dcTicketModel.getGlobalStatus(),request.getTicketNumber());
						String action = DispatchControllerConstants.STATUS_UNASSIGNED;
						String ticketStatus = DispatchControllerConstants.STATUS_UNASSIGNED;
						updateTicket(request,dcTicketModel,isTicketDetailsMissing,action,ticketStatus);
						log.info("Updated Ticket. TicketNumber : {}",request.getTicketNumber());
					}
					else if(request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) && 
							dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED))
					{
						log.info("Enrichment TicketStatus {} ,DC TicketStatus {} . TicketNumber : {}",request.getTicketStatus(),dcTicketModel.getGlobalStatus(),request.getTicketNumber());
						String action = DispatchControllerConstants.STATUS_RESCHEDULE;
						String ticketStatus = DispatchControllerConstants.STATUS_ASSIGNED;
						updateTicket(request,dcTicketModel,isTicketDetailsMissing,action,ticketStatus);
						UpdateTechnicianAndSolution(dcTicketModel);
						log.info("Updated Ticket,Technician,Solution. TicketNumber : {}",request.getTicketNumber());
					}
					else if(request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) && 
							dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED))
					{
					log.info("Enrichment TicketStatus {} ,DC TicketStatus {} . TicketNumber : {}",request.getTicketStatus(),dcTicketModel.getGlobalStatus(),request.getTicketNumber());
					String action = DispatchControllerConstants.STATUS_UNASSIGNED;
					String ticketStatus = DispatchControllerConstants.STATUS_UNASSIGNED;
					updateTicket(request,dcTicketModel,isTicketDetailsMissing,action,ticketStatus);
					log.info("Updated Ticket. TicketNumber : {}",request.getTicketNumber());
					}
					else
					{
						
						if(request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ENROUTE) ||
								request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ONSITE) ||
								request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_JOBCLOSEOUT) ||
								request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_PAUSE))
						{
							
							log.info("Enrichment TicketStatus {} ,DC TicketStatus {} . TicketNumber : {}",request.getTicketStatus(),dcTicketModel.getGlobalStatus(),request.getTicketNumber());
							String action = DispatchControllerConstants.STATUS_RESCHEDULE;
							String ticketStatus = DispatchControllerConstants.STATUS_ASSIGNED;
							updateTicket(request,dcTicketModel,isTicketDetailsMissing,action,ticketStatus);
							UpdateTechnicianAndSolution(dcTicketModel);
							log.info("Updated Ticket,Technician,Solution. TicketNumber : {}",request.getTicketNumber());
						}
						else
						{
							log.info("Enrichment TicketStatus {} ,DC TicketStatus {} . TicketNumber : {}",request.getTicketStatus(),dcTicketModel.getGlobalStatus(),request.getTicketNumber());
							String action = DispatchControllerConstants.STATUS_UNASSIGNED;
							String ticketStatus = DispatchControllerConstants.STATUS_UNASSIGNED;
							updateTicket(request,dcTicketModel,isTicketDetailsMissing,action,ticketStatus);
							log.info("Updated Ticket. TicketNumber : {}",request.getTicketNumber());
						}
					
					}

					
					
				}
				else if(dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED) && request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED))
				{
					//change ticke except tech id and global status
					updateTicketDetailsWithoutTechDetails(request,dcTicketModel,isTicketDetailsMissing);
					BusinessContext.setTenantId(businessId);
					BusinessTokenContext.setBusinessToken(token);
					TicketNumbersDto ticketNumbersDto = new TicketNumbersDto();
					ticketNumbersDto.setTechnicianId(dcTicketModel.getTechnicianId());
					ticketNumbersDto.setTicketNumber(dcTicketModel.getTicketNumber());
					ticketNumbersDto.setActionBy(DispatchControllerConstants.DEFAULT_TECHNICIAN_ID+":"+DispatchControllerConstants.ENRICHMENT_BOT);
					assignBackToQueueRepoImpl.updateByTicketNumber(ticketNumbersDto);
				}
				else if(dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) && request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) && request.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES))
				{
					//Update ticket
					updateTicketDetailsToDcTicketDetails(request,dcTicketModel,isTicketDetailsMissing);
					log.info("Emergency ticket saved");
					//Call emergency api
					log.info("Emergency API called");
					BusinessContext.setTenantId(businessId);
					BusinessTokenContext.setBusinessToken(token);
					 if(dcTicketModel.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES) && dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) )
						{		
						 log.info("Emergency API call start");
							EmergencyTicketDetailsDTO dto=new EmergencyTicketDetailsDTO();
							dto.setConversationId(request.getConversationId());
							dto.setTicketNumber(request.getTicketNumber());

							EmergencyTicketRequest emergencyTicketRequest = new EmergencyTicketRequest();
							emergencyTicketRequest.getEmergencyTicketList().add(dto);

							executor.execute(()->{
								BusinessContext.setTenantId(businessId);
								BusinessTokenContext.setBusinessToken(token);
								callEmergencyTicketAssignmentAPI(emergencyTicketRequest,token);
							});

						}
					 log.info("Emergency API call done");
				}
				else if(!StringUtils.equalsIgnoreCase(dcTicketModel.getTechnicianId(), request.getTechnicianId()))
				{
					if(dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED) && request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED))
					{
						//change ticke except tech id and global status
						updateTicketDetailsWithoutTechDetails(request,dcTicketModel,isTicketDetailsMissing);
						BusinessContext.setTenantId(businessId);
						BusinessTokenContext.setBusinessToken(token);
						transferAPIcall(dcTicketModel.getTechnicianId(),request.getTechnicianId(),dcTicketModel.getTicketNumber());
					}
					else if(dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) && request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED))
					{
						//change ticke except tech id and global status
						updateTicketDetailsWithoutTechDetails(request,dcTicketModel,isTicketDetailsMissing);
						BusinessContext.setTenantId(businessId);
						BusinessTokenContext.setBusinessToken(token);
						transferAPIcall(dcTicketModel.getTechnicianId(),request.getTechnicianId(),dcTicketModel.getTicketNumber());	
					}
				}
				else
				{
				
				String previousTicketStatus = dcTicketModel.getGlobalStatus(); 
				updateTicketDetailsToDcTicketDetails(request,dcTicketModel,isTicketDetailsMissing);
				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(token);

				//Update ticket in technician and technicianAssignment list
				updateTechnicianAndAssignmentSolution(dcTicketModel,previousTicketStatus);
				}
				
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
				
			}
			else
			{	
				if(missingTicketDetails.containsKey(DispatchControllerConstants.SUPERVISORID))
				{
				missingInfoNsAudit(request,missingTicketDetails);
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.SUPERVISOR_NOT_FOUND,missingTicketDetails, HttpStatus.OK);
				}
				//Insert ticket details if ticket not present
				dataConvertorUtils.convertExternalTicketDetailsToDcTicketModel(request,dcTicketModel,isTicketDetailsMissing,missingTicketDetails);
				String remarks ="";
				Ticket dcThreadTicket=dcTicketModel;
				
				
				if(StringUtils.equalsIgnoreCase(request.getAssignmentType(),DispatchControllerConstants.STICKY_ROUTING))
				{ 	
					if(request.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES) )
					{	
						executor.execute(()->{
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(token);
							stickyRoutingEmergencyAssignment(logKey,businessId,token,dcThreadTicket,request);
						});
						
						if(isTicketDetailsMissing)
						{
						
							log.info("return Missingticket details in StickyRouting {} ",request.getTicketNumber());
							missingInfoNsAudit(request,missingTicketDetails);
							return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.STATUS_MISSING_INFO,missingTicketDetails, HttpStatus.OK);
						}
						 
						log.info("StickyRouting for emergency ticket : {} ",request.getTicketNumber());
						return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
							
					}
					else
					{
						executor.execute(()->{
							
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(token);
							stickyRoutingNormalAssignment(logKey,businessId,token,dcThreadTicket,request);
						});
						
						if(isTicketDetailsMissing)
						{
						
							log.info("return Missingticket details in StickyRouting {} ",request.getTicketNumber());
							missingInfoNsAudit(request,missingTicketDetails);
							return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.STATUS_MISSING_INFO,missingTicketDetails, HttpStatus.OK);
						}
						
					
					log.info("StickyRouting for normal ticket : {} ",request.getTicketNumber());
					
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
							
						// boolean supervisorPresent = supervisorDataRepo.existsBySupervisorId(requestAgent.getSupervisorId());
					}
					
					
				}
	
	
				//Rule based assignment.Need to adapt actual requirement.
				if(StringUtils.equalsIgnoreCase(dcTicketModel.getGlobalStatus(),DispatchControllerConstants.STATUS_ASSIGNED))
				{
					TicketActionTrail ticketActionTrail = new TicketActionTrail();
					ticketActionTrail.setAction(DispatchControllerConstants.ACTION_ASSIGNMENT);
					ticketActionTrail.setActionBy(DispatchControllerConstants.KEYWORD_SYSTEM_INITIAL_ASSIGNMENT);
					ticketActionTrail.setActionOn(LocalDateTime.now());
					ticketActionTrail.setPreAction(DispatchControllerConstants.STATUS_UNASSIGNED);
					ticketActionTrail.setPostAction(DispatchControllerConstants.STATUS_ASSIGNED + " To " + dcTicketModel.getTechnicianId() + " : " + dcTicketModel.getTechnicianFirstName() + " " + dcTicketModel.getTechnicianLastName());
					dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
					dcTicketModel.setAssignmentDateTime(LocalDateTime.now());
					saveTechIntialAssigemnt(dcTicketModel);
				}
				ticketDataRepo.save(dcTicketModel);
				
				//Save to NSAUDIT
				String preAction=DispatchControllerConstants.REQUEST_RECEIVED_FROM_ENRICHMENT_BOT;
				String postAction=DispatchControllerConstants.TICKET_STORED;
				saveNsAudit(request,preAction,postAction);
				
				if(isTicketDetailsMissing)
				{
					missingInfoNsAudit(request,missingTicketDetails);
					log.info("return Missingticket details  ");
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.STATUS_MISSING_INFO,missingTicketDetails, HttpStatus.OK);
				}
				else
				{
					/*if(dcTicketModel.getTicketPriority()==2 && StringUtils.equalsIgnoreCase(dcTicketModel.getAfterHours(),DispatchControllerConstants.YES) && dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED)) {
						Ticket afterHrsTicket = dcTicketModel;
						executor.execute(()->{
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(token);
							assignAfterHrsTicket(afterHrsTicket,token,businessId);
						});
					}*/
					if(dcTicketModel.getEmergencyFlag().equalsIgnoreCase(DispatchControllerConstants.YES) && dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) )
					{		
						EmergencyTicketDetailsDTO dto=new EmergencyTicketDetailsDTO();
						dto.setConversationId(request.getConversationId());
						dto.setTicketNumber(request.getTicketNumber());

						EmergencyTicketRequest emergencyTicketRequest = new EmergencyTicketRequest();
						emergencyTicketRequest.getEmergencyTicketList().add(dto);

						executor.execute(()->{
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(token);
							callEmergencyTicketAssignmentAPI(emergencyTicketRequest,token);
						});

					}
					log.info("return submited ticket details  ");
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
				}
				
			}

			

		}catch(Exception e) {
			log.info("{} Unable to Save External Ticket Details, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR,e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
	
	private void updateTicket(ExternalTicketRequestDTO request, Ticket dcTicketModel,
			boolean isTicketDetailsMissing,String action,String ticketStatus) {
			
			 DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
			 
			 Map<String,Object> pre=new HashMap<>();
			 Map<String,Object> post=new HashMap<>();
		
			 
			 if(!StringUtils.isEmpty(request.getTicketNumber811()) && !StringUtils.equalsIgnoreCase(request.getTicketNumber811(),dcTicketModel.getTicketNumber811())) {
					pre.put(DispatchControllerConstants.TICKETNUMBER_811, dcTicketModel.getTicketNumber811());
					dcTicketModel.setTicketNumber811(request.getPolygonId());
					post.put(DispatchControllerConstants.TICKETNUMBER_811, request.getTicketNumber811());
			 }
			 
			 if(!StringUtils.equalsIgnoreCase(request.getPolygonId(),dcTicketModel.getPolygonId()) ) {
				pre.put(DispatchControllerConstants.POLYGON_ID, dcTicketModel.getPolygonId());
				dcTicketModel.setPolygonId(request.getPolygonId());
				post.put(DispatchControllerConstants.POLYGON_ID, request.getPolygonId());
			 }
			 
			 
			 if(request.getTicketETA() != dcTicketModel.getTicketETA()) {
			 pre.put(DispatchControllerConstants.TICKETETA, dcTicketModel.getTicketETA());
			 dcTicketModel.setTicketETA(request.getTicketETA());
			 post.put(DispatchControllerConstants.TICKETETA, request.getTicketETA());
			  }
			 
			 if(!request.getCertificationRequired().equals(dcTicketModel.getCertificationRequired())) {
				    pre.put(DispatchControllerConstants.CERTIFICATION_REQUIRED, dcTicketModel.getCertificationRequired());
					dcTicketModel.setCertificationRequired(request.getCertificationRequired());
					post.put(DispatchControllerConstants.CERTIFICATION_REQUIRED, request.getCertificationRequired());
					
			          }
			 
			 if(request.getTicketScore() != dcTicketModel.getTicketScore() ) {
					pre.put(DispatchControllerConstants.TICKETSCORE, dcTicketModel.getTicketScore());
					dcTicketModel.setTicketScore(request.getTicketScore());
					post.put(DispatchControllerConstants.TICKETSCORE, request.getTicketScore());
				 }
				try {  
					 
						if(!StringUtils.equalsIgnoreCase(request.getTicketDueDateAndTime(),dcTicketModel.getTicketDueDateAndTime().format(DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT)) )) {
							pre.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, dcTicketModel.getTicketDueDateAndTime());
							dcTicketModel.setTicketDueDateAndTime(LocalDateTime.parse(request.getTicketDueDateAndTime(), dtf));
							post.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, request.getTicketDueDateAndTime());
						}
						
						if(!StringUtils.equalsIgnoreCase(request.getCreatedDateTime(),dcTicketModel.getCreatedDateTime().format(DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT)) )) {
							pre.put(DispatchControllerConstants.CREATEDDATEANDTIME, dcTicketModel.getCreatedDateTime());
							dcTicketModel.setCreatedDateTime(LocalDateTime.parse(request.getCreatedDateTime(), dtf));
							post.put(DispatchControllerConstants.CREATEDDATEANDTIME, request.getCreatedDateTime());
						}
						
				}
				catch(Exception e)
				{
					 log.info("Unable to parse datetime"+e.getMessage());	
				}
				
			 if(!StringUtils.equalsIgnoreCase(request.getSupervisorId(),dcTicketModel.getSupervisorId())) {
					pre.put(DispatchControllerConstants.SUPERVISORID, dcTicketModel.getSupervisorId());
					dcTicketModel.setSupervisorId(request.getSupervisorId());
					post.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
				 }
			 
			 if(!StringUtils.equalsIgnoreCase(request.getSupervisorName(),dcTicketModel.getSupervisorName())) {
					pre.put(DispatchControllerConstants.SUPERVISORNAME, dcTicketModel.getSupervisorName());
					dcTicketModel.setSupervisorName(StringUtils.capitalize(request.getSupervisorName()));
					post.put(DispatchControllerConstants.SUPERVISOR_NAME, request.getSupervisorName());
				 }

			 
			 if(!Objects.equals(request.getLocation().getLatitude(),dcTicketModel.getLocation().getLatitude()) || !Objects.equals(request.getLocation().getLongitude(),dcTicketModel.getLocation().getLongitude()) ) {
				 pre.put(DispatchControllerConstants.LOCATION, dcTicketModel.getLocation().toString());
				 dcTicketModel.setLocation(request.getLocation());
				 post.put(DispatchControllerConstants.LOCATION, request.getLocation().toString());
			     }
			 
			 if(!StringUtils.equalsIgnoreCase(request.getTicketType(),dcTicketModel.getTicketType())) {
					pre.put(DispatchControllerConstants.TICKETTYPE, dcTicketModel.getTicketType());
					dcTicketModel.setTicketType(request.getTicketType());
					post.put(DispatchControllerConstants.TICKETTYPE, request.getTicketType());
				 }
			 
			 
			 if(!StringUtils.equalsIgnoreCase(request.getWorkType(),dcTicketModel.getWorkType())) {
				 	pre.put(DispatchControllerConstants.WORKTYPE, dcTicketModel.getWorkType());
					dcTicketModel.setWorkType(request.getWorkType());
					post.put(DispatchControllerConstants.WORKTYPE, request.getWorkType());
			 }
			 
			 if(!StringUtils.equalsIgnoreCase(request.getWorkCity(),dcTicketModel.getWorkCity())) {
				 	pre.put(DispatchControllerConstants.WORKCITY, dcTicketModel.getWorkCity());
					dcTicketModel.setWorkCity(StringUtils.capitalize(request.getWorkCity()));
					post.put(DispatchControllerConstants.WORKCITY, request.getWorkCity());
			 }
			 
			if(!StringUtils.equalsIgnoreCase(request.getWorkState(),dcTicketModel.getWorkState())) {
				 	pre.put(DispatchControllerConstants.WORKSTATE, dcTicketModel.getWorkState());
					dcTicketModel.setWorkState(StringUtils.upperCase(request.getWorkState()));
					post.put(DispatchControllerConstants.WORKSTATE, request.getWorkState());
			 }
			 
			 if(!StringUtils.equalsIgnoreCase(request.getWorkCounty(),dcTicketModel.getWorkCounty())) {
				 	pre.put(DispatchControllerConstants.WORKCOUNTY, dcTicketModel.getWorkCounty());
					dcTicketModel.setWorkCounty(StringUtils.capitalize(request.getWorkCounty()));
					post.put(DispatchControllerConstants.WORKCOUNTY, request.getWorkCounty());
			 }
			 
		
			 if(!StringUtils.equalsIgnoreCase(request.getWorkStreet(),dcTicketModel.getWorkStreet())) {
				 	pre.put(DispatchControllerConstants.WORKSTREET, dcTicketModel.getWorkStreet());
					dcTicketModel.setWorkStreet(request.getWorkStreet());
					post.put(DispatchControllerConstants.WORKSTREET, request.getWorkStreet());
			 }
			 
			 
			 if(!StringUtils.equalsIgnoreCase(request.getWorkAddress(),dcTicketModel.getWorkAddress())) {
				 	pre.put(DispatchControllerConstants.WORKADDRESS, dcTicketModel.getWorkAddress());
					dcTicketModel.setWorkAddress(request.getWorkAddress());
					post.put(DispatchControllerConstants.WORKADDRESS, request.getWorkAddress());
			 }
			 

			 if(!StringUtils.equalsIgnoreCase(request.getWorkZip(),dcTicketModel.getWorkZip())) {
				 	pre.put(DispatchControllerConstants.WORKZIP, dcTicketModel.getWorkZip());
					dcTicketModel.setWorkZip(request.getWorkZip());
					post.put(DispatchControllerConstants.WORKZIP, request.getWorkZip());
			 }
			 
			 if(!StringUtils.equalsIgnoreCase(request.getTechnicianId(),dcTicketModel.getTechnicianId())) {
				 	pre.put(DispatchControllerConstants.TECHNICIANID, dcTicketModel.getTechnicianId());
					dcTicketModel.setTechnicianId(request.getTechnicianId());
					post.put(DispatchControllerConstants.TECHNICIANID, request.getTechnicianId());
			 }
			
			 if(!StringUtils.equalsIgnoreCase(request.getTechnicianFirstName(),dcTicketModel.getTechnicianFirstName())) {
				 	pre.put(DispatchControllerConstants.TECHNICIAN_FIRSTNAME, dcTicketModel.getTechnicianFirstName());
					dcTicketModel.setTechnicianFirstName(StringUtils.capitalize(request.getTechnicianFirstName()));
					post.put(DispatchControllerConstants.TECHNICIAN_FIRSTNAME, request.getTechnicianFirstName());
			 }
			 
			 if(!StringUtils.equalsIgnoreCase(request.getTechnicianLastName(),dcTicketModel.getTechnicianLastName())) {
				 	pre.put(DispatchControllerConstants.TECHNICIAN_LASTNAME, dcTicketModel.getTechnicianLastName());
					dcTicketModel.setTechnicianLastName(StringUtils.capitalize(request.getTechnicianLastName()));
					post.put(DispatchControllerConstants.TECHNICIAN_LASTNAME, request.getTechnicianLastName());
			 }
			
			 if(!StringUtils.equalsIgnoreCase(request.getTechnicianEmailId(),dcTicketModel.getTechnicianEmailId())) {
				 	pre.put(DispatchControllerConstants.TECHNICIAN_EMAILID, dcTicketModel.getTechnicianEmailId());
					dcTicketModel.setTechnicianEmailId(request.getTechnicianEmailId());
					post.put(DispatchControllerConstants.TECHNICIAN_EMAILID, request.getTechnicianEmailId());
			 }
			 if(!StringUtils.equalsIgnoreCase(request.getSupervisorPolygonId(),dcTicketModel.getSupervisorPolygonId())) {
				 	pre.put(DispatchControllerConstants.SUPERVISORPOLYGONID, dcTicketModel.getSupervisorPolygonId());
					dcTicketModel.setSupervisorPolygonId(request.getSupervisorPolygonId());
					post.put(DispatchControllerConstants.SUPERVISORPOLYGONID, request.getSupervisorPolygonId());
			 }
			 if(!StringUtils.equalsIgnoreCase(request.getMasterTicketExternalId(),dcTicketModel.getMasterTicketExternalId())) {
				 	pre.put(DispatchControllerConstants.MASTER_TICKET_EXTERNALID, dcTicketModel.getMasterTicketExternalId());
					dcTicketModel.setMasterTicketExternalId(request.getMasterTicketExternalId());
					post.put(DispatchControllerConstants.MASTER_TICKET_EXTERNALID, request.getMasterTicketExternalId());
			 }
			 if((!request.getEmergencyFlag().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getEmergencyFlag(),dcTicketModel.getEmergencyFlag())) {
				 	pre.put(DispatchControllerConstants.EMERGENCY_FLAG, dcTicketModel.getEmergencyFlag());
					dcTicketModel.setEmergencyFlag(request.getEmergencyFlag());
					post.put(DispatchControllerConstants.EMERGENCY_FLAG, request.getEmergencyFlag());
			 }
			 if( (!request.getAfterHours().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getAfterHours(),dcTicketModel.getAfterHours())) {
				 	pre.put(DispatchControllerConstants.AFTER_HOURS, dcTicketModel.getAfterHours());
					dcTicketModel.setAfterHours(request.getAfterHours());
					post.put(DispatchControllerConstants.AFTER_HOURS, request.getAfterHours());
			 }
			 
			 if( (!request.getIsAssistTicket().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getIsAssistTicket(),dcTicketModel.getIsAssistTicket())) {
				 	pre.put(DispatchControllerConstants.IS_ASSIST_TICKET, dcTicketModel.getIsAssistTicket());
					dcTicketModel.setIsAssistTicket(request.getIsAssistTicket());
					post.put(DispatchControllerConstants.IS_ASSIST_TICKET, request.getIsAssistTicket());
			 }
			 if((!request.getMultiTechnicianTicket().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getMultiTechnicianTicket(),dcTicketModel.getMultiTechnicianTicket()) ) {
				 	pre.put(DispatchControllerConstants.MULTI_TECHNICIAN_TICKET, dcTicketModel.getMultiTechnicianTicket());
					dcTicketModel.setMultiTechnicianTicket(request.getMultiTechnicianTicket());
					post.put(DispatchControllerConstants.MULTI_TECHNICIAN_TICKET, request.getMultiTechnicianTicket());
			 }
			 
			
			 Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
			 String preAction = gson.toJson(pre);
			
			 String postAction = gson.toJson(post);
			
			 log.info("postAction"+postAction);
			
			 TicketActionTrail ticketActionTrail = ticketActionTrail(DispatchControllerConstants.TICKET_UPDATED,DispatchControllerConstants.ENRICHMENT_BOT,preAction,postAction);
			 log.info(" update TicketActionTrail details 1 {} ", ticketActionTrail);
			 dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
			 
			 dcTicketModel.setGlobalStatus(ticketStatus);
			 dcTicketModel.setActionOnTicket(action);
			 
			 //Update ticket data
			ticketDataRepo.save(dcTicketModel);
			
			//Save to NSAUDIT
			saveNsAudit(request,preAction,postAction);
			
	
			
			
	}

	private void UpdateTechnicianAndSolution(Ticket dcTicketModel) {
		//Save technicianDCSolver and Solution
		if(dcTicketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED))
			{
				// Find the tickets to transfer based on ticketNumber and fromtechnicianId
				AgentAssignmentSolutionModel TechnicianAssignment = assignmentRepository
						.findByAgentTicketListConversationIdAndAgentTicketListTicketNumber(dcTicketModel.getConversationId(), dcTicketModel.getTicketNumber()).findFirst().orElse(null);
						
				log.info("ticket in assignment solution {} ",TechnicianAssignment);
				if (TechnicianAssignment != null) {
					log.info("Ticket present in Agent bucket.");
					
					// Find the ticket to be updated in the Technician's ticket list
					Ticket ticketToTransfer = null;
					List<Ticket> ticketList = TechnicianAssignment.getAgent().getTicketList();
					//TODO gte index and add ticket to same index
					log.info("List of tickets {} ",ticketList);
					for (int i = 0; i < ticketList.size(); i++) {
						Ticket ticket = ticketList.get(i);
							if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), dcTicketModel.getTicketNumber())) {
							ticketToTransfer = ticket;
							// Remove ticket from the AgentAssignmentSolutionModel
								if (ticketToTransfer != null) {
									ticketList.remove(ticketToTransfer);

								}
								//Add updated ticket to corresponding index
								ticketList.add(i, dcTicketModel);
							break;
							}
					}
				
					technicianDataRepo.save(TechnicianAssignment.getAgent());
					assignmentRepository.save(TechnicianAssignment);

				}
				else
				{
					log.info("Ticket not present in Agent bucket.So bucket is not updated");
				}
			}
		
	}

	private void stickyRoutingEmergencyAssignment(String logKey, String businessId, String token, Ticket dcTicketModel,
			ExternalTicketRequestDTO request) {
		 try { 
				
				//Delay Added , need to make configurable 
	       	 Thread.sleep(DispatchControllerConstants.DEFAULT_EMERGENCY_ASSIGNMENT_THREAD_SLEEP);
	       	
			 String remarks="";
			 
		 boolean agentPresent = technicianDataRepo.existsByTechnicianIdAndIsActive(request.getTechnicianId(),DispatchControllerConstants.FLAG_Y);
		 if(!agentPresent)
		 {
			 remarks="Technician not found or inactive : "+request.getTechnicianId();
			 updateTicketAndTicketMaster(dcTicketModel,remarks);
			 
			 EmergencyTicketDetailsDTO dto=new EmergencyTicketDetailsDTO();
				dto.setConversationId(request.getConversationId());
				dto.setTicketNumber(request.getTicketNumber());

				EmergencyTicketRequest emergencyTicketRequest = new EmergencyTicketRequest();
				emergencyTicketRequest.getEmergencyTicketList().add(dto);

				executor.execute(()->{
					BusinessContext.setTenantId(businessId);
					BusinessTokenContext.setBusinessToken(token);
					callEmergencyTicketAssignmentAPI(emergencyTicketRequest,token);
				});
		 }
		 else
		 {
			 boolean agentAndSupPresent = technicianDataRepo.existsByTechnicianIdAndIsActiveAndSupervisorId(request.getTechnicianId(),DispatchControllerConstants.FLAG_Y,request.getSupervisorId());
			 if(!agentAndSupPresent)
			 {
				 remarks="Technician and supervisor are not found or inactive.TechnicianId : "+request.getTechnicianId()+" SupervisorId : "+request.getSupervisorId();					
				 updateTicketAndTicketMaster(dcTicketModel,remarks);
				 
				 EmergencyTicketDetailsDTO dto=new EmergencyTicketDetailsDTO();
					dto.setConversationId(request.getConversationId());
					dto.setTicketNumber(request.getTicketNumber());

					EmergencyTicketRequest emergencyTicketRequest = new EmergencyTicketRequest();
					emergencyTicketRequest.getEmergencyTicketList().add(dto);

					executor.execute(()->{
						BusinessContext.setTenantId(businessId);
						BusinessTokenContext.setBusinessToken(token);
						callEmergencyTicketAssignmentAPI(emergencyTicketRequest,token);
					});
			 }
			 else
			 {
				 	LocalTime now = LocalTime.now();
			        LocalTime startTime = LocalTime.of(7, 0); // 07:00 AM
			        LocalTime endTime = LocalTime.of(17, 0); // 05:00 PM
			        
			        if (!now.isAfter(startTime) && !now.isBefore(endTime)) {
			        	
			        	 remarks="CurrentTime is not between 0700 AM to 1700 PM : "+request.getTechnicianId();
						 updateTicketAndTicketMaster(dcTicketModel,remarks);
			        	
			        	EmergencyTicketDetailsDTO dto=new EmergencyTicketDetailsDTO();
						dto.setConversationId(request.getConversationId());
						dto.setTicketNumber(request.getTicketNumber());

						EmergencyTicketRequest emergencyTicketRequest = new EmergencyTicketRequest();
						emergencyTicketRequest.getEmergencyTicketList().add(dto);

						executor.execute(()->{
							BusinessContext.setTenantId(businessId);
							BusinessTokenContext.setBusinessToken(token);
							callEmergencyTicketAssignmentAPI(emergencyTicketRequest,token);
						});
						
			        }
					 else
					 {
						 LocalDate startDate = null ;
						 ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE));
					
						 startDate = zdt.toLocalDate();
						 TechnicianWorkHour technicianWorkHourByTech = technicianWorkHourRepository.findByTechEmpIdAndCalendarDate(request.getTechnicianId(),startDate);
						 
						 if(!ObjectUtils.isEmpty(technicianWorkHourByTech))
					    	{	
					    		long availableTime = Long.parseLong(technicianWorkHourByTech.getAvailableTime());
					    		log.info("availableTime"+availableTime);
					    		if(availableTime<0)
					    		{
					    			 remarks="Available time is less than 0 : "+availableTime;
									 updateTicketAndTicketMaster(dcTicketModel,remarks);
									 
									 	EmergencyTicketDetailsDTO dto=new EmergencyTicketDetailsDTO();
										dto.setConversationId(request.getConversationId());
										dto.setTicketNumber(request.getTicketNumber());

										EmergencyTicketRequest emergencyTicketRequest = new EmergencyTicketRequest();
										emergencyTicketRequest.getEmergencyTicketList().add(dto);

										executor.execute(()->{
											BusinessContext.setTenantId(businessId);
											BusinessTokenContext.setBusinessToken(token);
											callEmergencyTicketAssignmentAPI(emergencyTicketRequest,token);
										});
										
										
					    		}
					    		else
					    		{
					    			//available time is >0 
					    			TicketActionTrail ticketActionTrail = new TicketActionTrail();
									ticketActionTrail.setAction(DispatchControllerConstants.ACTION_ASSIGNMENT);
									ticketActionTrail.setActionBy(DispatchControllerConstants.KEYWORD_SYSTEM_INITIAL_ASSIGNMENT);
									ticketActionTrail.setActionOn(LocalDateTime.now());
									ticketActionTrail.setPreAction(DispatchControllerConstants.STATUS_UNASSIGNED);
									ticketActionTrail.setPostAction(DispatchControllerConstants.STATUS_ASSIGNED + " To " + dcTicketModel.getTechnicianId() + " : " + dcTicketModel.getTechnicianFirstName() + " " + dcTicketModel.getTechnicianLastName());
									dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
									
									dcTicketModel.setAssignmentDateTime(LocalDateTime.now());
									dcTicketModel.setGlobalStatus(DispatchControllerConstants.STATUS_ASSIGNED);
									dcTicketModel.setActionOnTicket(DispatchControllerConstants.STATUS_ASSIGNED);
					    		
					    			ticketDataRepo.save(dcTicketModel);
					    			
					    			//updateTicketMaster
					    			updateTicketMaster(dcTicketModel,ticketActionTrail);
					    		
					    			//Initial assignement to technician
					    			saveTechIntialAssigemnt(dcTicketModel);
					    			log.info("StickyRouting : technicianWorkHourByTech is not empty");
					    			log.info("StickyRouting : Initial assignement for emergency ticket : {} ",request.getTicketNumber());
				
					    		}
					    	}
						 else
						 {
							 remarks="Technician details not available in ts_technician_daily_task";
							 updateTicketAndTicketMaster(dcTicketModel,remarks);
							 
							 	EmergencyTicketDetailsDTO dto=new EmergencyTicketDetailsDTO();
								dto.setConversationId(request.getConversationId());
								dto.setTicketNumber(request.getTicketNumber());

								EmergencyTicketRequest emergencyTicketRequest = new EmergencyTicketRequest();
								emergencyTicketRequest.getEmergencyTicketList().add(dto);

								executor.execute(()->{
									BusinessContext.setTenantId(businessId);
									BusinessTokenContext.setBusinessToken(token);
									callEmergencyTicketAssignmentAPI(emergencyTicketRequest,token);
								});
								log.info("StickyRouting : technicianWorkHourByTech is  empty");
								log.info("StickyRouting : callEmergencyTicketAssignmentAPI for emergency ticket : {} ",request.getTicketNumber());
								
								
						 }
						  
					 }
			        
			 }
			 
	
			 
		 }
		 }catch(Exception e) {
				log.info("{} Unable to saveTechIntialAssigemnt, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
				
			}
		
	}

	private void stickyRoutingNormalAssignment(String logKey, String businessId, String token, Ticket dcTicketModel,
			ExternalTicketRequestDTO request) {
		 
		try { 
		
			//Delay Added , need to make configurable 
       	 Thread.sleep(DispatchControllerConstants.DEFAULT_EMERGENCY_ASSIGNMENT_THREAD_SLEEP);
       	
		 String remarks="";
		 boolean agentPresent = technicianDataRepo.existsByTechnicianIdAndIsActive(request.getTechnicianId(),DispatchControllerConstants.FLAG_Y);
		 if(!agentPresent)
		 {
			 remarks="Technician not found or inactive : "+request.getTechnicianId();
			 updateTicketAndTicketMaster(dcTicketModel,remarks);
		 }
		 else
		 {
			 boolean agentAndSupPresent = technicianDataRepo.existsByTechnicianIdAndIsActiveAndSupervisorId(request.getTechnicianId(),DispatchControllerConstants.FLAG_Y,request.getSupervisorId());
			 if(!agentAndSupPresent)
			 {
				 remarks="Technician and supervisor are not found or inactive.TechnicianId : "+request.getTechnicianId()+" SupervisorId : "+request.getSupervisorId();					
				 updateTicketAndTicketMaster(dcTicketModel,remarks);
			 }
			 else
			 {
				 LocalDate startDate = null ;
				 ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE));
			
				 startDate = zdt.toLocalDate();
				 TechnicianWorkHour technicianWorkHourByTech = technicianWorkHourRepository.findByTechEmpIdAndCalendarDate(request.getTechnicianId(),startDate);
				 
				 if(!ObjectUtils.isEmpty(technicianWorkHourByTech))
			    	{	
			    		long availableTime = Long.parseLong(technicianWorkHourByTech.getAvailableTime());
			    		log.info("availableTime"+availableTime);
			    		if(availableTime<0)
			    		{
			    			 remarks="Available time is less than 0 : "+availableTime;
							 updateTicketAndTicketMaster(dcTicketModel,remarks);
			    		}
			    		else
			    		{
			    			//available time is >0 
			    			TicketActionTrail ticketActionTrail = new TicketActionTrail();
							ticketActionTrail.setAction(DispatchControllerConstants.ACTION_ASSIGNMENT);
							ticketActionTrail.setActionBy(DispatchControllerConstants.KEYWORD_SYSTEM_INITIAL_ASSIGNMENT);
							ticketActionTrail.setActionOn(LocalDateTime.now());
							ticketActionTrail.setPreAction(DispatchControllerConstants.STATUS_UNASSIGNED);
							ticketActionTrail.setPostAction(DispatchControllerConstants.STATUS_ASSIGNED + " To " + dcTicketModel.getTechnicianId() + " : " + dcTicketModel.getTechnicianFirstName() + " " + dcTicketModel.getTechnicianLastName());
							dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
							
							dcTicketModel.setAssignmentDateTime(LocalDateTime.now());
							dcTicketModel.setGlobalStatus(DispatchControllerConstants.STATUS_ASSIGNED);
							dcTicketModel.setActionOnTicket(DispatchControllerConstants.STATUS_ASSIGNED);
			    			ticketDataRepo.save(dcTicketModel);
			    		
			    			//Initial assignement to technician
			    			saveTechIntialAssigemnt(dcTicketModel);
			    			
			    			//updateTicketMaster
			    			updateTicketMaster(dcTicketModel,ticketActionTrail);
			    			log.info("StickyRouting : technicianWorkHourByTech is not empty");
			    			log.info("StickyRouting : Initial assignement for normal ticket : {} ",request.getTicketNumber());
							
		
			    		}
			    	}
				 else
				 {
					//available time is >0 
		    			TicketActionTrail ticketActionTrail = new TicketActionTrail();
						ticketActionTrail.setAction(DispatchControllerConstants.ACTION_ASSIGNMENT);
						ticketActionTrail.setActionBy(DispatchControllerConstants.KEYWORD_SYSTEM_INITIAL_ASSIGNMENT);
						ticketActionTrail.setActionOn(LocalDateTime.now());
						ticketActionTrail.setPreAction(DispatchControllerConstants.STATUS_UNASSIGNED);
						ticketActionTrail.setPostAction(DispatchControllerConstants.STATUS_ASSIGNED + " To " + dcTicketModel.getTechnicianId() + " : " + dcTicketModel.getTechnicianFirstName() + " " + dcTicketModel.getTechnicianLastName());
						dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
						
						dcTicketModel.setAssignmentDateTime(LocalDateTime.now());
						dcTicketModel.setGlobalStatus(DispatchControllerConstants.STATUS_ASSIGNED);
						dcTicketModel.setActionOnTicket(DispatchControllerConstants.STATUS_ASSIGNED);
		    		
		    			ticketDataRepo.save(dcTicketModel);
		    			
		    			//Initial assignement to technician
		    			saveTechIntialAssigemnt(dcTicketModel);
		    			
		    			//updateTicketMaster
		    			updateTicketMaster(dcTicketModel,ticketActionTrail);
		    			log.info("StickyRouting : technicianWorkHourByTech is empty");
		    			log.info("StickyRouting : Initial assignement for normal ticket : {} ",request.getTicketNumber());
						
		    			
				 }
				  
			 }
			 
		 }
		}catch(Exception e) {
			log.info("{} Unable to saveTechIntialAssigemnt, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			
		}

		
	}

	private void updateTicketMaster(Ticket dcTicketModel, TicketActionTrail ticketActionTrail) {
			//Update ticketmaster
			LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();
			lumenCollectionUpdateDTO.setGlobalStatus(DispatchControllerConstants.STATUS_ASSIGNED);
			lumenCollectionUpdateDTO.setActionOnTicket(DispatchControllerConstants.STATUS_ASSIGNED);
			lumenCollectionUpdateDTO.setTechnicianId(dcTicketModel.getTechnicianId());
			lumenCollectionUpdateDTO.setTechnicianFirstName(dcTicketModel.getTechnicianFirstName());
			lumenCollectionUpdateDTO.setTechnicianLastName(dcTicketModel.getTechnicianLastName());
			lumenCollectionUpdateDTO.setTechnicianEmailId(dcTicketModel.getTechnicianEmailId());
			lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
			lumenCollectionUpdateDTO.setTicketNumber(dcTicketModel.getTicketNumber());
			lumenCollectionUpdateDTO.setConversationId(dcTicketModel.getConversationId());
			lumenCollectionUpdateDTO.setSupervisorId(dcTicketModel.getSupervisorId());
			lumenCollectionUpdateDTO.setSupervisorName(dcTicketModel.getSupervisorName());
			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.ACTION_ASSIGNMENT);
			lumenCollectionUpdateDTO.setError("");
			lumenCollectionUpdateDTO.setAssignmentDateTime(LocalDateTime.now());
			// Updating master ticket collection
			
			ticketRepository.updateLumenTicketCollection(lumenCollectionUpdateDTO);
			try {
				
		        String businessToken = BusinessTokenContext.getBusinessToken();
			    executor.execute(()->{
				BusinessContext.setTenantId(BusinessContext.getTenantId());
				BusinessTokenContext.setBusinessToken(businessToken);
				dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO,BusinessContext.getTenantId(),businessToken);
			});
			}
			catch(Exception e)
			{
				log.info("Unable to save Audit due to {} ", e.getMessage());
			}
			
		
	}

	private void updateTicketAndTicketMaster(Ticket dcTicketModel,String remarks) {
		
			TicketActionTrail ticketActionTrail = new TicketActionTrail();
			ticketActionTrail.setAction(DispatchControllerConstants.ACTION_ASSIGNMENT);
			ticketActionTrail.setActionBy(DispatchControllerConstants.DEFAULT_TECHNICIAN_ID+":"+DispatchControllerConstants.SYSTEM_DC);
			ticketActionTrail.setActionOn(LocalDateTime.now());
			ticketActionTrail.setPreAction(DispatchControllerConstants.STATUS_UNASSIGNED);
			ticketActionTrail.setPostAction(DispatchControllerConstants.STATUS_UNASSIGNED);
		
		// Updating Ticket DC Solver
		    dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
			
			dcTicketModel.setTechnicianId("");
			dcTicketModel.setTechnicianFirstName("");
			dcTicketModel.setTechnicianLastName("");
			dcTicketModel.setTechnicianEmailId("");
					
			ticketDataRepo.save(dcTicketModel);
			
	//Update ticketmaster
			LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();
			 
			lumenCollectionUpdateDTO.setActionOnTicket(DispatchControllerConstants.STATUS_UNASSIGNED);
			lumenCollectionUpdateDTO.setGlobalStatus(DispatchControllerConstants.STATUS_UNASSIGNED);
			lumenCollectionUpdateDTO.setAssignmentDateTime(LocalDateTime.now());
			lumenCollectionUpdateDTO.setTechnicianId("");
			lumenCollectionUpdateDTO.setTechnicianFirstName("");
			lumenCollectionUpdateDTO.setTechnicianLastName("");
			lumenCollectionUpdateDTO.setTechnicianEmailId("");
			lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
			lumenCollectionUpdateDTO.setTicketNumber(dcTicketModel.getTicketNumber());
			lumenCollectionUpdateDTO.setConversationId(dcTicketModel.getConversationId());
			lumenCollectionUpdateDTO.setSupervisorId(dcTicketModel.getSupervisorId());
			lumenCollectionUpdateDTO.setSupervisorName(dcTicketModel.getSupervisorName());
			lumenCollectionUpdateDTO.setRemarks(remarks);
			lumenCollectionUpdateDTO.setError("");
			
			
			// Updating master ticket collection
			
			ticketRepository.updateLumenTicketCollection(lumenCollectionUpdateDTO);
			try {
				String businessId = BusinessContext.getTenantId();
		        String businessToken = BusinessTokenContext.getBusinessToken();
			executor.execute(()->{
				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(businessToken);
				dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO,businessId,businessToken);
			});
			}
			catch(Exception e)
			{
				log.info("Unable to save Audit due to {} ", e.getMessage());
			}
			 
	 
	 }
	 private void saveNsAudit(ExternalTicketRequestDTO request, String preAction,String postAction) {
			
			
			LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();
			TicketActionTrail ticketActionTrail = ticketActionTrail(DispatchControllerConstants.DC_TICKET_SAVE ,DispatchControllerConstants.ENRICHMENT_BOT,preAction,postAction);
			     
			lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
			lumenCollectionUpdateDTO.setTicketNumber(request.getTicketNumber());
			lumenCollectionUpdateDTO.setConversationId(request.getConversationId());
			
			lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.REQUEST_RECEIVED_FROM_ENRICHMENT_BOT);
			
			try {
				String businessId = BusinessContext.getTenantId();
		        String businessToken = BusinessTokenContext.getBusinessToken();
			executor.execute(()->{
				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(businessToken);
				dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO,businessId,businessToken);
			});
			}
			catch(Exception e)
			{
				log.info("Unable to save Audit due to {} ", e.getMessage());
			}
		
	}

	 private void missingInfoNsAudit(ExternalTicketRequestDTO request, Map<String, Object> missingTicketDetails) {
			Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
			String missingData = gson.toJson(missingTicketDetails);
			
			LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();
			TicketActionTrail ticketActionTrail = ticketActionTrail(DispatchControllerConstants.DC_TICKET_SAVE ,DispatchControllerConstants.ENRICHMENT_BOT,DispatchControllerConstants.REQUEST_RECEIVED_FROM_ENRICHMENT_BOT,missingData);
			     
			lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
			lumenCollectionUpdateDTO.setTicketNumber(request.getTicketNumber());
			lumenCollectionUpdateDTO.setConversationId(request.getConversationId());
			
			lumenCollectionUpdateDTO.setRemarks(missingData);
			
			try {
				String businessId = BusinessContext.getTenantId();
		        String businessToken = BusinessTokenContext.getBusinessToken();
			executor.execute(()->{
				BusinessContext.setTenantId(businessId);
				BusinessTokenContext.setBusinessToken(businessToken);
				dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO,businessId,businessToken);
			});
			}
			catch(Exception e)
			{
				log.info("Unable to save Audit due to {} ", e.getMessage());
			}
		
	}

	public void updateTicketDetailsWithoutTechDetails(ExternalTicketRequestDTO request, Ticket dcTicketModel, boolean isTicketDetailsMissing) {
		
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		 
		 Map<String,Object> pre=new HashMap<>();
		 Map<String,Object> post=new HashMap<>();
	
		 if(!StringUtils.isEmpty(request.getTicketNumber811()) && !StringUtils.equalsIgnoreCase(request.getTicketNumber811(),dcTicketModel.getTicketNumber811())) {
				pre.put(DispatchControllerConstants.TICKETNUMBER_811, dcTicketModel.getTicketNumber811());
				dcTicketModel.setTicketNumber811(request.getPolygonId());
				post.put(DispatchControllerConstants.TICKETNUMBER_811, request.getTicketNumber811());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getPolygonId(),dcTicketModel.getPolygonId()) ) {
			pre.put(DispatchControllerConstants.POLYGON_ID, dcTicketModel.getPolygonId());
			dcTicketModel.setPolygonId(request.getPolygonId());
			post.put(DispatchControllerConstants.POLYGON_ID, request.getPolygonId());
		 }
		 
		 
		 if(request.getTicketETA() != dcTicketModel.getTicketETA()) {
		 pre.put(DispatchControllerConstants.TICKETETA, dcTicketModel.getTicketETA());
		 dcTicketModel.setTicketETA(request.getTicketETA());
		 post.put(DispatchControllerConstants.TICKETETA, request.getTicketETA());
		  }
		 
		 if(!request.getCertificationRequired().equals(dcTicketModel.getCertificationRequired())) {
			    pre.put(DispatchControllerConstants.CERTIFICATION_REQUIRED, dcTicketModel.getCertificationRequired());
				dcTicketModel.setCertificationRequired(request.getCertificationRequired());
				post.put(DispatchControllerConstants.CERTIFICATION_REQUIRED, request.getCertificationRequired());
				
		          }
		 
		 if(request.getTicketScore() != dcTicketModel.getTicketScore() ) {
				pre.put(DispatchControllerConstants.TICKETSCORE, dcTicketModel.getTicketScore());
				dcTicketModel.setTicketScore(request.getTicketScore());
				post.put(DispatchControllerConstants.TICKETSCORE, request.getTicketScore());
			 }
		
			try {  
					if(!StringUtils.equalsIgnoreCase(request.getTicketDueDateAndTime(),dcTicketModel.getTicketDueDateAndTime().format(DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT)) )) {
						pre.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, dcTicketModel.getTicketDueDateAndTime());
						dcTicketModel.setTicketDueDateAndTime(LocalDateTime.parse(request.getTicketDueDateAndTime(), dtf));
						post.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, request.getTicketDueDateAndTime());
					}
					
					if(!StringUtils.equalsIgnoreCase(request.getCreatedDateTime(),dcTicketModel.getCreatedDateTime().format(DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT)) )) {
						pre.put(DispatchControllerConstants.CREATEDDATEANDTIME, dcTicketModel.getCreatedDateTime());
						dcTicketModel.setCreatedDateTime(LocalDateTime.parse(request.getCreatedDateTime(), dtf));
						post.put(DispatchControllerConstants.CREATEDDATEANDTIME, request.getCreatedDateTime());
					}
					
			}
			catch(Exception e)
			{
				 log.info("Unable to parse datetime"+e.getMessage());	
			}
			
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorId(),dcTicketModel.getSupervisorId())) {
				pre.put(DispatchControllerConstants.SUPERVISORID, dcTicketModel.getSupervisorId());
				dcTicketModel.setSupervisorId(request.getSupervisorId());
				post.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
			 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorName(),dcTicketModel.getSupervisorName())) {
				pre.put(DispatchControllerConstants.SUPERVISORNAME, dcTicketModel.getSupervisorName());
				dcTicketModel.setSupervisorName(StringUtils.capitalize(request.getSupervisorName()));
				post.put(DispatchControllerConstants.SUPERVISOR_NAME, request.getSupervisorName());
			 }

		 
		 if(!Objects.equals(request.getLocation().getLatitude(),dcTicketModel.getLocation().getLatitude()) || !Objects.equals(request.getLocation().getLongitude(),dcTicketModel.getLocation().getLongitude()) ) {
			 pre.put(DispatchControllerConstants.LOCATION, dcTicketModel.getLocation().toString());
			 dcTicketModel.setLocation(request.getLocation());
			 post.put(DispatchControllerConstants.LOCATION, request.getLocation().toString());
		     }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getTicketType(),dcTicketModel.getTicketType())) {
				pre.put(DispatchControllerConstants.TICKETTYPE, dcTicketModel.getTicketType());
				dcTicketModel.setTicketType(request.getTicketType());
				post.put(DispatchControllerConstants.TICKETTYPE, request.getTicketType());
			 }
		 
		 
		 if(!StringUtils.equalsIgnoreCase(request.getWorkType(),dcTicketModel.getWorkType())) {
			 	pre.put(DispatchControllerConstants.WORKTYPE, dcTicketModel.getWorkType());
				dcTicketModel.setWorkType(request.getWorkType());
				post.put(DispatchControllerConstants.WORKTYPE, request.getWorkType());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getWorkCity(),dcTicketModel.getWorkCity())) {
			 	pre.put(DispatchControllerConstants.WORKCITY, dcTicketModel.getWorkCity());
				dcTicketModel.setWorkCity(StringUtils.capitalize(request.getWorkCity()));
				post.put(DispatchControllerConstants.WORKCITY, request.getWorkCity());
		 }
		 
		if(!StringUtils.equalsIgnoreCase(request.getWorkState(),dcTicketModel.getWorkState())) {
			 	pre.put(DispatchControllerConstants.WORKSTATE, dcTicketModel.getWorkState());
				dcTicketModel.setWorkState(StringUtils.upperCase(request.getWorkState()));
				post.put(DispatchControllerConstants.WORKSTATE, request.getWorkState());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getWorkCounty(),dcTicketModel.getWorkCounty())) {
			 	pre.put(DispatchControllerConstants.WORKCOUNTY, dcTicketModel.getWorkCounty());
				dcTicketModel.setWorkCounty(StringUtils.capitalize(request.getWorkCounty()));
				post.put(DispatchControllerConstants.WORKCOUNTY, request.getWorkCounty());
		 }
		 
	
		 if(!StringUtils.equalsIgnoreCase(request.getWorkStreet(),dcTicketModel.getWorkStreet())) {
			 	pre.put(DispatchControllerConstants.WORKSTREET, dcTicketModel.getWorkStreet());
				dcTicketModel.setWorkStreet(request.getWorkStreet());
				post.put(DispatchControllerConstants.WORKSTREET, request.getWorkStreet());
		 }

		 if(!StringUtils.equalsIgnoreCase(request.getWorkAddress(),dcTicketModel.getWorkAddress())) {
			 	pre.put(DispatchControllerConstants.WORKADDRESS, dcTicketModel.getWorkAddress());
				dcTicketModel.setWorkAddress(request.getWorkAddress());
				post.put(DispatchControllerConstants.WORKADDRESS, request.getWorkAddress());
		 }
		 

		 if(!StringUtils.equalsIgnoreCase(request.getWorkZip(),dcTicketModel.getWorkZip())) {
			 	pre.put(DispatchControllerConstants.WORKZIP, dcTicketModel.getWorkZip());
				dcTicketModel.setWorkZip(request.getWorkZip());
				post.put(DispatchControllerConstants.WORKZIP, request.getWorkZip());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorPolygonId(),dcTicketModel.getSupervisorPolygonId())) {
			 	pre.put(DispatchControllerConstants.SUPERVISORPOLYGONID, dcTicketModel.getSupervisorPolygonId());
				dcTicketModel.setSupervisorPolygonId(request.getSupervisorPolygonId());
				post.put(DispatchControllerConstants.SUPERVISORPOLYGONID, request.getSupervisorPolygonId());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getMasterTicketExternalId(),dcTicketModel.getMasterTicketExternalId())) {
			 	pre.put(DispatchControllerConstants.MASTER_TICKET_EXTERNALID, dcTicketModel.getMasterTicketExternalId());
				dcTicketModel.setMasterTicketExternalId(request.getMasterTicketExternalId());
				post.put(DispatchControllerConstants.MASTER_TICKET_EXTERNALID, request.getMasterTicketExternalId());
		 }
		 if((!request.getEmergencyFlag().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getEmergencyFlag(),dcTicketModel.getEmergencyFlag()) ) {
			 	pre.put(DispatchControllerConstants.EMERGENCY_FLAG, dcTicketModel.getEmergencyFlag());
				dcTicketModel.setEmergencyFlag(request.getEmergencyFlag());
				post.put(DispatchControllerConstants.EMERGENCY_FLAG, request.getEmergencyFlag());
		 }
		 if((!request.getAfterHours().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getAfterHours(),dcTicketModel.getAfterHours()) ) {
			 	pre.put(DispatchControllerConstants.AFTER_HOURS, dcTicketModel.getAfterHours());
				dcTicketModel.setAfterHours(request.getAfterHours());
				post.put(DispatchControllerConstants.AFTER_HOURS, request.getAfterHours());
		 }
		 
		 if( (!request.getIsAssistTicket().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getIsAssistTicket(),dcTicketModel.getIsAssistTicket()) ) {
			 	pre.put(DispatchControllerConstants.IS_ASSIST_TICKET, dcTicketModel.getIsAssistTicket());
				dcTicketModel.setIsAssistTicket(request.getIsAssistTicket());
				post.put(DispatchControllerConstants.IS_ASSIST_TICKET, request.getIsAssistTicket());
		 }
		 if((!request.getMultiTechnicianTicket().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getMultiTechnicianTicket(),dcTicketModel.getMultiTechnicianTicket())) {
			 	pre.put(DispatchControllerConstants.MULTI_TECHNICIAN_TICKET, dcTicketModel.getMultiTechnicianTicket());
				dcTicketModel.setMultiTechnicianTicket(request.getMultiTechnicianTicket());
				post.put(DispatchControllerConstants.MULTI_TECHNICIAN_TICKET, request.getMultiTechnicianTicket());
		 }
		 //Commented : Reason : First ticket will not be done with this api.(sendticketDetailsToDC)
			/*
			 * if((!request.getIsFirstTicket().isEmpty()) &&
			 * !StringUtils.equalsIgnoreCase(request.getIsFirstTicket(),dcTicketModel.
			 * getIsFirstTicket())) { pre.put(DispatchControllerConstants.IS_FIRST_TICKET,
			 * dcTicketModel.getIsFirstTicket());
			 * dcTicketModel.setIsFirstTicket(request.getIsFirstTicket());
			 * post.put(DispatchControllerConstants.IS_FIRST_TICKET,
			 * request.getIsFirstTicket()); }
			 */
		 
		Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
		 String preAction = gson.toJson(pre);
		 
		 String postAction =gson.toJson(post);
		 	 
		 TicketActionTrail ticketActionTrail = ticketActionTrail(DispatchControllerConstants.TICKET_UPDATED,DispatchControllerConstants.ENRICHMENT_BOT,preAction,postAction);
		 log.info(" update TicketActionTrail details 1 {} ", ticketActionTrail);
		 dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
		
		//Update ticket data
		ticketDataRepo.save(dcTicketModel);	
		
		//Save to NSAUDIT
		saveNsAudit(request,preAction,postAction);
			
			
		}
	private void transferAPIcall(String fromTechnicianId, String toTechnicianId, String ticketNumber) {
		
		GroupByTransferList groupByTransferList = new GroupByTransferList();
		TransferTicketDTO transferTicketDTO = new TransferTicketDTO();
		transferTicketDTO.setFromSupervisorId("");
		transferTicketDTO.setToSupervisorId("");
		transferTicketDTO.setFromTechnicianId(fromTechnicianId);
		transferTicketDTO.setToTechnicianId(toTechnicianId);
		transferTicketDTO.setTicketNumbers(ticketNumber);
		transferTicketDTO.setActionBy(DispatchControllerConstants.DEFAULT_TECHNICIAN_ID+":"+DispatchControllerConstants.ENRICHMENT_BOT);
		
		List<TransferTicketDTO> TransferTicketDTOList= new ArrayList<>();
		TransferTicketDTOList.set(0, transferTicketDTO);
		
		groupByTransferList.setGroupByTransferList(TransferTicketDTOList);
		ResponseEntity<ApiResponseDto> responseEntity =  groupByActionService.GroupByAction(groupByTransferList);
	
		
	}
	
		private void transferAPI(String toTechnicianId,String ticketNumbers) {
		log.info("transfer call  {} ", ticketNumbers);
		GroupByTransferList groupByTransferList = new GroupByTransferList();
		TransferTicketDTO transferTicketDTO = new TransferTicketDTO();
		transferTicketDTO.setFromSupervisorId("");
		transferTicketDTO.setToSupervisorId("");
		transferTicketDTO.setFromTechnicianId("");
		transferTicketDTO.setToTechnicianId(toTechnicianId);
		transferTicketDTO.setTicketNumbers(ticketNumbers);
		transferTicketDTO.setActionBy(toTechnicianId +" :"+DispatchControllerConstants.FIRST_TICKET_ASSIGNMENT);
		
		List<TransferTicketDTO> TransferTicketDTOList= new ArrayList<>();
		TransferTicketDTOList.add(transferTicketDTO);
		
		groupByTransferList.setGroupByTransferList(TransferTicketDTOList);
		ResponseEntity<ApiResponseDto> responseEntity =  groupByActionService.GroupByAction(groupByTransferList);
	
		
	}

	private void saveTechIntialAssigemnt(Ticket dcTicketModel) {
		// TODO Auto-generated method stub
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.FILE_DATETIME_FORMAT);
		Agent agent = technicianDataRepo.findByTechnicianId(dcTicketModel.getTechnicianId());
		if(!ObjectUtils.isEmpty(agent)) {

			//Set Tech Details in DC
			
			agent.getTicketList().add(dcTicketModel);
			agent.setTotalWorkHourGlobal(0);
			
			Location previousLocation = agent.getLocation();
			long totalWorkHourGlobal = 0;
			double evaluatedDistanceLocal=0.0;
			for(Ticket ticketLocal : agent.getTicketList()){
				
				//long travelTime = dispatchControllerSupportUtils.calculateDistance(previousLocation,ticketLocal.getLocation()) / 60000;
				
				RouteSolverPath routeSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(previousLocation,ticketLocal.getLocation());
				long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath) ? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME : routeSolverPath.getTime();
				evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
				long travelTime = travelTimeLocal / 60000;
				
				
				long workHourTime = agent.getTotalWorkHourGlobal() 
						+ travelTime
						+ ticketLocal.getTicketETA();
				totalWorkHourGlobal += workHourTime;
				
				previousLocation = ticketLocal.getLocation();
				
			}
			agent.setTotalWorkHourGlobal(totalWorkHourGlobal);
			agent.setEvaluatedDistance(evaluatedDistanceLocal);
			
			
			String assignmentStatus = agent.getAvailableTime() > agent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED 
					: agent.getAvailableTime() == agent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED : DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;
			
			agent.setAssignmentStatus(assignmentStatus);
			
			technicianDataRepo.save(agent);
			
			LocalDateTime startDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
			LocalDateTime endDateTime =LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);

			long dcSolverProcessIdStart = Long.parseLong(startDateTime.format(dtf));
			long dcSolverProcessIdEnd = Long.parseLong(endDateTime.format(dtf));
			
			Pageable processPage = PageRequest.of(0, 1);
			
			Page<AgentAssignmentSolutionModel> agentAssignmentSolutionModelPageCurrent = assignmentRepository.findByDcSolverProcessIdBetween(dcSolverProcessIdStart,dcSolverProcessIdEnd,processPage);
			List<AgentAssignmentSolutionModel> agentAssignmentSolutionModelListCurrent = agentAssignmentSolutionModelPageCurrent.getContent();

			
			Page<AgentAssignmentSolutionModel> agentAssignmentSolutionModelPage = assignmentRepository.findByAgentTechnicianIdAndDcSolverProcessIdBetween(agent.getTechnicianId(),dcSolverProcessIdStart,dcSolverProcessIdEnd,processPage);
			List<AgentAssignmentSolutionModel> agentAssignmentSolutionModelList = agentAssignmentSolutionModelPage.getContent();

			if(!CollectionUtils.isEmpty(agentAssignmentSolutionModelList)) {
				AgentAssignmentSolutionModel agentAssignmentSolutionModel = agentAssignmentSolutionModelList.get(0);
				Agent solutionAgent = agentAssignmentSolutionModel.getAgent();
				solutionAgent.getTicketList().add(dcTicketModel);
				
				 previousLocation = solutionAgent.getLocation();
				 totalWorkHourGlobal = 0;
				 double evaluatedDistance=0.0;
				for(Ticket ticketLocal : solutionAgent.getTicketList()){
					
					//long travelTime = dispatchControllerSupportUtils.calculateDistance(previousLocation,ticketLocal.getLocation()) / 60000;
					RouteSolverPath routeSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(previousLocation,ticketLocal.getLocation());
					long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath) ? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME : routeSolverPath.getTime();
					evaluatedDistance = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
					long travelTime = travelTimeLocal / 60000;
					
					long workHourTime = solutionAgent.getTotalWorkHourGlobal() 
							+ travelTime
							+ ticketLocal.getTicketETA();
					totalWorkHourGlobal += workHourTime;
					
					previousLocation = ticketLocal.getLocation();
					
				}
				solutionAgent.setTotalWorkHourGlobal(totalWorkHourGlobal);
				solutionAgent.setEvaluatedDistance(evaluatedDistance);
				
				 assignmentStatus = solutionAgent.getAvailableTime() > solutionAgent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED 
						: solutionAgent.getAvailableTime() == solutionAgent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED : DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;
				
				solutionAgent.setAssignmentStatus(assignmentStatus);
				
				technicianDataRepo.save(solutionAgent);
				solutionAgent.setTotalWorkHourGlobal(totalWorkHourGlobal);
				agentAssignmentSolutionModel.setAgent(solutionAgent);
				assignmentRepository.save(agentAssignmentSolutionModel);
				
			}else {
				AgentAssignmentSolutionModel agentAssignmentSolutionModel = new AgentAssignmentSolutionModel();
				if(!agentAssignmentSolutionModelListCurrent.isEmpty()) {
					agentAssignmentSolutionModel.setDcSolverProcessId(agentAssignmentSolutionModelListCurrent.get(0).getDcSolverProcessId());
					agentAssignmentSolutionModel.setDcSolverTaskId(agentAssignmentSolutionModelListCurrent.get(0).getDcSolverTaskId());
				}else {
					agentAssignmentSolutionModel.setDcSolverProcessId(Long.parseLong(LocalDateTime.now().format(dtf)));
					agentAssignmentSolutionModel.setDcSolverTaskId(Long.parseLong(LocalDateTime.now().format(dtf)));
				}
				agentAssignmentSolutionModel.setTimestamp(LocalDateTime.now());
				
				
				agentAssignmentSolutionModel.setAgent(agent);
				assignmentRepository.save(agentAssignmentSolutionModel);
				
				//Comment Due to PTO Logic Changes
//				AgentAvailabilityLookUp agentAvailabilityLookUp = new AgentAvailabilityLookUp();
//				agentAvailabilityLookUp.setAvailabilityStatus(DispatchControllerConstants.AGENT_AVAILABILITY_STATUS_SPECIAL_ASSIGNMENT);
//				agentAvailabilityLookUp.setCalenderDate(LocalDate.now());
//				agentAvailabilityLookUp.setTicket(dcTicketModel);
//				agentAvailabilityLookUp.setTimestamp(LocalDateTime.now());
//				agentAvailabilityLookUp.setTimeoff(dcTicketModel.getTicketETA());
//				agentAvailabilityLookUp.setLocation(dcTicketModel.getLocation());
//				agentAvailabilityLookUpRepo.save(agentAvailabilityLookUp);
			}
		}
		if(StringUtils.equalsIgnoreCase(dcTicketModel.getIsAssistTicket(), DispatchControllerConstants.YES))
		{
			Ticket ticket;
			ticket = ticketDataRepo.findByMasterTicketExternalIdAndIsAssistTicket(dcTicketModel.getMasterTicketExternalId(),DispatchControllerConstants.NO);
			ticket.setAssistPresent(DispatchControllerConstants.YES);
			ticketDataRepo.save(ticket);
			
			
		}
		
		
	}
	
		 private void updateAssignedPastDueTicket(Ticket dcTicketModel) {
		 
				// Find the tickets to transfer based on ticketNumber and fromtechnicianId
				AgentAssignmentSolutionModel TechnicianAssignment = assignmentRepository
						.findByAgentTicketListConversationIdAndAgentTicketListTicketNumber(dcTicketModel.getConversationId(), dcTicketModel.getTicketNumber()).findFirst().orElse(null);

				log.info(" TechnicianId {} ", TechnicianAssignment);
				if (TechnicianAssignment != null) {

					log.info("ticket in assignment solution {} ");
					// Find the ticket to be updated in the Technician's ticket list
					Ticket ticketToTransfer = null;
					List<Ticket> ticketList = TechnicianAssignment.getAgent().getTicketList();
					//TODO gte index and add ticket to same index
					log.info("List of tickets {} ",ticketList);
					for (int i = 0; i < ticketList.size(); i++) {
						Ticket ticket = ticketList.get(i);
							if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), dcTicketModel.getTicketNumber())) {
							ticketToTransfer = ticket;
							// Remove ticket from the AgentAssignmentSolutionModel
								if (ticketToTransfer != null) {
									ticketList.remove(ticketToTransfer);

								}
								
							break;
							}
					}
				
					technicianDataRepo.save(TechnicianAssignment.getAgent());
					assignmentRepository.save(TechnicianAssignment);

				}		
	}

// need to deprecate this Method as all emergency will be handled by special assignment
	private void assignAfterHrsTicket(Ticket afterHrsticket, String token, String businessId) {
		BusinessContext.setTenantId(businessId);
		 BusinessTokenContext.setBusinessToken(token);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.FILE_DATETIME_FORMAT);
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DispatchControllerConstants.DATE_FORMAT);
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_TIME_FORMAT);
		try {
			ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE));
			
			List<Agent> agentUnFilteredList = technicianDataRepo.findBySupervisorIdAndIsActiveAndAgentType(afterHrsticket.getSupervisorId(),DispatchControllerConstants.FLAG_Y,DispatchControllerConstants.AGENT_TYPE_ACTUAL);
			
			List<Agent> agentList = new ArrayList<>();
			
			for(Agent agentUnFiltered : agentUnFilteredList) {
				try {					
					
					Sort sort = Sort.by(Sort.Order.desc( DispatchControllerConstants.AGENT_AVAILABILITY_CALENDAR_DATE));
					
					List<TechnicianWorkHour> technicianAvailabilitieList = technicianWorkHourRepository.findByCalendarDateAndTechEmpId(zonedDateTime.toLocalDate(),agentUnFiltered.getTechnicianId(),sort);
					
					if(!CollectionUtils.isEmpty(technicianAvailabilitieList)) {
						TechnicianWorkHour technicianWorkHour = technicianAvailabilitieList.get(0);
						log.info("Technician : {} Work hour found with Details : {}",agentUnFiltered.getTechnicianId(),technicianWorkHour);
						
						boolean isTechWorking = false;
						LocalDateTime afterHrStartTime = zonedDateTime.toLocalDateTime();
						LocalDateTime afterHrEndTime = zonedDateTime.toLocalDateTime();
						LocalDateTime thresholdTime = zonedDateTime.toLocalDateTime();
						try {
							afterHrStartTime = zonedDateTime.withHour(Integer.parseInt(technicianWorkHour.getOnCallStartDateTime().substring(0, 2))).withMinute(Integer.parseInt(technicianWorkHour.getOnCallStartDateTime().substring(3, 5))).toLocalDateTime();
							afterHrEndTime = zonedDateTime.withHour(Integer.parseInt(technicianWorkHour.getOnCallEndDateTime().substring(0, 2))).withMinute(Integer.parseInt(technicianWorkHour.getOnCallEndDateTime().substring(3, 5))).toLocalDateTime();
						
							if((zonedDateTime.toLocalDateTime().isEqual(afterHrStartTime) || zonedDateTime.toLocalDateTime().isAfter(afterHrStartTime)) 
									&& (zonedDateTime.toLocalDateTime().isEqual(afterHrEndTime) || zonedDateTime.toLocalDateTime().isBefore(afterHrEndTime))) {
								
								agentList.add(agentUnFiltered);
								
//								List<Ticket> afterHrsTicketList = agentUnFiltered.getTicketList().stream().filter(assignedTicket-> assignedTicket.getTicketPriority()==2 && StringUtils.equalsIgnoreCase(assignedTicket.getAfterHours(),DispatchControllerConstants.YES)).collect(Collectors.toList());
//								Location currentLocation = !CollectionUtils.isEmpty(afterHrsTicketList) ? afterHrsTicketList.get(afterHrsTicketList.size()-1).getLocation() : agentUnFiltered.getLocation();
//							
//								long travelTime = dispatchControllerSupportUtils.calculateDistance(currentLocation,afterHrsticket.getLocation()) / 60000;
//								long workHourTime = travelTime + afterHrsticket.getTicketETA();
//								
//								thresholdTime = zonedDateTime.toLocalDateTime().withMinute((int)workHourTime);
//								
//								if(thresholdTime.isEqual(afterHrEndTime) || thresholdTime.isBefore(afterHrEndTime)) {
//									agentList.add(agentUnFiltered);
//								}
							}
						
						}catch(Exception e) {
							log.info("Unable to Procress Technician : {} Work hour Details for After Hour Assignment",agentUnFiltered.getTechnicianId());
							isTechWorking = false;
						}
						
						if(isTechWorking) {
							agentList.add(agentUnFiltered);
						}
					}else {
						log.info("Technician : {} Work hour Not found",agentUnFiltered.getTechnicianId());
					}
				}catch(Exception e) {
					log.info("Unable to preprocess for Agent: {} for businessId : {} , due to {}",agentUnFiltered.getFirstName()+" "+agentUnFiltered.getLastName()+" : "+agentUnFiltered.getTechnicianId(),e.getMessage());
				}
			}
			
			if(!CollectionUtils.isEmpty(agentList)) {
				
				Agent afterHrsAgent = new Agent();
				
				afterHrsAgent = agentList.get(0);
				for(Agent agent : agentList){
					if(afterHrsAgent.getTicketList().size() > agent.getTicketList().size()) {
						afterHrsAgent = agent;
					}
				};
				
				//Set Ticket Details
				
				LocalDateTime localDateTimeNow = LocalDateTime.now();
				afterHrsticket.setGlobalStatus(DispatchControllerConstants.STATUS_ASSIGNED);
				afterHrsticket.setActionOnTicket(DispatchControllerConstants.STATUS_ASSIGNED);
				afterHrsticket.setTechnicianId(afterHrsAgent.getTechnicianId());
				afterHrsticket.setTechnicianFirstName(afterHrsAgent.getFirstName());
				afterHrsticket.setTechnicianLastName(afterHrsAgent.getLastName());
				afterHrsticket.setTechnicianEmailId(afterHrsAgent.getEmailId());
				afterHrsticket.setAssignmentDateTime(localDateTimeNow);
				TicketActionTrail ticketActionTrail = new TicketActionTrail();
				ticketActionTrail.setAction(DispatchControllerConstants.ACTION_ASSIGNMENT);
				ticketActionTrail.setActionBy(DispatchControllerConstants.KEYWORD_SYSTEM_AFTER_HRS_TICKET_ASSIGNMENT);
				ticketActionTrail.setActionOn(localDateTimeNow);
				ticketActionTrail.setPreAction(DispatchControllerConstants.STATUS_UNASSIGNED);
				ticketActionTrail.setPostAction(DispatchControllerConstants.STATUS_ASSIGNED + " To " + afterHrsAgent.getTechnicianId() + " : " + afterHrsAgent.getFirstName() + " " + afterHrsAgent.getLastName());
				afterHrsticket.getTicketActionTrails().add(ticketActionTrail);
				
			if(!StringUtils.equalsIgnoreCase(afterHrsticket.getIsAssistTicket(), DispatchControllerConstants.YES) ) {
				//Set Master Ticket
				LumenCollectionUpdateDTO lumenCollectionUpdateDTO = new LumenCollectionUpdateDTO();
				
				lumenCollectionUpdateDTO.setGlobalStatus(DispatchControllerConstants.STATUS_ASSIGNED);
				lumenCollectionUpdateDTO.setActionOnTicket(DispatchControllerConstants.STATUS_ASSIGNED);
				lumenCollectionUpdateDTO.setTechnicianId(afterHrsAgent.getTechnicianId());
				lumenCollectionUpdateDTO.setTechnicianFirstName(afterHrsAgent.getFirstName());
				lumenCollectionUpdateDTO.setTechnicianLastName(afterHrsAgent.getLastName());
				lumenCollectionUpdateDTO.setTechnicianEmailId(afterHrsAgent.getEmailId());
				lumenCollectionUpdateDTO.setSupervisorName(afterHrsticket.getSupervisorName());
				lumenCollectionUpdateDTO.setSupervisorId(afterHrsticket.getSupervisorId());
				lumenCollectionUpdateDTO.setAssignmentDateTime(localDateTimeNow);
				lumenCollectionUpdateDTO.setTicketActionTrails(ticketActionTrail);
				lumenCollectionUpdateDTO.setTicketNumber(afterHrsticket.getTicketNumber());
				lumenCollectionUpdateDTO.setConversationId(afterHrsticket.getConversationId());
				lumenCollectionUpdateDTO.setRemarks(DispatchControllerConstants.AUDIT_MESSAGE_AFTER_HRS_ASSIGNMENT);
				lumenCollectionUpdateDTO.setError("");
				
				ticketRepositoryImpl.updateLumenTicketCollection(lumenCollectionUpdateDTO);
				
				
		        String businessToken = BusinessTokenContext.getBusinessToken();
				try {
					executor.execute(()->{
						BusinessContext.setTenantId(businessId);
						BusinessTokenContext.setBusinessToken(businessToken);
						dataConvertorUtils.callNsAuditSave(lumenCollectionUpdateDTO,businessId,businessToken);
					});
					}
					catch(Exception e)
					{
						log.info("Unable to save Audit due to {} ", e.getMessage());
					}
				
			}
				ticketDataRepo.save(afterHrsticket);
				
				//Set Tech Details in DC
				
				afterHrsAgent.getTicketList().add(afterHrsticket);
				afterHrsAgent.setTotalWorkHourGlobal(0);
				
				Location previousLocation = afterHrsAgent.getLocation();
				long totalWorkHourGlobal = 0;
				double evaluatedDistanceLocal=0.0;
				for(Ticket ticketLocal : afterHrsAgent.getTicketList()){
					
					//long travelTime = dispatchControllerSupportUtils.calculateDistance(previousLocation,ticketLocal.getLocation()) / 60000;
					RouteSolverPath routeSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(previousLocation,ticketLocal.getLocation());
					long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath) ? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME : routeSolverPath.getTime();
					evaluatedDistanceLocal = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
					long travelTime = travelTimeLocal / 60000;
					
					long workHourTime = afterHrsAgent.getTotalWorkHourGlobal() 
							+ travelTime
							+ ticketLocal.getTicketETA();
					totalWorkHourGlobal += workHourTime;
					
					previousLocation = ticketLocal.getLocation();
					
				}
				afterHrsAgent.setTotalWorkHourGlobal(totalWorkHourGlobal);
				afterHrsAgent.setEvaluatedDistance(evaluatedDistanceLocal);
				
				String assignmentStatus = afterHrsAgent.getAvailableTime() > afterHrsAgent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED 
						: afterHrsAgent.getAvailableTime() == afterHrsAgent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED : DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;
				
				afterHrsAgent.setAssignmentStatus(assignmentStatus);
				
				technicianDataRepo.save(afterHrsAgent);
				
				LocalDateTime startDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
				LocalDateTime endDateTime =LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);

				long dcSolverProcessIdStart = Long.parseLong(startDateTime.format(dtf));
				long dcSolverProcessIdEnd = Long.parseLong(endDateTime.format(dtf));
				
				Pageable processPage = PageRequest.of(0, 1);
				
				Page<AgentAssignmentSolutionModel> agentAssignmentSolutionModelPageCurrent = assignmentRepository.findByDcSolverProcessIdBetween(dcSolverProcessIdStart,dcSolverProcessIdEnd,processPage);
				List<AgentAssignmentSolutionModel> agentAssignmentSolutionModelListCurrent = agentAssignmentSolutionModelPageCurrent.getContent();

				
				Page<AgentAssignmentSolutionModel> agentAssignmentSolutionModelPage = assignmentRepository.findByAgentTechnicianIdAndDcSolverProcessIdBetween(afterHrsAgent.getTechnicianId(),dcSolverProcessIdStart,dcSolverProcessIdEnd,processPage);
				List<AgentAssignmentSolutionModel> agentAssignmentSolutionModelList = agentAssignmentSolutionModelPage.getContent();

				if(!CollectionUtils.isEmpty(agentAssignmentSolutionModelList)) {
					AgentAssignmentSolutionModel agentAssignmentSolutionModel = agentAssignmentSolutionModelList.get(0);
					Agent solutionAgent = agentAssignmentSolutionModel.getAgent();
					solutionAgent.getTicketList().add(afterHrsticket);
					
					 previousLocation = solutionAgent.getLocation();
					 totalWorkHourGlobal = 0;
					double evaluatedDistance=0.0;
					
					for(Ticket ticketLocal : solutionAgent.getTicketList()){
						
						//long travelTime = dispatchControllerSupportUtils.calculateDistance(previousLocation,ticketLocal.getLocation()) / 60000;
						RouteSolverPath routeSolverPath = dispatchControllerSupportUtils.getLocationDistanceDetails(previousLocation,ticketLocal.getLocation());
						long travelTimeLocal = ObjectUtils.isEmpty(routeSolverPath) ? DispatchControllerConstants.DEFAULT_AGENT_TRAVEL_TIME : routeSolverPath.getTime();
						evaluatedDistance = ObjectUtils.isEmpty(routeSolverPath) ? 0.0 : routeSolverPath.getDistance();
						long travelTime = travelTimeLocal / 60000;
						
						long workHourTime = solutionAgent.getTotalWorkHourGlobal() 
								+ travelTime
								+ ticketLocal.getTicketETA();
						totalWorkHourGlobal += workHourTime;
						
						previousLocation = ticketLocal.getLocation();
						
					}
					solutionAgent.setTotalWorkHourGlobal(totalWorkHourGlobal);
					solutionAgent.setEvaluatedDistance(evaluatedDistance);
					
					 assignmentStatus = solutionAgent.getAvailableTime() > solutionAgent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED 
							: solutionAgent.getAvailableTime() == solutionAgent.getTotalWorkHourGlobal() ? DispatchControllerConstants.ASSIGNMENT_STATUS_IDEAL_ASSIGNED : DispatchControllerConstants.ASSIGNMENT_STATUS_OVER_ASSIGNED;
					
					solutionAgent.setAssignmentStatus(assignmentStatus);
					
					technicianDataRepo.save(solutionAgent);
					solutionAgent.setTotalWorkHourGlobal(totalWorkHourGlobal);
					agentAssignmentSolutionModel.setAgent(solutionAgent);
					assignmentRepository.save(agentAssignmentSolutionModel);
				}else {
					AgentAssignmentSolutionModel agentAssignmentSolutionModel = new AgentAssignmentSolutionModel();
					if(!agentAssignmentSolutionModelListCurrent.isEmpty()) {
						agentAssignmentSolutionModel.setDcSolverProcessId(agentAssignmentSolutionModelListCurrent.get(0).getDcSolverProcessId());
						agentAssignmentSolutionModel.setDcSolverTaskId(agentAssignmentSolutionModelListCurrent.get(0).getDcSolverTaskId());
					}else {
						agentAssignmentSolutionModel.setDcSolverProcessId(Long.parseLong(LocalDateTime.now().format(dtf)));
						agentAssignmentSolutionModel.setDcSolverTaskId(Long.parseLong(LocalDateTime.now().format(dtf)));
					}
					agentAssignmentSolutionModel.setTimestamp(LocalDateTime.now());
					
//					Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
//					
//					String afterHrsAgentString = gson.toJson(afterHrsAgent);
//					
//					Agent solutionAgent = gson.fromJson(afterHrsAgentString, Agent.class);
					
					agentAssignmentSolutionModel.setAgent(afterHrsAgent);
					assignmentRepository.save(agentAssignmentSolutionModel);
				}
				
				LumenRoboCallerBotRequest lumenRoboCallerBotRequest = new LumenRoboCallerBotRequest();
				LumenUser lumenUser = new LumenUser();
				LumenOrigin lumenOrigin = new LumenOrigin();
				
				SupervisorPolygonMapping supervisor = supervisorDataRepo.findBySupervisorId(afterHrsticket.getSupervisorId());
				
				lumenUser.setMessage("AfterHrs Ticket");
				lumenUser.setPhoneNumber(String.valueOf(dispatchControllerSupportUtils.getTenDigitRandomNumber(1000000000l, 9999999999l)));
				
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER,String.valueOf(afterHrsticket.getTicketNumber())));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("ticketConversationId",afterHrsticket.getConversationId()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.TECHNICIANID,afterHrsticket.getTechnicianId()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("technicianName",afterHrsticket.getTechnicianFirstName()+" "+afterHrsticket.getTechnicianLastName()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.SUPERVISORID,afterHrsticket.getSupervisorId()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.SUPERVISORNAME,afterHrsticket.getSupervisorName()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("technicianPhoneNumber",afterHrsAgent.getPhoneNumber()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("supervisorPhoneNumber",supervisor.getPhoneNumber()));
				lumenRoboCallerBotRequest.setUser(lumenUser);
				lumenRoboCallerBotRequest.setOrigin(lumenOrigin);
				
				executor.execute(()->{
					BusinessContext.setTenantId(businessId);
					BusinessTokenContext.setBusinessToken(token);
					dispatchControllerSupportUtils.callRoboCallerBot(lumenRoboCallerBotRequest, token,businessId);
				});
				
			}else {
				log.info("Unable to assign After Hrs ticket due to NO TECHNICIAN FOUND for ticket {}",afterHrsticket);
				LumenRoboCallerBotRequest lumenRoboCallerBotRequest = new LumenRoboCallerBotRequest();
				LumenUser lumenUser = new LumenUser();
				LumenOrigin lumenOrigin = new LumenOrigin();
				
				SupervisorPolygonMapping supervisor = supervisorDataRepo.findBySupervisorId(afterHrsticket.getSupervisorId());
				
				lumenUser.setMessage("AfterHrs Ticket");
				lumenUser.setPhoneNumber(String.valueOf(dispatchControllerSupportUtils.getTenDigitRandomNumber(1000000000l, 9999999999l)));
				
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.LUMEN_COLLECTION_TICKET_NUMBER,String.valueOf(afterHrsticket.getTicketNumber())));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("ticketConversationId",afterHrsticket.getConversationId()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.TECHNICIANID,""));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("technicianName",""));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.SUPERVISORID,afterHrsticket.getSupervisorId()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo(DispatchControllerConstants.SUPERVISORNAME,afterHrsticket.getSupervisorName()));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("technicianPhoneNumber",""));
				lumenRoboCallerBotRequest.getAdditionalInfoValues().add(new LumenAdditionalInfo("supervisorPhoneNumber",supervisor.getPhoneNumber()));
				lumenRoboCallerBotRequest.setUser(lumenUser);
				lumenRoboCallerBotRequest.setOrigin(lumenOrigin);
				
				executor.execute(()->{
					BusinessContext.setTenantId(businessId);
					BusinessTokenContext.setBusinessToken(token);
					dispatchControllerSupportUtils.callRoboCallerBot(lumenRoboCallerBotRequest, token,businessId);
				});
			}
		}catch(Exception e) {
		
			log.info("Unable to assign After Hrs ticket due to {} for ticket {}",e.getMessage(),afterHrsticket);
		}

	}

	private void updateTechnicianAndAssignmentSolutionIfCancelled(String ticketNumber,String technicianId) {
		
			 TicketNumbersDto ticketNumbersDto = new TicketNumbersDto();
			 ticketNumbersDto.setTicketNumber(ticketNumber);
			 ticketNumbersDto.setTechnicianId(technicianId);
			 ticketNumbersDto.setActionBy(DispatchControllerConstants.ENRICHMENT_BOT);
			 ResponseEntity<ApiResponseDto> responseEntity = assignBackToQueueRepoImpl.cancelByTicketNumber(ticketNumbersDto);
			 log.info(" ticket cancelled  {} ", responseEntity);
	}

	private void callEmergencyTicketAssignmentAPI(EmergencyTicketRequest emergencyTicketRequest,String token) {
		
         log.info("Calling emergencyTicketAssignment_url , url : {} , request : {}",emergencyTicketAssignment_url,emergencyTicketRequest.getEmergencyTicketList());
         try {
        	//Delay Added , need to make configurable 
        	 Thread.sleep(DispatchControllerConstants.DEFAULT_EMERGENCY_ASSIGNMENT_THREAD_SLEEP);
        	
			 RestTemplate rest = new RestTemplate();
             HttpHeaders headers = new HttpHeaders();
           
             headers.add("Authorization", "Bearer "+token+"");
             HttpEntity<?> request = new HttpEntity<>(emergencyTicketRequest, headers);
             ResponseEntity<Object> response = rest.exchange(emergencyTicketAssignment_url, HttpMethod.POST, request, Object.class);
            
             log.info("response of emergency Ticket Assignment url API  : " + response);
            
         }catch(Exception e) {
             log.info("Unable to Calling emergencyTicketAssignment_url , url : {}, due to : {}",emergencyTicketAssignment_url, e.getMessage());
            
         }
	}

	@Override
	public ResponseEntity<ResponseDTO> addTechnicianDetailsToDC(Agent requestAgent) {

		String logKey = DispatchControllerConstants.ADD_TECHNICIAN_DETAILS_TO_DC;
		String businessId = BusinessContext.getTenantId();

		try {
			
			 boolean isSupervisorPresent = supervisorDataRepo.existsBySupervisorId(requestAgent.getSupervisorId());
			 if(!isSupervisorPresent)
				{
					log.info("Supervisor not present");
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.RESPONSE_SUPERVISOR_NOT_FOUND,"", HttpStatus.OK);
					
				}
			 Map<String, Object> missingTechnicianDetails = MissingInfoTechnician(requestAgent);	
			 boolean isTechnicianDetailsMissing =false;
			 if (!CollectionUtils.isEmpty(missingTechnicianDetails))
				{
					log.info("MissingTechnician details ");
					isTechnicianDetailsMissing =true;
				 
				}
			
			 boolean isAgentPresent = technicianDataRepo.existsByTechnicianId(requestAgent.getTechnicianId());
				
			 
			Agent agent = new Agent();
			if(isAgentPresent) {
				//Update
				agent = technicianDataRepo.findByTechnicianId(requestAgent.getTechnicianId());
				updateTechnicianDetailsToDcTechnicianDetails(requestAgent,agent,isTechnicianDetailsMissing);
				
			}
			else
			{
			//TODO test .Is agent saving proper value
			//Insert
			agent = defaultValuesInsertForAgent(requestAgent);
			technicianDataRepo.save(requestAgent);
			}
			
			if(isTechnicianDetailsMissing)
			{
				log.info("retuen MissingTechnician details  ");
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.STATUS_MISSING_INFO,missingTechnicianDetails, HttpStatus.OK);
				
			}
			else
			{
				//Call createHirarchy 
			    log.info("Request received for Hierarchy with businessId: {}",businessId);
			    getCockpitService.generateTechnicianHierarchy();
				
				log.info("return submited technician details  ");
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
			}
			
		}catch(Exception e) {
			log.info("{} Unable to Save External technician Details, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR,e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}
	
	private Agent defaultValuesInsertForAgent(Agent agent) {
		// TODO Auto-generated method stub
		try {
			
		if(StringUtils.isEmpty(agent.getIsActive()))
		{
			agent.setIsActive(DispatchControllerConstants.FLAG_Y);
		}
		if(agent.getAvailableTime()<=0)
		{
			agent.setAvailableTime(DispatchControllerConstants.DEFAULT_AVAILABLETIME);
		}
		agent.setTotalWorkHourGlobal(0); //add compulsary as 0
		if(agent.getTechnicianScore()<=0)
		{
			agent.setTechnicianScore(1);
		}
		
		if(StringUtils.isEmpty(agent.getAutoAssignment()))
		{
			agent.setAutoAssignment(DispatchControllerConstants.YES);
		}
		
		return agent;
		
	}catch(Exception e) {
		log.info("Unable to Save External technician Details due to {}",e.getMessage());
		return agent;
	}
		
	}

	@Override
    public ResponseEntity<ApiResponseDto> getUnassignedTicketCounts() {
        ApiResponseDto apiResponseDto = new ApiResponseDto();
        Map<String, Object> response = new HashMap<>();
        log.info("{} total Unassigened Count");
        try {
                Query query = new Query();
                query.addCriteria(Criteria.where(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED));
                long count = 0;
                count =  mongoTemplate.count(query, DispatchControllerConstants.TICKET_COLLECTION);
                response.put(DispatchControllerConstants.STATUS_UNASSIGNED, count);
                apiResponseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
                apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
                apiResponseDto.setResponseData(response);
        } catch (Exception e) {
            apiResponseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
            apiResponseDto.setMessage("Unable to execute Query");
        }
        return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
    }
	
	 public void updateTicketDetailsToDcTicketDetails(ExternalTicketRequestDTO request, Ticket dcTicketModel, boolean isTicketDetailsMissing) {
		
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		 
		 Map<String,Object> pre=new HashMap<>();
		 Map<String,Object> post=new HashMap<>();
	
		 
		 if(!StringUtils.isEmpty(request.getTicketNumber811()) && !StringUtils.equalsIgnoreCase(request.getTicketNumber811(),dcTicketModel.getTicketNumber811())) {
				pre.put(DispatchControllerConstants.TICKETNUMBER_811, dcTicketModel.getTicketNumber811());
				dcTicketModel.setTicketNumber811(request.getPolygonId());
				post.put(DispatchControllerConstants.TICKETNUMBER_811, request.getTicketNumber811());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getPolygonId(),dcTicketModel.getPolygonId()) ) {
			pre.put(DispatchControllerConstants.POLYGON_ID, dcTicketModel.getPolygonId());
			dcTicketModel.setPolygonId(request.getPolygonId());
			post.put(DispatchControllerConstants.POLYGON_ID, request.getPolygonId());
		 }
		 
		 
		 if(request.getTicketETA() != dcTicketModel.getTicketETA()) {
		 pre.put(DispatchControllerConstants.TICKETETA, dcTicketModel.getTicketETA());
		 dcTicketModel.setTicketETA(request.getTicketETA());
		 post.put(DispatchControllerConstants.TICKETETA, request.getTicketETA());
		  }
		 
		 if(!request.getCertificationRequired().equals(dcTicketModel.getCertificationRequired())) {
			    pre.put(DispatchControllerConstants.CERTIFICATION_REQUIRED, dcTicketModel.getCertificationRequired());
				dcTicketModel.setCertificationRequired(request.getCertificationRequired());
				post.put(DispatchControllerConstants.CERTIFICATION_REQUIRED, request.getCertificationRequired());
				
		          }
		 
		 if(request.getTicketScore() != dcTicketModel.getTicketScore() ) {
				pre.put(DispatchControllerConstants.TICKETSCORE, dcTicketModel.getTicketScore());
				dcTicketModel.setTicketScore(request.getTicketScore());
				post.put(DispatchControllerConstants.TICKETSCORE, request.getTicketScore());
			 }
			try {  
				 
					if(!StringUtils.equalsIgnoreCase(request.getTicketDueDateAndTime(),dcTicketModel.getTicketDueDateAndTime().format(DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT)) )) {
						pre.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, dcTicketModel.getTicketDueDateAndTime());
						dcTicketModel.setTicketDueDateAndTime(LocalDateTime.parse(request.getTicketDueDateAndTime(), dtf));
						post.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, request.getTicketDueDateAndTime());
					}
					
					if(!StringUtils.equalsIgnoreCase(request.getCreatedDateTime(),dcTicketModel.getCreatedDateTime().format(DateTimeFormatter.ofPattern(DispatchControllerConstants.RESPONSE_DATETIME_FORMAT)) )) {
						pre.put(DispatchControllerConstants.CREATEDDATEANDTIME, dcTicketModel.getCreatedDateTime());
						dcTicketModel.setCreatedDateTime(LocalDateTime.parse(request.getCreatedDateTime(), dtf));
						post.put(DispatchControllerConstants.CREATEDDATEANDTIME, request.getCreatedDateTime());
					}
					
			}
			catch(Exception e)
			{
				 log.info("Unable to parse datetime"+e.getMessage());	
			}
			
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorId(),dcTicketModel.getSupervisorId())) {
				pre.put(DispatchControllerConstants.SUPERVISORID, dcTicketModel.getSupervisorId());
				dcTicketModel.setSupervisorId(request.getSupervisorId());
				post.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
			 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorName(),dcTicketModel.getSupervisorName())) {
				pre.put(DispatchControllerConstants.SUPERVISORNAME, dcTicketModel.getSupervisorName());
				dcTicketModel.setSupervisorName(StringUtils.capitalize(request.getSupervisorName()));
				post.put(DispatchControllerConstants.SUPERVISOR_NAME, request.getSupervisorName());
			 }

		 
		 if(!Objects.equals(request.getLocation().getLatitude(),dcTicketModel.getLocation().getLatitude()) || !Objects.equals(request.getLocation().getLongitude(),dcTicketModel.getLocation().getLongitude()) ) {
			 pre.put(DispatchControllerConstants.LOCATION, dcTicketModel.getLocation().toString());
			 dcTicketModel.setLocation(request.getLocation());
			 post.put(DispatchControllerConstants.LOCATION, request.getLocation().toString());
		     }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getTicketType(),dcTicketModel.getTicketType())) {
				pre.put(DispatchControllerConstants.TICKETTYPE, dcTicketModel.getTicketType());
				dcTicketModel.setTicketType(request.getTicketType());
				post.put(DispatchControllerConstants.TICKETTYPE, request.getTicketType());
			 }
		 
		 if(isTicketDetailsMissing)
			{	
			 pre.put(DispatchControllerConstants.TICKETSTATUS, dcTicketModel.getGlobalStatus());
			dcTicketModel.setGlobalStatus(DispatchControllerConstants.STATUS_MISSING_INFO);
			dcTicketModel.setIntialTicketStatus(DispatchControllerConstants.STATUS_MISSING_INFO);
			post.put(DispatchControllerConstants.TICKETSTATUS, request.getTicketStatus());
			}
			else if(!StringUtils.equalsIgnoreCase(request.getTicketStatus(),dcTicketModel.getGlobalStatus()))
			{   
				pre.put(DispatchControllerConstants.TICKETSTATUS, dcTicketModel.getGlobalStatus());	
				dcTicketModel.setGlobalStatus(request.getTicketStatus());
				dcTicketModel.setIntialTicketStatus(DispatchControllerConstants.WORD_ACTIVE);
				post.put(DispatchControllerConstants.TICKETSTATUS, request.getTicketStatus());
			}
			else
			{
				dcTicketModel.setIntialTicketStatus(DispatchControllerConstants.WORD_ACTIVE);
			}
		 
		 if(!StringUtils.equalsIgnoreCase(request.getWorkType(),dcTicketModel.getWorkType())) {
			 	pre.put(DispatchControllerConstants.WORKTYPE, dcTicketModel.getWorkType());
				dcTicketModel.setWorkType(request.getWorkType());
				post.put(DispatchControllerConstants.WORKTYPE, request.getWorkType());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getWorkCity(),dcTicketModel.getWorkCity())) {
			 	pre.put(DispatchControllerConstants.WORKCITY, dcTicketModel.getWorkCity());
				dcTicketModel.setWorkCity(StringUtils.capitalize(request.getWorkCity()));
				post.put(DispatchControllerConstants.WORKCITY, request.getWorkCity());
		 }
		 
		if(!StringUtils.equalsIgnoreCase(request.getWorkState(),dcTicketModel.getWorkState())) {
			 	pre.put(DispatchControllerConstants.WORKSTATE, dcTicketModel.getWorkState());
				dcTicketModel.setWorkState(StringUtils.upperCase(request.getWorkState()));
				post.put(DispatchControllerConstants.WORKSTATE, request.getWorkState());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getWorkCounty(),dcTicketModel.getWorkCounty())) {
			 	pre.put(DispatchControllerConstants.WORKCOUNTY, dcTicketModel.getWorkCounty());
				dcTicketModel.setWorkCounty(StringUtils.capitalize(request.getWorkCounty()));
				post.put(DispatchControllerConstants.WORKCOUNTY, request.getWorkCounty());
		 }
		 
	
		 if(!StringUtils.equalsIgnoreCase(request.getWorkStreet(),dcTicketModel.getWorkStreet())) {
			 	pre.put(DispatchControllerConstants.WORKSTREET, dcTicketModel.getWorkStreet());
				dcTicketModel.setWorkStreet(request.getWorkStreet());
				post.put(DispatchControllerConstants.WORKSTREET, request.getWorkStreet());
		 }
		 
		 
		 if(!StringUtils.equalsIgnoreCase(request.getWorkAddress(),dcTicketModel.getWorkAddress())) {
			 	pre.put(DispatchControllerConstants.WORKADDRESS, dcTicketModel.getWorkAddress());
				dcTicketModel.setWorkAddress(request.getWorkAddress());
				post.put(DispatchControllerConstants.WORKADDRESS, request.getWorkAddress());
		 }
		 

		 if(!StringUtils.equalsIgnoreCase(request.getWorkZip(),dcTicketModel.getWorkZip())) {
			 	pre.put(DispatchControllerConstants.WORKZIP, dcTicketModel.getWorkZip());
				dcTicketModel.setWorkZip(request.getWorkZip());
				post.put(DispatchControllerConstants.WORKZIP, request.getWorkZip());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getTechnicianId(),dcTicketModel.getTechnicianId())) {
			 	pre.put(DispatchControllerConstants.TECHNICIANID, dcTicketModel.getTechnicianId());
				dcTicketModel.setTechnicianId(request.getTechnicianId());
				post.put(DispatchControllerConstants.TECHNICIANID, request.getTechnicianId());
		 }
		
		 if(!StringUtils.equalsIgnoreCase(request.getTechnicianFirstName(),dcTicketModel.getTechnicianFirstName())) {
			 	pre.put(DispatchControllerConstants.TECHNICIAN_FIRSTNAME, dcTicketModel.getTechnicianFirstName());
				dcTicketModel.setTechnicianFirstName(StringUtils.capitalize(request.getTechnicianFirstName()));
				post.put(DispatchControllerConstants.TECHNICIAN_FIRSTNAME, request.getTechnicianFirstName());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getTechnicianLastName(),dcTicketModel.getTechnicianLastName())) {
			 	pre.put(DispatchControllerConstants.TECHNICIAN_LASTNAME, dcTicketModel.getTechnicianLastName());
				dcTicketModel.setTechnicianLastName(StringUtils.capitalize(request.getTechnicianLastName()));
				post.put(DispatchControllerConstants.TECHNICIAN_LASTNAME, request.getTechnicianLastName());
		 }
		
		 if(!StringUtils.equalsIgnoreCase(request.getTechnicianEmailId(),dcTicketModel.getTechnicianEmailId())) {
			 	pre.put(DispatchControllerConstants.TECHNICIAN_EMAILID, dcTicketModel.getTechnicianEmailId());
				dcTicketModel.setTechnicianEmailId(request.getTechnicianEmailId());
				post.put(DispatchControllerConstants.TECHNICIAN_EMAILID, request.getTechnicianEmailId());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorPolygonId(),dcTicketModel.getSupervisorPolygonId())) {
			 	pre.put(DispatchControllerConstants.SUPERVISORPOLYGONID, dcTicketModel.getSupervisorPolygonId());
				dcTicketModel.setSupervisorPolygonId(request.getSupervisorPolygonId());
				post.put(DispatchControllerConstants.SUPERVISORPOLYGONID, request.getSupervisorPolygonId());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getMasterTicketExternalId(),dcTicketModel.getMasterTicketExternalId())) {
			 	pre.put(DispatchControllerConstants.MASTER_TICKET_EXTERNALID, dcTicketModel.getMasterTicketExternalId());
				dcTicketModel.setMasterTicketExternalId(request.getMasterTicketExternalId());
				post.put(DispatchControllerConstants.MASTER_TICKET_EXTERNALID, request.getMasterTicketExternalId());
		 }
		 if((!request.getEmergencyFlag().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getEmergencyFlag(),dcTicketModel.getEmergencyFlag())) {
			 	pre.put(DispatchControllerConstants.EMERGENCY_FLAG, dcTicketModel.getEmergencyFlag());
				dcTicketModel.setEmergencyFlag(request.getEmergencyFlag());
				post.put(DispatchControllerConstants.EMERGENCY_FLAG, request.getEmergencyFlag());
		 }
		 if( (!request.getAfterHours().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getAfterHours(),dcTicketModel.getAfterHours())) {
			 	pre.put(DispatchControllerConstants.AFTER_HOURS, dcTicketModel.getAfterHours());
				dcTicketModel.setAfterHours(request.getAfterHours());
				post.put(DispatchControllerConstants.AFTER_HOURS, request.getAfterHours());
		 }
		 
		 if( (!request.getIsAssistTicket().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getIsAssistTicket(),dcTicketModel.getIsAssistTicket())) {
			 	pre.put(DispatchControllerConstants.IS_ASSIST_TICKET, dcTicketModel.getIsAssistTicket());
				dcTicketModel.setIsAssistTicket(request.getIsAssistTicket());
				post.put(DispatchControllerConstants.IS_ASSIST_TICKET, request.getIsAssistTicket());
		 }
		 if((!request.getMultiTechnicianTicket().isEmpty()) && !StringUtils.equalsIgnoreCase(request.getMultiTechnicianTicket(),dcTicketModel.getMultiTechnicianTicket()) ) {
			 	pre.put(DispatchControllerConstants.MULTI_TECHNICIAN_TICKET, dcTicketModel.getMultiTechnicianTicket());
				dcTicketModel.setMultiTechnicianTicket(request.getMultiTechnicianTicket());
				post.put(DispatchControllerConstants.MULTI_TECHNICIAN_TICKET, request.getMultiTechnicianTicket());
		 }
		 
		 //Commneted : Reason : First ticket will not be done with this api.(sendticketDetailsToDC)
			/*
			 * if((!request.getIsFirstTicket().isEmpty()) &&
			 * !StringUtils.equalsIgnoreCase(request.getIsFirstTicket(),dcTicketModel.
			 * getIsFirstTicket())) { pre.put(DispatchControllerConstants.IS_FIRST_TICKET,
			 * dcTicketModel.getIsFirstTicket());
			 * dcTicketModel.setIsFirstTicket(request.getIsFirstTicket());
			 * post.put(DispatchControllerConstants.IS_FIRST_TICKET,
			 * request.getIsFirstTicket()); }
			 */
		
		 Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
		 String preAction = gson.toJson(pre);
		
		 String postAction = gson.toJson(post);
		
		 log.info("postAction"+postAction);
		
		 TicketActionTrail ticketActionTrail = ticketActionTrail(DispatchControllerConstants.TICKET_UPDATED,DispatchControllerConstants.ENRICHMENT_BOT,preAction,postAction);
		 log.info(" update TicketActionTrail details 1 {} ", ticketActionTrail);
		 dcTicketModel.getTicketActionTrails().add(ticketActionTrail);
		
		//Update ticket data
		ticketDataRepo.save(dcTicketModel);
		
		//Save to NSAUDIT
		saveNsAudit(request,preAction,postAction);
		
			
			
		}
	 
	 private void updateTechnicianAndAssignmentSolution(Ticket dcTicketModel,String previousTicketStatus) {
		 if(previousTicketStatus.equals(DispatchControllerConstants.STATUS_ASSIGNED) || previousTicketStatus.equals(DispatchControllerConstants.STATUS_COMPLETED) )
			{
				// Find the tickets to transfer based on ticketNumber and fromtechnicianId
				AgentAssignmentSolutionModel TechnicianAssignment = assignmentRepository
						.findByAgentTicketListConversationIdAndAgentTicketListTicketNumber(dcTicketModel.getConversationId(), dcTicketModel.getTicketNumber()).findFirst().orElse(null);

				log.info(" TechnicianId {} ", TechnicianAssignment);
				if (TechnicianAssignment != null) {

					log.info("ticket in assignment solution {} ");
					// Find the ticket to be updated in the Technician's ticket list
					Ticket ticketToTransfer = null;
					List<Ticket> ticketList = TechnicianAssignment.getAgent().getTicketList();
					//TODO gte index and add ticket to same index
					log.info("List of tickets {} ",ticketList);
					for (int i = 0; i < ticketList.size(); i++) {
						Ticket ticket = ticketList.get(i);
							if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), dcTicketModel.getTicketNumber())) {
							ticketToTransfer = ticket;
							// Remove ticket from the AgentAssignmentSolutionModel
								if (ticketToTransfer != null) {
									ticketList.remove(ticketToTransfer);

								}
								//Add updated ticket to corresponding index
								ticketList.add(i, dcTicketModel);
							break;
							}
					}
				
					technicianDataRepo.save(TechnicianAssignment.getAgent());
					assignmentRepository.save(TechnicianAssignment);

				}		
			}
		
		
	}

	public TicketActionTrail ticketActionTrail(String action, String actionBy, String preAction,
				String postAction) {
			TicketActionTrail ticketActionTrail = new TicketActionTrail();

			log.info(" update TicketActionTrail details 1 {} ", ticketActionTrail);
			ticketActionTrail.setAction(action);
			ticketActionTrail.setActionBy(actionBy);
			ticketActionTrail.setActionOn(LocalDateTime.now());
			ticketActionTrail.setPreAction(preAction);
			ticketActionTrail.setPostAction(postAction);
			log.info(" ticketActionTrail 1 {} ", ticketActionTrail);
			return ticketActionTrail;
		}
	 
	 public Map<String, Object> MissingInfoTicket(ExternalTicketRequestDTO request)
	 {
		 Map<String,Object> pre=new HashMap<>();
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		 //Map<String,Object> post=new HashMap<>();
		 
					if(StringUtils.isEmpty(request.getConversationId()))
					{
					pre.put(DispatchControllerConstants.CONVERSATIONID, request.getTechnicianEmailId());
					}
					
					if(StringUtils.isEmpty(request.getTicketNumber()))
					{
					pre.put(DispatchControllerConstants.TICKETNUMBER, request.getTicketNumber());
					}
					/*
					 * if(StringUtils.isEmpty(request.getTicketNumber811())) {
					 * pre.put(DispatchControllerConstants.TICKETNUMBER_811,
					 * request.getTicketNumber811()); }
					 */
					
					/*if(StringUtils.isEmpty(request.getPolygonId()))
					{
					pre.put(DispatchControllerConstants.POLYGON_ID, request.getPolygonId());
					}*/
					
					if(request.getTicketETA()<=0)
					{
					pre.put(DispatchControllerConstants.TICKETETA, request.getTicketETA());
					}
					
					if (CollectionUtils.isEmpty(request.getCertificationRequired()))
					{
					pre.put(DispatchControllerConstants.CERTIFICATION_REQUIRED, request.getCertificationRequired());
					}
					
					if (request.getTicketScore()<=0)
					{
					pre.put(DispatchControllerConstants.TICKETSCORE, request.getTicketScore());
					}
					
					if (StringUtils.isEmpty(request.getTicketDueDateAndTime()))
					{
					pre.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, request.getTicketDueDateAndTime());
					}
					else
					{
						try {
							LocalDateTime.parse(request.getTicketDueDateAndTime(), dtf);
						}
						catch(Exception e) {
							pre.put(DispatchControllerConstants.TICKETDUEDATEANDTIME, e.getMessage());
						}
					}
					
					if (StringUtils.isEmpty(request.getSupervisorId()))
					{
					pre.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
					}
					else
					{
						boolean isSupervisorPresent = supervisorDataRepo.existsBySupervisorId(request.getSupervisorId());
						if(!isSupervisorPresent) {
							pre.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
						}
					}
					
					/*if (StringUtils.isEmpty(request.getSupervisorName()))
					{
					pre.put(DispatchControllerConstants.SUPERVISOR_NAME, request.getSupervisorName());
					}*/
					
					
					if (ObjectUtils.isEmpty(request.getLocation()))
					{
					pre.put(DispatchControllerConstants.LOCATION, request.getLocation());
					}
					
					if (request.getLocation().getLatitude()== 0)
					{
					pre.put(DispatchControllerConstants.LOCATION_LATITUDE, request.getLocation().getLatitude());
					}
					
					if (request.getLocation().getLongitude()== 0)
					{
					pre.put(DispatchControllerConstants.LOCATION_LONGITUDE, request.getLocation().getLongitude());
					}
					
					if (StringUtils.isEmpty(request.getTicketType()))
					{
					pre.put(DispatchControllerConstants.TICKETTYPE, request.getTicketType());
					}
					
					
					if (request.getTicketPriority()<=0)
					{
					pre.put(DispatchControllerConstants.TICKETPRIORITY, request.getTicketPriority());
					}
					
					if (StringUtils.isEmpty(request.getCreatedDateTime()))
					{
					pre.put(DispatchControllerConstants.CREATEDDATEANDTIME, request.getCreatedDateTime());
					}
					else
					{
						try {
							LocalDateTime.parse(request.getCreatedDateTime(), dtf);
						}
						catch(Exception e) {
							pre.put(DispatchControllerConstants.CREATEDDATEANDTIME, e.getMessage());
						}
					}
					
					if (StringUtils.isEmpty(request.getTicketStatus()))
					{
					pre.put(DispatchControllerConstants.TICKETSTATUS, request.getTicketStatus());
					}
					else if(request.getTicketStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_ASSIGNED))
					{
						boolean agentExist=technicianDataRepo.existsByTechnicianId(request.getTechnicianId());
						if(!agentExist)
						{
							pre.put(DispatchControllerConstants.TECHNICIANID , DispatchControllerConstants.STATUS_TECHNICIAN_NOT_AVAILABLE +" "+ request.getTechnicianId());
						}
					}
					if (StringUtils.isEmpty(request.getSupervisorPolygonId()))
					{
					pre.put(DispatchControllerConstants.SUPERVISORPOLYGONID, request.getSupervisorPolygonId());
					}
					if (StringUtils.isEmpty(request.getMasterTicketExternalId()))
					{
					pre.put(DispatchControllerConstants.MASTER_TICKET_EXTERNALID, request.getMasterTicketExternalId());
					}
				
					
								
					return pre;			
					
	 }
	 public Map<String, Object> MissingInfoTechnician(Agent request)
	 {
		 Map<String,Object> pre=new HashMap<>();
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		 
					if(StringUtils.isEmpty(request.getTechnicianId()))
					{
					pre.put(DispatchControllerConstants.TECHNICIANID, request.getTechnicianId());
					}
					if(StringUtils.isEmpty(request.getFirstName()))
					{
					pre.put(DispatchControllerConstants.FIELD_FIRSTNAME, request.getFirstName());
					}
					if(StringUtils.isEmpty(request.getLastName()))
					{
					pre.put(DispatchControllerConstants.FIELD_LASTNAME, request.getLastName());
					}
					if(StringUtils.isEmpty(request.getEmailId()))
					{
					pre.put(DispatchControllerConstants.FIELD_EMAILID, request.getEmailId());
					}
					if(StringUtils.isEmpty(request.getSupervisorName()))
					{
					pre.put(DispatchControllerConstants.SUPERVISOR_NAME, request.getSupervisorName());
					}
					if(StringUtils.isEmpty(request.getSupervisorId()))
					{
					pre.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
					}
					if (StringUtils.isEmpty(request.getPolygonId()))
					{
					pre.put(DispatchControllerConstants.POLYGON_ID, request.getPolygonId());
					}
					if(StringUtils.isEmpty(request.getJobTitle()))
					{
					pre.put(DispatchControllerConstants.FIELD_JOBTITLE, request.getJobTitle());
					}
					if (ObjectUtils.isEmpty(request.getLocation()))
					{
					pre.put(DispatchControllerConstants.LOCATION, request.getLocation());
					}
					
					if (request.getLocation().getLatitude()== 0)
					{
					pre.put(DispatchControllerConstants.LOCATION_LATITUDE, request.getLocation().getLatitude());
					}
					
					if (request.getLocation().getLongitude()== 0)
					{
					pre.put(DispatchControllerConstants.LOCATION_LONGITUDE, request.getLocation().getLongitude());
					}
					if(StringUtils.isEmpty(request.getCity()))
					{
					pre.put(DispatchControllerConstants.FIELD_CITY, request.getCity());
					}
					if(StringUtils.isEmpty(request.getCounty()))
					{
					pre.put(DispatchControllerConstants.FIELD_COUNTY, request.getCounty());
					}
					if(StringUtils.isEmpty(request.getState()))
					{
					pre.put(DispatchControllerConstants.FIELD_STATE, request.getState());
					}
					if(StringUtils.isEmpty(request.getVehicleType()))
					{
					pre.put(DispatchControllerConstants.FIELD_VEHICLETYPE, request.getVehicleType());
					}
					if(StringUtils.isEmpty(request.getAgentType()))
					{
					pre.put(DispatchControllerConstants.FIELD_AGENT_TYPE, request.getAgentType());
					}
					if (request.getTechnicianScore() <= 0)
					{
					pre.put(DispatchControllerConstants.FIELD_TECHNICIANSCORE, request.getTechnicianScore());
					}
					if (StringUtils.isEmpty(request.getIsActive()))
					{
					pre.put(DispatchControllerConstants.FIELD_ISACTIVE, request.getIsActive());
					}
					
					
		return pre;
	 }

	 public void updateTechnicianDetailsToDcTechnicianDetails(Agent request, Agent agent, boolean isTicketDetailsMissing) {
			
		 if(!StringUtils.equalsIgnoreCase(request.getFirstName(),agent.getFirstName())) {
			 
			 agent.setFirstName(StringUtils.capitalize(request.getFirstName()));
			
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getLastName(),agent.getLastName())) {
			 
			 agent.setLastName(StringUtils.capitalize(request.getLastName()));
			
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getEmailId(),agent.getEmailId())) {
			 
			 agent.setEmailId(request.getEmailId());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getCuid(),agent.getCuid())) {
			 
			 agent.setCuid(request.getCuid());

		 }
		 if(!StringUtils.equalsIgnoreCase(request.getAddress(),agent.getAddress())) {
			 
			 agent.setAddress(request.getAddress());

		 }
		 if(!StringUtils.equalsIgnoreCase(request.getIsActive(), agent.getIsActive())) {
			 
			 agent.setIsActive(request.getIsActive());

		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorId(),agent.getSupervisorId())) {
			 
			 agent.setSupervisorId(request.getSupervisorId());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorName(),agent.getSupervisorName())) {
			 
			 agent.setSupervisorName(request.getSupervisorName());
		 }
		 if(request.getTechnicianExplevel() != agent.getTechnicianExplevel() ) {
				
			 agent.setTechnicianExplevel(request.getTechnicianExplevel());
			
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getTechnicianAvailability(),agent.getTechnicianAvailability())) {
			 
			 agent.setTechnicianAvailability(request.getTechnicianAvailability());
		 }
		 if(request.getPolygonId() != agent.getPolygonId() ) {
			
			 agent.setPolygonId(request.getPolygonId());
			
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getTenure(),agent.getTenure())) {
			 
			 agent.setTenure(request.getTenure());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getPhoneNumber(),agent.getPhoneNumber())) {
			 
			 agent.setPhoneNumber(request.getPhoneNumber());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getJobTitle(),agent.getJobTitle())) {
			 
			 agent.setJobTitle(request.getJobTitle());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getJobTitle(),agent.getJobTitle())) {
			 
			 agent.setJobTitle(request.getJobTitle());
		 }
		 if(!Objects.equals(request.getLocation().getLatitude(),agent.getLocation().getLatitude()) || !Objects.equals(request.getLocation().getLongitude(),agent.getLocation().getLongitude()) ) {
			
			 agent.setLocation(request.getLocation());
			 
		  }
		 if(request.getAvailableTime()!=agent.getAvailableTime()) {
			 
			 agent.setAvailableTime(request.getAvailableTime());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getCity(),agent.getCity())) {
			 
			 agent.setCity(request.getCity());
		 }
		
		 if(!StringUtils.equalsIgnoreCase(request.getCounty(),agent.getCounty())) {
	 
			 agent.setCounty(request.getCounty());
		 }

		 if(!StringUtils.equalsIgnoreCase(request.getState(),agent.getState())) {
	 
			 agent.setState(request.getState());
		 }

		 if(!StringUtils.equalsIgnoreCase(request.getVehicleType(),agent.getVehicleType())) {
	 
			 agent.setVehicleType(request.getVehicleType());
		 }
		
		 
		/* if(!StringUtils.equalsIgnoreCase(request.getAgentType(),agent.getAgentType())) {
			 
			 agent.setAgentType(request.getAgentType());
		 }*/
		 if(request.getTechnicianScore()!=agent.getTechnicianScore()) {
			 
			 agent.setTechnicianScore(request.getTechnicianScore());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getAvailabilityStatus(),agent.getAvailabilityStatus())) {
			 
			 agent.setAgentType(request.getAgentType());
		 }
		 if (ObjectUtils.notEqual(request.getCertificationList(), agent.getCertificationList())) {
			 agent.setCertificationList(request.getCertificationList());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getAutoAssignment(),agent.getAutoAssignment())) {
			 
			 agent.setAutoAssignment(request.getAutoAssignment());
		 }
		log.info(" update technician details  {} ", agent);
		
		
		//Update technician data
		 technicianDataRepo.save(agent);
			
		}

	@Override
	public ResponseEntity<ResponseDTO> addSupervisorDetailsToDC(SupervisorPolygonMapping request) {
		String logKey = DispatchControllerConstants.ADD_SUPERVISOR_DETAILS_TO_DC;
		String businessId = BusinessContext.getTenantId();
		log.info("log key "+logKey);
		try {
			 Map<String, Object> missingSupervisorDetails = MissingInfoSupervisor(request);	
			 boolean isSupervisorDetailsMissing =false;
			 if (!CollectionUtils.isEmpty(missingSupervisorDetails))
				{
					log.info("MissingSupervisor details ");
					isSupervisorDetailsMissing =true;
				 
				}
			
			 boolean isSupervisorPresent = supervisorDataRepo.existsBySupervisorId(request.getSupervisorId());
			 
			 SupervisorPolygonMapping supervisor = new SupervisorPolygonMapping();
			if(isSupervisorPresent) {
				//Update
				supervisor = supervisorDataRepo.findBySupervisorId(request.getSupervisorId());
				updateSupervisorDetailsToDcSupervisorDetails(request,supervisor);
				
			}
			else
			{
			//Insert
			supervisor = defaultValuesInsertForSupervisor(request);
			supervisorDataRepo.save(supervisor);
			}
			
			if(isSupervisorDetailsMissing)
			{
				log.info("retuen MissingSupervisor details  ");
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.STATUS_MISSING_INFO,missingSupervisorDetails, HttpStatus.OK);
				
			}
			else
			{
				//Call createHirarchy 
			    log.info("Request received for Hierarchy with businessId: {}",businessId);
			    getCockpitService.generateTechnicianHierarchy();
				
			    log.info("return submited Supervisor details  ");
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
			}
			
		}catch(Exception e) {
			log.info("{} Unable to Save External Supervisor Details, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR,e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	

	private SupervisorPolygonMapping defaultValuesInsertForSupervisor(SupervisorPolygonMapping request) {
		try {
			
			if(StringUtils.isEmpty(request.getIsActive()))
			{
				request.setIsActive(DispatchControllerConstants.FLAG_Y);
			}
			if(StringUtils.isEmpty(request.getAutoAssignment()))
			{
				request.setAutoAssignment(DispatchControllerConstants.YES);
			}
			
			
			//Add Virtual Agent to Supervisor
			
			Agent virtualAgent = new Agent();
			
			virtualAgent.setAddress(request.getCity());
			virtualAgent.setAgentType(DispatchControllerConstants.AGENT_TYPE_VIRTUAL);
			virtualAgent.setAssignmentStatus(DispatchControllerConstants.ASSIGNMENT_STATUS_UNDER_ASSIGNED);
			virtualAgent.setAvailabilityStatus(DispatchControllerConstants.STATUS_AVAILABLE);
			virtualAgent.setAvailableTime(600);
			List<String> certificationList = new ArrayList<>();
			certificationList.add("No");
			virtualAgent.setCertificationList(certificationList);
			virtualAgent.setCity(request.getCity());
			virtualAgent.setCounty(request.getCounty());
			virtualAgent.setCuid(request.getSupervisorId());
			virtualAgent.setDefaultAvailableTime(600);
			virtualAgent.setEmailId(request.getEmailId());
			virtualAgent.setFirstName(request.getFirstName()+DispatchControllerConstants.AGENT_TYPE_VIRTUAL);
			
			virtualAgent.setLastName(request.getLastName()+DispatchControllerConstants.AGENT_TYPE_VIRTUAL);
			virtualAgent.setIsActive(DispatchControllerConstants.FLAG_Y);
			virtualAgent.setJobTitle("Technician");
			virtualAgent.setLocation(request.getLocation());
			virtualAgent.setPhoneNumber(request.getPhoneNumber());
			virtualAgent.setPolygonId(request.getSupervisorPolygonId());
			virtualAgent.setSkills("All");
			virtualAgent.setState(request.getState());
			virtualAgent.setSupervisorId(request.getSupervisorId());
			virtualAgent.setSupervisorName(request.getFirstName()+" "+request.getLastName());
			virtualAgent.setTechnicianAvailability(DispatchControllerConstants.STATUS_AVAILABLE);
			virtualAgent.setTechnicianExplevel(1);
			virtualAgent.setTechnicianId("V"+request.getSupervisorId());
			virtualAgent.setTenure("1");
			virtualAgent.setVehicleType("car");
			virtualAgent.setAutoAssignment(DispatchControllerConstants.YES);
			
			technicianDataRepo.save(virtualAgent);
			
			return request;
			
		}catch(Exception e) {
			log.info("Unable to Save External supervisor Details due to {}",e.getMessage());
			return request;
		}
	}

	private void updateSupervisorDetailsToDcSupervisorDetails(SupervisorPolygonMapping supervisorRequest,
			SupervisorPolygonMapping supervisor) {
		
		 Agent virtualAgent = technicianDataRepo.findByTechnicianIdAndAgentType("V"+supervisor.getSupervisorId(), DispatchControllerConstants.AGENT_TYPE_VIRTUAL);
				
		 if(ObjectUtils.notEqual(supervisorRequest.getPolygonList(), supervisor.getPolygonList())) {
			 
			 supervisor.setPolygonList(supervisorRequest.getPolygonList());

		 }
		 
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getFirstName(), supervisor.getFirstName())) {
			 
			 supervisor.setFirstName(supervisorRequest.getFirstName());
			 
				virtualAgent.setFirstName(supervisorRequest.getFirstName()+DispatchControllerConstants.AGENT_TYPE_VIRTUAL);
			
		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getLastName(), supervisor.getLastName())) {
			 
			 supervisor.setLastName(supervisorRequest.getLastName());
			 virtualAgent.setLastName(supervisorRequest.getLastName()+DispatchControllerConstants.AGENT_TYPE_VIRTUAL);

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getEmailId(), supervisor.getEmailId())) {
			 
			 supervisor.setEmailId(supervisorRequest.getEmailId());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getIsActive(), supervisor.getIsActive())) {
			 
			 supervisor.setIsActive(supervisorRequest.getIsActive());
			 virtualAgent.setIsActive(supervisorRequest.getIsActive());
			 

		 }
		 
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getManagerId(), supervisor.getManagerId())) {
			 
			 supervisor.setManagerId(supervisorRequest.getManagerId());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getManagerName(), supervisor.getManagerName())) {
			 
			 supervisor.setManagerName(supervisorRequest.getManagerName());

		 }
		 if(supervisorRequest.getSupervisorExplevel()!=supervisor.getSupervisorExplevel()) {
			 
			 supervisor.setSupervisorExplevel(supervisorRequest.getSupervisorExplevel());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getSupervisorAvailability(), supervisor.getSupervisorAvailability())) {
			 
			 supervisor.setSupervisorAvailability(supervisorRequest.getSupervisorAvailability());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getPhoneNumber(), supervisor.getPhoneNumber())) {
			 
			 supervisor.setPhoneNumber(supervisorRequest.getPhoneNumber());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getJobTitle(), supervisor.getJobTitle())) {
			 
			 supervisor.setJobTitle(supervisorRequest.getJobTitle());

		 }
		 if(!Objects.equals(supervisorRequest.getLocation(), supervisor.getLocation())) {
			 
			 supervisor.setLocation(supervisorRequest.getLocation());
			 virtualAgent.setLocation(supervisorRequest.getLocation());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getCity(), supervisor.getCity())) {
			 
			 supervisor.setCity(supervisorRequest.getCity());
			 virtualAgent.setCity(supervisorRequest.getCity());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getCounty(), supervisor.getCounty())) {
			 
			 supervisor.setCounty(supervisorRequest.getCounty());
			 virtualAgent.setCounty(supervisorRequest.getCounty());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getState(), supervisor.getState())) {
			 
			 supervisor.setState(supervisorRequest.getState());
			 virtualAgent.setState(supervisorRequest.getState());

		 }
		 if(!StringUtils.equalsIgnoreCase(supervisorRequest.getAvailabilityStatus(), supervisor.getAvailabilityStatus())) {
			 
			 supervisor.setAvailabilityStatus(supervisorRequest.getAvailabilityStatus());

		 }
		
		
		 
		 
		log.info(" update supervisor details  {} ", supervisor);
		
		
		//Update supervisor data
		 supervisorDataRepo.save(supervisor);
		//update virtual technician 
		technicianDataRepo.save(virtualAgent);
			
		
	}

	private Map<String, Object> MissingInfoSupervisor(SupervisorPolygonMapping request) {
		 Map<String,Object> pre=new HashMap<>();
		 
			if(StringUtils.isEmpty(request.getSupervisorId()))
			{
			pre.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
			}
				if (StringUtils.isEmpty(request.getSupervisorPolygonId()))
			{
			pre.put(DispatchControllerConstants.FIELD_SUPERVISORPOLYGONID, request.getSupervisorPolygonId());
			}
				
			//	Commented : need extra handling 17 jul 2023
//			if (CollectionUtils.isEmpty(request.getPolygonList()))
//			{
//			pre.put(DispatchControllerConstants.FIELD_POLYGONLIST, request.getPolygonList());
//			}
			
			return pre;		
	}
	@Override
	public ResponseEntity<ApiResponseDto> updateByTicketStatus(List<UpdateTicketNumbersDto> ticketNumbersDto) {
			Ticket ticketModel = new Ticket();
			String preGlobalStatus ;
			GroupByActionResponse groupByActionResponse = new GroupByActionResponse();
			ApiResponseDto apiResponseDto = new ApiResponseDto();
	try {
		 for (UpdateTicketNumbersDto ticketNumber : ticketNumbersDto) 
		 {
			 	boolean isTicketPresent = ticketDataRepo.existsByTicketNumber(ticketNumber.getTicketNumber());
				log.info("isTicketPresent{}",isTicketPresent);
			
				if(isTicketPresent) {
					
					ticketModel = ticketDataRepo.findByTicketNumber(ticketNumber.getTicketNumber());
					preGlobalStatus=ticketModel.getGlobalStatus();
					ticketModel.setGlobalStatus(ticketNumber.getTicketStatus());
					ticketModel.setActionOnTicket(ticketNumber.getTicketStatus());
					ticketModel.setActualTimeSpent(ticketNumber.getActualTimeSpent());
					
					if(!StringUtils.equalsIgnoreCase(ticketNumber.getTicketStatus(),DispatchControllerConstants.STATUS_RESCHEDULE))
					{
						ticketModel.setCompletionDateTime(LocalDateTime.now());
					}
			
					//Maintain actionTrail
					 TicketActionTrail ticketActionTrail = ticketActionTrailForUpdate(DispatchControllerConstants.TICKET_UPDATED,ticketNumber.getActionBy(),preGlobalStatus,ticketModel.getGlobalStatus());
					 log.info(" update TicketActionTrail details 1 {} ", ticketActionTrail);
					 ticketModel.getTicketActionTrails().add(ticketActionTrail);
					
					 //Update in ticketDetails
					 ticketDataRepo.save(ticketModel);
					updateTechnicianAndAssignment(ticketModel);
					groupByActionResponse.getResponse().put(String.valueOf(ticketNumber.getTicketNumber()), DispatchControllerConstants.STATUS_SUCCESS);
					
				}
				else
				{
					groupByActionResponse.getResponse().put(String.valueOf(ticketNumber.getTicketNumber()), DispatchControllerConstants.TICKET_NOT_AVAILABLE);
				}
		   
		 }
		 	apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
			apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
			apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			
			
				return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
		

		} catch (Exception e) {
			log.info("Unable to updateTicket");
			groupByActionResponse.getResponse().put(DispatchControllerConstants.STATUS_FAILED,DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(groupByActionResponse.getResponse());
			return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
	}

	private void updateTechnicianAndAssignment(Ticket ticketModel) {
		// Find the tickets to transfer based on ticketNumber and fromtechnicianId
		AgentAssignmentSolutionModel TechnicianAssignment = assignmentRepository
				.findByAgentTicketListConversationIdAndAgentTicketListTicketNumber(ticketModel.getConversationId(), ticketModel.getTicketNumber()).findFirst().orElse(null);

		log.info(" TechnicianId {} ", TechnicianAssignment);
		if (TechnicianAssignment != null) {

			log.info("ticket in assignment solution {} ");
			// Find the ticket to be updated in the Technician's ticket list
			Ticket ticketToupdate = null;
			List<Ticket> ticketList = TechnicianAssignment.getAgent().getTicketList();
			//TODO gte index and add ticket to same index
			log.info("List of tickets {} ",ticketList);
			for (int i = 0; i < ticketList.size(); i++) {
				Ticket ticket = ticketList.get(i);
					if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketModel.getTicketNumber())) {
						ticketToupdate = ticket;
					// Remove ticket from the AgentAssignmentSolutionModel
						if (ticketToupdate != null) {
							ticketList.remove(ticketToupdate);

						}
						//Add updated ticket to corresponding index
						ticketList.add(i, ticketModel);
					break;
					}
			}
		
			//reduce remaining work time after ticket complete in agent work hours
			TechnicianAssignment.getAgent().setRemainingWorkTime(TechnicianAssignment.getAgent().getRemainingWorkTime() - ticketModel.getActualTimeSpent());
			
			technicianDataRepo.save(TechnicianAssignment.getAgent());
			
			assignmentRepository.save(TechnicianAssignment);

		}		
	}
	private void updateTechnicianAndAssignmentForFirstTicketUpdate(Ticket ticketModel) {
		// Find the tickets to transfer based on ticketNumber and fromtechnicianId
		AgentAssignmentSolutionModel TechnicianAssignment = assignmentRepository
				.findByAgentTicketListConversationIdAndAgentTicketListTicketNumber(ticketModel.getConversationId(), ticketModel.getTicketNumber()).findFirst().orElse(null);

		log.info(" TechnicianId {} ", TechnicianAssignment);
		if (TechnicianAssignment != null) {

			log.info("ticket in assignment solution {} ");
			// Find the ticket to be updated in the Technician's ticket list
			Ticket ticketToupdate = null;
			List<Ticket> ticketList = TechnicianAssignment.getAgent().getTicketList();
			//TODO gte index and add ticket to same index
			log.info("List of tickets {} ",ticketList);
			for (int i = 0; i < ticketList.size(); i++) {
				Ticket ticket = ticketList.get(i);
					if (StringUtils.equalsIgnoreCase(ticket.getTicketNumber(), ticketModel.getTicketNumber())) {
						ticketToupdate = ticket;
					// Remove ticket from the AgentAssignmentSolutionModel
						if (ticketToupdate != null) {
							ticketList.remove(ticketToupdate);

						}
						//Add updated ticket to corresponding index
						ticketList.add(i, ticketModel);
					break;
					}
			}
			
			technicianDataRepo.save(TechnicianAssignment.getAgent());
			
			assignmentRepository.save(TechnicianAssignment);

		}		
	}
	public TicketActionTrail ticketActionTrailForUpdate(String action, String actionBy, String preAction,
			String postAction) {
		TicketActionTrail ticketActionTrail = new TicketActionTrail();

		log.info(" update TicketActionTrail details 1 {} ", ticketActionTrail);
		ticketActionTrail.setAction(action);
		ticketActionTrail.setActionBy(actionBy);
		ticketActionTrail.setActionOn(LocalDateTime.now());
		ticketActionTrail.setPreAction(preAction);
		ticketActionTrail.setPostAction(postAction);
		log.info(" ticketActionTrail 1 {} ", ticketActionTrail);
		return ticketActionTrail;
	}

	@Override
	public ResponseEntity<ApiResponseDto> callTechnician(CallTechnicianDto callTechnicianDto) {
		
		ApiResponseDto apiResponseDto=new ApiResponseDto();   
		try {

			if(callTechnicianDto != null) {

				RestTemplate restTemplate = new RestTemplate();
				HttpHeaders headers = new HttpHeaders();
				headers.add("Authorization", "Bearer " + BusinessTokenContext.getBusinessToken());
				headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);


				MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

				parameters.add("message", callTechnicianDto.getMessage());
				parameters.add("numbers",callTechnicianDto.getPhoneNumber());
				parameters.add("callType", DispatchControllerConstants.CALL_TYPE);
				parameters.add("sourceApp", DispatchControllerConstants.SOURCE_APP);

				HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);
				restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

				ResponseEntity<String> response = restTemplate.exchange(service11callAPIUrl, HttpMethod.POST, requestEntity, String.class);
				log.info("## BUSINESS ID: " + BusinessContext.getTenantId() + "## ** Service11 API Response :  "
						+ response.getBody().toString());

				apiResponseDto.setMessage("Success");
				apiResponseDto.setResponseData(response.getBody());
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);

			}
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);   
		}
		catch(Exception e){
		
			log.info("Unable to get service11 response due to : {} " ,e.getMessage());
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@Override
	public ResponseEntity<ExternalAppResponse> myTeamWorkload(MyTeamWorkloadRequest myTeamWorkloadRequest) {
		ExternalAppResponse externalAppResponse = new ExternalAppResponse();
		MyTeamWorkloadResponse myTeamWorkloadResponse = new MyTeamWorkloadResponse();
		MyTeamWorkloadSupervisorDetails supervisorDetail = new MyTeamWorkloadSupervisorDetails();
		List<MyTeamWorkloadTechnicianDetails> techList = new ArrayList<>();
		String logKey = DispatchControllerConstants.MY_TEAM_WORKLOAD;
		String businessId = BusinessContext.getTenantId();
		 long totalAvailableMins = 0;
		 long totalOpenTickets = 0;
		 long completedTickets = 0;
		
		if(ObjectUtils.isEmpty(myTeamWorkloadRequest) 
				|| StringUtils.isEmpty(myTeamWorkloadRequest.getSupervisorId())
				|| StringUtils.isEmpty(myTeamWorkloadRequest.getStartDateTime())
				|| StringUtils.isEmpty(myTeamWorkloadRequest.getEndDateTime())) {
			
			externalAppResponse.setError(DispatchControllerConstants.APP_STATUS_TRUE);
			externalAppResponse.setErrorMessage(DispatchControllerConstants.RESPONSE_INCOMPLETE_REQUEST);
			externalAppResponse.setData(myTeamWorkloadResponse);
			log.info("{} unable to process due to Incomplete request {} for businessId {}",logKey,myTeamWorkloadRequest,businessId);
			 return new ResponseEntity<>(externalAppResponse, HttpStatus.OK);  
		}
		
		LocalDateTime startDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
		LocalDateTime endDate  = LocalDateTime.now().plusDays(8).withHour(0).withMinute(0).withSecond(0);
		try {
			 DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
	          startDate = LocalDateTime.parse(myTeamWorkloadRequest.getStartDateTime(), formatter);
	           endDate = LocalDateTime.parse(myTeamWorkloadRequest.getEndDateTime(), formatter);
	          
		}catch(Exception e) {
			externalAppResponse.setError(DispatchControllerConstants.APP_STATUS_TRUE);
			externalAppResponse.setErrorMessage(e.getMessage());
			externalAppResponse.setData(myTeamWorkloadResponse);
			log.info("{} unable to process request {} for businessId {} , due to {}",logKey,myTeamWorkloadRequest,businessId,e.getMessage());
			 return new ResponseEntity<>(externalAppResponse, HttpStatus.OK);  
		}
		
		try {
			log.debug("{} Supervisor check start in db: {} and businessId: {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,myTeamWorkloadRequest.getSupervisorId(),businessId);
			
			SupervisorPolygonMapping supervisor = supervisorDataRepo.findBySupervisorId(myTeamWorkloadRequest.getSupervisorId());
			log.debug("{} Supervisor check end in db: {} and businessId: {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,myTeamWorkloadRequest.getSupervisorId(),businessId);
			
			if(ObjectUtils.isEmpty(supervisor)) {

				externalAppResponse.setError(DispatchControllerConstants.APP_STATUS_TRUE);
				externalAppResponse.setErrorMessage(DispatchControllerConstants.RESPONSE_SUPERVISOR_NOT_FOUND);
				externalAppResponse.setData(myTeamWorkloadResponse);
				log.info("{} unable to process due to Supervisor Not Found request {} for businessId {}",logKey,myTeamWorkloadRequest,businessId);
				return new ResponseEntity<>(externalAppResponse, HttpStatus.OK);  
			}
			log.debug("{} Fetch technicianList for Supervisor : start in db: {} and businessId: {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,myTeamWorkloadRequest.getSupervisorId(),businessId);
			
			List<Agent> technicianList = technicianDataRepo.findByIsActiveAndAgentTypeAndSupervisorId(DispatchControllerConstants.FLAG_Y, DispatchControllerConstants.AGENT_TYPE_ACTUAL, myTeamWorkloadRequest.getSupervisorId());
			log.debug("{} Fetch technicianList for Supervisor : end in db: {} and businessId: {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,myTeamWorkloadRequest.getSupervisorId(),businessId);
			
			if(!CollectionUtils.isEmpty(technicianList)) {

				for(Agent technician : technicianList){
					MyTeamWorkloadTechnicianDetails myTeamWorkloadTechnicianDetails = new MyTeamWorkloadTechnicianDetails();
					log.debug("{} Fetch myTeamWorkloadTechnicianDetails for technician : start in db: {} and businessId: {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,technician,businessId);
					myTeamWorkloadTechnicianDetails = agentRepository.getTechnicianTicketCount(technician, startDate, endDate);
					log.debug("{} Fetch myTeamWorkloadTechnicianDetails for technician : end in db: {} and businessId: {} ",DispatchControllerConstants.MY_TEAM_WORKLOAD,technician,businessId);
					
					
					if(StringUtils.equalsIgnoreCase(myTeamWorkloadTechnicianDetails.getTechId(),myTeamWorkloadTechnicianDetails.getSupervisorId())) {
						supervisorDetail.setOpen(myTeamWorkloadTechnicianDetails.getOpen());
						supervisorDetail.setComplete(myTeamWorkloadTechnicianDetails.getComplete());
						supervisorDetail.setAvailableMin( myTeamWorkloadTechnicianDetails.getAvailableMins());
						System.out.println(" myTeamWorkloadTechnicianDetails : " + myTeamWorkloadTechnicianDetails);
					}else {
					techList.add(myTeamWorkloadTechnicianDetails);
					completedTickets += myTeamWorkloadTechnicianDetails.getComplete();
					totalOpenTickets += myTeamWorkloadTechnicianDetails.getOpen();
					totalAvailableMins += myTeamWorkloadTechnicianDetails.getAvailableMins();
					}
				};
			}

			supervisorDetail.setSupervisorId(supervisor.getSupervisorId());
			supervisorDetail.setSupervisorName(supervisor.getFirstName() + " " + supervisor.getLastName());

			// Comment following three line : TE10036395
			//			supervisorDetail.setAvailableMin(totalAvailableMins);
//			supervisorDetail.setComplete(completedTickets);
//			supervisorDetail.setOpen(totalOpenTickets);

			myTeamWorkloadResponse.setSupervisorDetail(supervisorDetail);
			myTeamWorkloadResponse.setCompletedTickets(completedTickets);
			myTeamWorkloadResponse.setTechList(techList);
			myTeamWorkloadResponse.setTotalAvailableMins(totalAvailableMins);
			myTeamWorkloadResponse.setTotalOpenTickets(totalOpenTickets);

			externalAppResponse.setError(DispatchControllerConstants.APP_STATUS_FALSE);
			externalAppResponse.setErrorMessage(DispatchControllerConstants.STATUS_SUCCESS);
			externalAppResponse.setData(myTeamWorkloadResponse);

			return new ResponseEntity<>(externalAppResponse, HttpStatus.OK);   
		}catch(Exception e) {
			externalAppResponse.setError(DispatchControllerConstants.APP_STATUS_TRUE);
			externalAppResponse.setErrorMessage(e.getMessage());
			externalAppResponse.setData(myTeamWorkloadResponse);
			log.info("{} unable to process request {} for businessId {} , due to {}",logKey,myTeamWorkloadRequest,businessId,e.getMessage());
			 return new ResponseEntity<>(externalAppResponse, HttpStatus.INTERNAL_SERVER_ERROR);  
		}
	}


	@Override
	public ResponseEntity<ApiResponseDto> firstTicketUpdate(firstTicketNumberDto ticketNumberDto) {
		Ticket ticketModel = new Ticket();
		String preStatus ;
		ApiResponseDto apiResponseDto = new ApiResponseDto();
		String ticketNumber=ticketNumberDto.getTicketExternalId();
		try {
			
		 	boolean isTicketPresent = ticketDataRepo.existsByTicketNumber(ticketNumber,ticketNumberDto.getConversationId());
			log.info("isTicketPresent{}",isTicketPresent);
		
			if(isTicketPresent) {
				
				ticketModel = ticketDataRepo.findByTicketNumber(ticketNumber);
				preStatus=ticketModel.getIsFirstTicket();
				ticketModel.setIsFirstTicket(ticketNumberDto.getIsFirstTicket());
				
				//Maintain actionTrail
				 TicketActionTrail ticketActionTrail = ticketActionTrailForUpdate(DispatchControllerConstants.TICKET_UPDATED,ticketNumberDto.getActionBy(),preStatus,ticketModel.getIsFirstTicket());
				 log.info("update TicketActionTrail details 1 {} ", ticketActionTrail);
				 ticketModel.getTicketActionTrails().add(ticketActionTrail);
				
				 //Update in ticketDetails
				 ticketDataRepo.save(ticketModel);
				 
				 	if(ticketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_UNASSIGNED) || ticketModel.getGlobalStatus().equalsIgnoreCase(DispatchControllerConstants.STATUS_PAST_DUE))
					{
						transferAPI(ticketNumberDto.getTechnicianId(),ticketModel.getTicketNumber());	
					}
					else
					{
						updateTechnicianAndAssignmentForFirstTicketUpdate(ticketModel);
					}
				 apiResponseDto.setResponseData(ticketNumberDto.getTicketExternalId()+" : "+DispatchControllerConstants.STATUS_SUCCESS);
				 apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				 apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
				 return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
			}
			else
			{
				 apiResponseDto.setResponseData(ticketNumberDto.getTicketExternalId()+" : 	"+DispatchControllerConstants.TICKET_NOT_AVAILABLE);
				 apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				 apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
				 return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
			}
	   
			
	

	} catch (Exception e) {
		log.info("Unable to firstTicket" );
	
		apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
		apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
		apiResponseDto.setResponseData("");
		return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
	}

	/*@Override
	public ResponseEntity<ApiResponseDto> fetchQueueListBySupervisorId(
			AgentAndSupervisorRequestDTO agentAndSupervisorRequest) {
		ApiResponseDto apiResponseDto = new ApiResponseDto();

		try {
			boolean isSupervisorPresent = supervisorDataRepo.existsBySupervisorId(agentAndSupervisorRequest.getSupervisorId());
			 if(!isSupervisorPresent)
				{
					log.info("Supervisor not present");
					apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
					apiResponseDto.setMessage(DispatchControllerConstants.SUPERVISOR_NOT_FOUND);
					apiResponseDto.setResponseData(new ArrayList<>());
					return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
				}
			 
			 boolean isAgentPresent = technicianDataRepo.existsByTechnicianId(agentAndSupervisorRequest.getTechnicianId());
			 if(!isAgentPresent)
				{
					log.info("technician not present");
					apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
					apiResponseDto.setMessage(DispatchControllerConstants.TECHNICIAN_NOT_AVAILABLE);
					apiResponseDto.setResponseData(new ArrayList<>());
					return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
				}
			 
			 		Criteria criteria = new Criteria();
				
					Agent agent = technicianDataRepo.findByTechnicianIdAndSupervisorId(agentAndSupervisorRequest.getTechnicianId(),agentAndSupervisorRequest.getSupervisorId());
					
					criteria.and(DispatchControllerConstants.SUPERVISORID).in(agentAndSupervisorRequest.getSupervisorId());
					criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
					criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0));
					criteria.and(DispatchControllerConstants.POLYGON_ID).is(agentAndSupervisorRequest.getTechnicianId());
					if(agent.getCertificationList().contains(DispatchControllerConstants.NO))
					{
						criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(DispatchControllerConstants.NO);
						
					}
					else if(agent.getCertificationList().contains(DispatchControllerConstants.ALL) || agent.getCertificationList().contains(DispatchControllerConstants.GAS))
					{
						//No need to add criteria in this case
					}
					else 
					{
						criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(agent.getCertificationList());
						
					}
					Query q = new Query();
					q.addCriteria(criteria);
					q.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
					List<Ticket> list = mongoTemplate.find(q, Ticket.class);
						
					if (!list.isEmpty()) {
							apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
							apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
							apiResponseDto.setResponseData(list);
							return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
						}
						else
						{
							criteria = new Criteria();
							
							criteria.and(DispatchControllerConstants.SUPERVISORID).in(agentAndSupervisorRequest.getSupervisorId());
							criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
							criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0));
							
							if(agent.getCertificationList().contains(DispatchControllerConstants.NO))
							{
								criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(DispatchControllerConstants.NO);
								
							}
							else if(agent.getCertificationList().contains(DispatchControllerConstants.ALL) || agent.getCertificationList().contains(DispatchControllerConstants.GAS))
							{
								//No need to add criteria in this case
							}
							else 
							{
								criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(agent.getCertificationList());
								
							}
							Query q2 = new Query();
							q2.addCriteria(criteria);
							q2.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
							List<Ticket> list2 = mongoTemplate.find(q2, Ticket.class);
								if (!list2.isEmpty()) {
									apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
									apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
									apiResponseDto.setResponseData(list2);
									return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
								}
							apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
							apiResponseDto.setMessage(DispatchControllerConstants.TICKET_NOT_AVAILABLE);
							apiResponseDto.setResponseData(new ArrayList<>());
							return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
								
							
						
						}
					
				
			

		} catch (Exception e) {
			log.info("Unable to get tickets");
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(new ArrayList<>());
			return new ResponseEntity<>(apiResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);

		}
		
	
}
	*/
	
	 @Override
		public ResponseEntity<ApiResponseDto> fetchQueueListBySupervisorId(
				AgentAndSupervisorRequestDTO agentAndSupervisorRequest) {
			ApiResponseDto apiResponseDto = new ApiResponseDto();
			try {
			
			String isSatAvailable="No";
			String isSunAvailable="No";
			ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of(DispatchControllerConstants.DEFAULT_TIMEZONE));
			//DayOfWeek dayOfWeek = DayOfWeek.of(zdt.get(ChronoField.DAY_OF_WEEK));
			LocalDateTime startDateTime = null ;
			LocalDateTime endDateTime = null ;
			
			LocalDate startDate = null ;
		
			//boolean enddateflag = false;
			boolean isWeekend = false;
			boolean techFound = false;
			boolean iscontinue=false;
			List<TechnicianWorkHour> technicianWorkHourByTech = new ArrayList<>();
			
			   int i=1;    
			    do{    
			        System.out.println(i);
			        startDate = zdt.plusDays(i).toLocalDate();
			    	technicianWorkHourByTech = technicianWorkHourRepository.findByCalendarDateAndTechEmpId(startDate,agentAndSupervisorRequest.getTechnicianId());
			    	log.info("technicianWorkHourByTech"+technicianWorkHourByTech.toString());
			    	if(!CollectionUtils.isEmpty(technicianWorkHourByTech))
			    	{	
			    		long availableTime = Long.parseLong(technicianWorkHourByTech.get(technicianWorkHourByTech.size()-1).getAvailableTime());
			    		log.info("availableTime"+availableTime);
			    		if(availableTime>0)
			    		{
			    			iscontinue=false;
			    		}
			    		else
			    		{
			    			iscontinue=true;
			    		}
			    	}
			    	else
			    	{
			    		iscontinue=false;
			    	}
			    	i++;    
			    }while(iscontinue); 
			  
			    if(CollectionUtils.isEmpty(technicianWorkHourByTech))
				{
					log.info("Technician calender details not present");
					apiResponseDto.setStatus(DispatchControllerConstants.RESPONSE_OK);
					apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
					apiResponseDto.setResponseData(new ArrayList<>());
					return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);
				}
	
		        
		        DayOfWeek dayOfWeekForTest = DayOfWeek.of(technicianWorkHourByTech.get(technicianWorkHourByTech.size()-1).getCalendarDate().get(ChronoField.DAY_OF_WEEK));
		      
		        System.out.println("dayOfWeek"+dayOfWeekForTest);
		        LocalDate calenderDate=technicianWorkHourByTech.get(technicianWorkHourByTech.size()-1).getCalendarDate();
		       
		        if(dayOfWeekForTest == DayOfWeek.SATURDAY || dayOfWeekForTest == DayOfWeek.SUNDAY)
		        {
		        	//isSatAvailable="Yes";
		        	log.info("WeekEnd");
		        	isWeekend=true;
		        	startDateTime = calenderDate.atTime(0, 0, 0);
					endDateTime = calenderDate.atTime(23, 59, 59);
		        	ApiResponseDto apiResponseDtoWD = fetchQueueListBySupervisorIdWeekEND(agentAndSupervisorRequest,startDateTime,endDateTime,isWeekend,dayOfWeekForTest,calenderDate);
		        	return new ResponseEntity<>(apiResponseDtoWD, HttpStatus.OK);
		        	
		       
		        }
		        else
		        {
		        	log.info("Weekday");
		        	isWeekend=false;
		        	startDateTime = calenderDate.atTime(0, 0, 0);
					endDateTime = calenderDate.atTime(23, 59, 59);
					log.info("startDateTime" +startDateTime);
					log.info("endDateTime"+endDateTime);
		        	ApiResponseDto apiResponseDtoWD = fetchQueueListBySupervisorIdWeekEND(agentAndSupervisorRequest,startDateTime,endDateTime,isWeekend,dayOfWeekForTest,calenderDate);
		        	return new ResponseEntity<>(apiResponseDtoWD, HttpStatus.OK);
		        }
					
		} catch (Exception e) {
			log.info("Unable to get tickets");
			apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			apiResponseDto.setResponseData(new ArrayList<>());
			return new ResponseEntity<>(apiResponseDto, HttpStatus.OK);

		}
			
			
	}
		
	public ApiResponseDto fetchQueueListBySupervisorIdWeekEND(AgentAndSupervisorRequestDTO agentAndSupervisorRequest, LocalDateTime startDateTime, LocalDateTime endDateTime, boolean isWeekend, DayOfWeek dayOfWeekForTest, LocalDate calenderDate)
		{
		log.info("fetchQueueListBySupervisorIdWeekEND");
		ApiResponseDto apiResponseDto = new ApiResponseDto();
	try {
			boolean isSupervisorPresent = supervisorDataRepo.existsBySupervisorId(agentAndSupervisorRequest.getSupervisorId());
			if(!isSupervisorPresent)
			{
				log.info("Supervisor not present");
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				apiResponseDto.setMessage(DispatchControllerConstants.SUPERVISOR_NOT_FOUND);
				apiResponseDto.setResponseData(new ArrayList<>());
				return apiResponseDto;
			}
			log.info("isSupervisorPresent");
			boolean isAgentPresent = technicianDataRepo.existsByTechnicianId(agentAndSupervisorRequest.getTechnicianId());
			if(!isAgentPresent)
			{
				log.info("technician not present");
				apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				apiResponseDto.setMessage(DispatchControllerConstants.TECHNICIAN_NOT_AVAILABLE);
				apiResponseDto.setResponseData(new ArrayList<>());
				return  apiResponseDto;
			}
			log.info("isAgentPresent");
		 		Criteria criteria = new Criteria();
			
				Agent agent = technicianDataRepo.findByTechnicianIdAndSupervisorId(agentAndSupervisorRequest.getTechnicianId(),agentAndSupervisorRequest.getSupervisorId());
				 log.info("agent"+agent.toString());
				criteria.and(DispatchControllerConstants.SUPERVISORID).in(agentAndSupervisorRequest.getSupervisorId());
				criteria.and(DispatchControllerConstants.POLYGON_ID).is(agentAndSupervisorRequest.getTechnicianId());
				if(agent.getCertificationList().contains(DispatchControllerConstants.NO))
				{
					criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(DispatchControllerConstants.NO);
					
				}
				else if(agent.getCertificationList().contains(DispatchControllerConstants.ALL) || agent.getCertificationList().contains(DispatchControllerConstants.GAS))
				{
					//No need to add criteria in this case
				}
				else 
				{
					criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(agent.getCertificationList());
					
				}
				
				if(isWeekend)
				{
					if(dayOfWeekForTest == DayOfWeek.SATURDAY)
					{
					log.info("pasteDue");
					endDateTime = endDateTime.plusDays(1);
					criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).in(DispatchControllerConstants.getPastDueFetchStatusList());
					criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
					}
					else if(dayOfWeekForTest == DayOfWeek.SUNDAY)
					{
						criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).in(DispatchControllerConstants.getPastDueFetchStatusList());
						criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
					}
				}
				else
				{
					if(dayOfWeekForTest == DayOfWeek.FRIDAY)
					{
						
						//friday
						log.info("startDateTime"+startDateTime);
						log.info("endDateTime"+endDateTime);
						criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
						criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
					
						Query q = new Query();
						q.addCriteria(criteria);
						q.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
						List<Ticket> listFriday = mongoTemplate.find(q, Ticket.class);
						
						//monday
						
						Criteria criteriaForFridayAndMonday = new Criteria();
						
						startDateTime=startDateTime.plusDays(3);
						endDateTime=endDateTime.plusDays(3);
						log.info("startDateTime"+startDateTime);
						log.info("endDateTime"+endDateTime);
						
						criteriaForFridayAndMonday.and(DispatchControllerConstants.SUPERVISORID).in(agentAndSupervisorRequest.getSupervisorId());
						criteriaForFridayAndMonday.and(DispatchControllerConstants.POLYGON_ID).is(agentAndSupervisorRequest.getTechnicianId());
						criteriaForFridayAndMonday.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
						criteriaForFridayAndMonday.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
						
						if(agent.getCertificationList().contains(DispatchControllerConstants.NO))
						{
							criteriaForFridayAndMonday.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(DispatchControllerConstants.NO);
							
						}
						else if(agent.getCertificationList().contains(DispatchControllerConstants.ALL) || agent.getCertificationList().contains(DispatchControllerConstants.GAS))
						{
							//No need to add criteria in this case
						}
						else 
						{
							criteriaForFridayAndMonday.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(agent.getCertificationList());
							
						}
						Query q1 = new Query();
						q1.addCriteria(criteriaForFridayAndMonday);
						q1.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
						List<Ticket> listMonday = mongoTemplate.find(q1, Ticket.class);
						
						listFriday.addAll(listMonday);
					
						apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
						apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
						apiResponseDto.setResponseData(listFriday);
						return  apiResponseDto;
						
					}
					else
					{
						endDateTime = endDateTime.plusDays(1);
						criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
						log.info("endDateTime"+endDateTime);
						log.info("startDateTime"+startDateTime);
						criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
					}
					 log.info("unassigned");
			
				}
				
				log.info("log1");
				Query q = new Query();
				q.addCriteria(criteria);
				q.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
				List<Ticket> list = mongoTemplate.find(q, Ticket.class);
				log.info("log2");
					
				if (!list.isEmpty()) {
						apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
						apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
						apiResponseDto.setResponseData(list);
						log.info("log3");
						return apiResponseDto;
					}
					else
					{
						 log.info("log4");
						criteria = new Criteria();
						
						criteria.and(DispatchControllerConstants.SUPERVISORID).in(agentAndSupervisorRequest.getSupervisorId());
						
						if(isWeekend)
						{
							if(dayOfWeekForTest == DayOfWeek.SATURDAY)
							{
							log.info("pasteDue");
							criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).in(DispatchControllerConstants.getPastDueFetchStatusList());
							endDateTime = endDateTime.plusDays(1);
							criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
							}
							else if(dayOfWeekForTest == DayOfWeek.SUNDAY)
							{
								
								criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).in(DispatchControllerConstants.getPastDueFetchStatusList());
								
								criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
							}
						}
						else
						{
							if(dayOfWeekForTest == DayOfWeek.FRIDAY)
							{
								
								//friday
								log.info("startDateTime"+startDateTime);
								log.info("endDateTime"+endDateTime);
								criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
								criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
							
								Query q2 = new Query();
								q2.addCriteria(criteria);
								q2.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
								List<Ticket> listFriday = mongoTemplate.find(q2, Ticket.class);
								
								//monday
								
								Criteria criteriaForFridayAndMonday = new Criteria();
								
								startDateTime=startDateTime.plusDays(3);
								endDateTime=endDateTime.plusDays(3);
								log.info("startDateTime"+startDateTime);
								log.info("endDateTime"+endDateTime);
								
								criteriaForFridayAndMonday.and(DispatchControllerConstants.SUPERVISORID).in(agentAndSupervisorRequest.getSupervisorId());
								criteriaForFridayAndMonday.and(DispatchControllerConstants.POLYGON_ID).is(agentAndSupervisorRequest.getTechnicianId());
								criteriaForFridayAndMonday.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
								criteriaForFridayAndMonday.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
								
								if(agent.getCertificationList().contains(DispatchControllerConstants.NO))
								{
									criteriaForFridayAndMonday.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(DispatchControllerConstants.NO);
									
								}
								else if(agent.getCertificationList().contains(DispatchControllerConstants.ALL) || agent.getCertificationList().contains(DispatchControllerConstants.GAS))
								{
									//No need to add criteria in this case
								}
								else 
								{
									criteriaForFridayAndMonday.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(agent.getCertificationList());
									
								}
								Query q1 = new Query();
								q1.addCriteria(criteriaForFridayAndMonday);
								q1.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
								List<Ticket> listMonday = mongoTemplate.find(q1, Ticket.class);
								
								listFriday.addAll(listMonday);
							
								apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
								apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
								apiResponseDto.setResponseData(listFriday);
								return  apiResponseDto;
								
							}
							else
							{
								endDateTime = endDateTime.plusDays(1);
								criteria.and(DispatchControllerConstants.TICKETDUEDATEANDTIME).gte(startDateTime).lte(endDateTime);
								log.info("endDateTime"+endDateTime);
								log.info("startDateTime"+startDateTime);
								criteria.and(DispatchControllerConstants.FIELD_GLOBAL_STATUS).is(DispatchControllerConstants.STATUS_UNASSIGNED);
							}
							 log.info("unassigned");
							
						}
						
						if(agent.getCertificationList().contains(DispatchControllerConstants.NO))
						{
							criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(DispatchControllerConstants.NO);
							
						}
						else if(agent.getCertificationList().contains(DispatchControllerConstants.ALL) || agent.getCertificationList().contains(DispatchControllerConstants.GAS))
						{
							//No need to add criteria in this case
						}
						else 
						{
							criteria.and(DispatchControllerConstants.CERTIFICATION_REQUIRED).in(agent.getCertificationList());
							
						}
						
						Query q2 = new Query();
						q2.addCriteria(criteria);
						q2.with(Sort.by(Sort.Direction.ASC, DispatchControllerConstants.TICKETDUEDATEANDTIME));
						List<Ticket> list2 = mongoTemplate.find(q2, Ticket.class);
							if (!list2.isEmpty()) {
								apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
								apiResponseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
								apiResponseDto.setResponseData(list2);
								return  apiResponseDto;
							}
						apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
						apiResponseDto.setMessage(DispatchControllerConstants.TICKET_NOT_AVAILABLE);
						apiResponseDto.setResponseData(new ArrayList<>());
						return  apiResponseDto;
							
						
					
					}
				
			
		

	} catch (Exception e) {
		log.info("Unable to get tickets");
		apiResponseDto.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
		apiResponseDto.setMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
		apiResponseDto.setResponseData(new ArrayList<>());
		return  apiResponseDto;

	}
			
}
	
	 public Map<String, Object> missingInfoPTOFeedTommorrow(TechnicianAvailabilityDTO request)
	 {
		 Map<String,Object> pre=new HashMap<>();
		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		 //Map<String,Object> post=new HashMap<>();
		 
					if(StringUtils.isEmpty(request.getTechnicianId()))
					{
					pre.put(DispatchControllerConstants.TECHNICIANID, request.getTechnicianId());
					}
					
					if(StringUtils.isEmpty(request.getSupervisorId()))
					{
					pre.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
					}
					if(StringUtils.isEmpty(request.getCalenderDate()))
					{
					pre.put(DispatchControllerConstants.CALENDERDATE, request.getCalenderDate());
					}
					if(request.getAvailableTime()<0)
					{
					pre.put(DispatchControllerConstants.FIELD_AVAILABLETIME, request.getAvailableTime());
					}
					if(request.getProjectTime()<0)
					{
					pre.put(DispatchControllerConstants.FIELD_PROJECTTIME, request.getProjectTime());
					}

					if(StringUtils.isEmpty(request.getOnCallStartDateTime()))
					{
					pre.put(DispatchControllerConstants.ONCALL_STARTDATETIME, request.getOnCallStartDateTime());
					}
					if(StringUtils.isEmpty(request.getOnCallEndDateTime()))
					{
					pre.put(DispatchControllerConstants.ONCALL_ENDDATETIME, request.getOnCallEndDateTime());
					}
					if(StringUtils.isEmpty(request.getIsOnCall()))
					{
					pre.put(DispatchControllerConstants.ISONCALL, request.getIsOnCall());
					}
					if(StringUtils.isEmpty(request.getAvailabilityStatus()))
					{
					pre.put(DispatchControllerConstants.FIELD_AVAILABILITYSTATUS, request.getAvailabilityStatus());
					}
					
					
		return pre;
	 }

	 @Override
		public ResponseEntity<ResponseDTO> ptoTomorrowFeed(TechnicianAvailabilityRequest request) {
			
			String logKey = DispatchControllerConstants.PTO_TOMMORROW_FEED;
			String businessId = BusinessContext.getTenantId();
			String failedResponce= "Unable to get response for : ";
			int failedCount=0;
			String token = BusinessTokenContext.getBusinessToken();
			
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DATE_FORMAT);
		
			if(ObjectUtils.isEmpty(request) || CollectionUtils.isEmpty(request.getTechnicianAvailability()))
			{
				 log.info("bad request ");
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.RESPONSE_BAD_REQUEST,DispatchControllerConstants.RESPONSE_BAD_REQUEST, HttpStatus.OK);
				 
			}
			try {
			for (TechnicianAvailabilityDTO techAvailabilityDTO: request.getTechnicianAvailability())
			{
				
				ApiResponseDto response = callPTOFeed(techAvailabilityDTO,logKey,businessId);
				if(!StringUtils.equalsIgnoreCase(response.getStatus(), DispatchControllerConstants.STATUS_CODE_OK))
				{
					++failedCount;
					failedResponce.concat(response.getMessage()).concat(",");
				}
				
			}
			if(failedCount==0)
			{
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.STATUS_SUCCESS,DispatchControllerConstants.STATUS_SUCCESS, HttpStatus.OK);
				
			}
			else if(failedCount<request.getTechnicianAvailability().size())
			{
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.STATUS_PARTIAL_SUCCESS,failedResponce, HttpStatus.OK);
				
			}
			else
			{
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.STATUS_FAILED,failedResponce, HttpStatus.OK);
					
			}
			}catch(Exception e) {
				log.info("Unable to process {}  business {} for {} due to {} ",logKey,businessId,request,e.getMessage());
				return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),DispatchControllerConstants.STATUS_FAILED,e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		

			
		}

	private ApiResponseDto callPTOFeed(TechnicianAvailabilityDTO technicianAvailabilityDTO, String logKey, String businessId) {
		ApiResponseDto response = new ApiResponseDto();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		try {
			Map<String, Object> missingTechDetails = missingInfoPTOFeedTommorrow(technicianAvailabilityDTO);
			boolean isTechDetailsMissing =false;

			if (!CollectionUtils.isEmpty(missingTechDetails))
			{
				log.info("missingTechDetails  ");
				isTechDetailsMissing =true;

			}
			
			if(isTechDetailsMissing)
			{
				log.info("return Missingtech details  ");
				response.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
				response.setMessage(technicianAvailabilityDTO.getTechnicianName() +"-"+technicianAvailabilityDTO.getTechnicianId() );
				return response;
			}

			List<TechnicianAvailability> techModelList = new ArrayList<>();
			TechnicianAvailability techModel=new TechnicianAvailability();
			LocalDateTime localDateTimeStart= LocalDateTime.parse(technicianAvailabilityDTO.getCalenderDate()+DispatchControllerConstants.START_DATETIME_APPEND, dtf);
			LocalDateTime localDateTimeEnd= LocalDateTime.parse(technicianAvailabilityDTO.getCalenderDate()+DispatchControllerConstants.END_DATETIME_APPEND, dtf);
				
			boolean isTechPresent = techAvailabilityRepo.existsByTechnicianIdAndCalenderDateBetween(technicianAvailabilityDTO.getTechnicianId(),localDateTimeStart,localDateTimeEnd);
			
			log.info("isTechPresent{}",isTechPresent);
			//Update tech details if tech present
			
			if(isTechPresent) {
				techModelList = techAvailabilityRepo.findByTechnicianIdAndCalenderDateBetween(technicianAvailabilityDTO.getTechnicianId(),localDateTimeStart,localDateTimeEnd);
				techModel=techModelList.get(techModelList.size()-1);
				updateTechDetails(technicianAvailabilityDTO,techModel,isTechDetailsMissing);
				
				response.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				return response;
			}
			else
			{	
				dataConvertorUtils.convertTechModel(technicianAvailabilityDTO,techModel);
				techAvailabilityRepo.save(techModel);
			
				response.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
				return response;	
			}
			
				
		}catch(Exception e) {
		
			log.info("{} Unable to Save External technician Details, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			response.setStatus(DispatchControllerConstants.STATUS_CODE_INTERNAL_SERVER_ERROR);
			response.setMessage(technicianAvailabilityDTO.getTechnicianName() +"-"+technicianAvailabilityDTO.getTechnicianId() );
			return response;
		
		}
		
	}

	private void updateTechDetails(TechnicianAvailabilityDTO request, TechnicianAvailability techModel,
			boolean isTechDetailsMissing) {
		
		Map<String,Object> pre= new HashMap<>();
		Map<String,Object> post= new HashMap<>();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		DateTimeFormatter dtf_time = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_TIME_FORMAT);
		DateTimeFormatter dtf_date = DateTimeFormatter.ofPattern(DispatchControllerConstants.DATE_FORMAT);
		
		DateTimeFormatter FDF = DateTimeFormatter.ofPattern(DispatchControllerConstants.FILE_DATETIME_FORMAT);
		
		 if(!StringUtils.equalsIgnoreCase(request.getSupervisorId(),techModel.getSupervisorId())) {
			 	pre.put(DispatchControllerConstants.SUPERVISORID, techModel.getSupervisorId());
			 	techModel.setSupervisorId(request.getSupervisorId());
				post.put(DispatchControllerConstants.SUPERVISORID, request.getSupervisorId());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getTechnicianName(),techModel.getTechnicianName())) {
			 	pre.put(DispatchControllerConstants.TECHNICIAN_NAME, techModel.getTechnicianName());
			 	techModel.setTechnicianName(request.getTechnicianName());
				post.put(DispatchControllerConstants.TECHNICIAN_NAME, request.getTechnicianName());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getAvailabilityStatus(),techModel.getAvailabilityStatus())) {
			 	pre.put(DispatchControllerConstants.FIELD_AVAILABILITYSTATUS, techModel.getAvailabilityStatus());
			 	techModel.setAvailabilityStatus(request.getAvailabilityStatus());
				post.put(DispatchControllerConstants.FIELD_AVAILABILITYSTATUS, request.getAvailabilityStatus());
		 }
		 
		 if(!StringUtils.equalsIgnoreCase(request.getIsOnCall(),techModel.getIsOnCall())) {
			 	pre.put(DispatchControllerConstants.ISONCALL, techModel.getIsOnCall());
			 	techModel.setIsOnCall(request.getIsOnCall());
				post.put(DispatchControllerConstants.ISONCALL, request.getIsOnCall());
		 }
		 
		 
		 if(!StringUtils.equalsIgnoreCase(request.getJobTitle(),techModel.getJobTitle())) {
			 	pre.put(DispatchControllerConstants.JOBTITLE, techModel.getJobTitle());
			 	techModel.setJobTitle(request.getJobTitle());
				post.put(DispatchControllerConstants.JOBTITLE, request.getJobTitle());
		 }
		 
		 if(request.getAvailableTime()!=techModel.getAvailableTime()) {
			 	pre.put(DispatchControllerConstants.FIELD_AVAILABLETIME, techModel.getAvailableTime());
			 	techModel.setAvailableTime(request.getAvailableTime());
				post.put(DispatchControllerConstants.FIELD_AVAILABLETIME, request.getAvailableTime());
		 }
		 if(request.getProjectTime()!=techModel.getProjectTime()) {
			 	pre.put(DispatchControllerConstants.FIELD_PROJECTTIME, techModel.getProjectTime());
			 	techModel.setProjectTime(request.getProjectTime());
				post.put(DispatchControllerConstants.FIELD_PROJECTTIME, request.getProjectTime());
		 }
			
		 if(!StringUtils.equalsIgnoreCase(request.getOnCallStartDateTime(),techModel.getOnCallStartDateTime().format(dtf_time) )) {
			 	pre.put(DispatchControllerConstants.ONCALL_STARTDATETIME, techModel.getOnCallStartDateTime().format(dtf_time));
			 	String onCallStartDateTime=request.getCalenderDate()+ " "+request.getOnCallStartDateTime()+ ":00" ;
			 	techModel.setOnCallStartDateTime(LocalDateTime.parse(onCallStartDateTime, dtf));
				post.put(DispatchControllerConstants.ONCALL_STARTDATETIME, request.getOnCallStartDateTime());
		 }
		 if(!StringUtils.equalsIgnoreCase(request.getOnCallEndDateTime(),techModel.getOnCallEndDateTime().format(dtf_time))) {
			 	pre.put(DispatchControllerConstants.ONCALL_ENDDATETIME, techModel.getOnCallEndDateTime().format(dtf_time));
			 	String onCallEndDateTime=request.getCalenderDate()+ " "+request.getOnCallEndDateTime()+ ":00" ;
			 	LocalDateTime endDT=LocalDateTime.parse(onCallEndDateTime, dtf);
				
				if(techModel.getOnCallStartDateTime().isAfter(endDT))
				{
					endDT=endDT.plusDays(1);
				}
			 	techModel.setOnCallEndDateTime(endDT);
				post.put(DispatchControllerConstants.ONCALL_ENDDATETIME, request.getOnCallEndDateTime());
		 }
		 techModel.setTimestamp(LocalDateTime.now());
		 String preAction = new Gson().toJson(pre);
		 String postAction = new Gson().toJson(post);
		 techModel.getAdditionalInfo().put("oldRecords" +"_"+LocalDateTime.now().format(FDF), preAction);
		 techModel.getAdditionalInfo().put("updatedRecords" +"_"+LocalDateTime.now().format(FDF), postAction);
		 
		
		//Update ticket data
		 techAvailabilityRepo.save(techModel);	
		 log.info(" Tech details updated {} ");
		
	}

	@Override
	public ResponseEntity<ResponseDTO> ptoWeeklyFeed(TechnicianAvailabilityRequest request) {
		
		String logKey = DispatchControllerConstants.PTO_WEEKLY_FEED;
		String businessId = BusinessContext.getTenantId();
		String failedResponce= "Unable to get response for : ";
		int failedCount=0;
		String token = BusinessTokenContext.getBusinessToken();
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DispatchControllerConstants.DATE_FORMAT);
	
		if(ObjectUtils.isEmpty(request) || CollectionUtils.isEmpty(request.getTechnicianAvailability()))
		{
			 log.info("bad request ");
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.RESPONSE_BAD_REQUEST,DispatchControllerConstants.RESPONSE_BAD_REQUEST, HttpStatus.OK);
			 
		}
		try {
		for (TechnicianAvailabilityDTO techAvailabilityDTO: request.getTechnicianAvailability())
		{
			
			ApiResponseDto response = callPTOFeed(techAvailabilityDTO,logKey,businessId);
			if(!StringUtils.equalsIgnoreCase(response.getStatus(), DispatchControllerConstants.STATUS_CODE_OK))
			{
				++failedCount;
				failedResponce.concat(response.getMessage()).concat(",");
			}
			
		}
		if(failedCount==0)
		{
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.STATUS_SUCCESS,DispatchControllerConstants.STATUS_SUCCESS, HttpStatus.OK);
			
		}
		else if(failedCount<request.getTechnicianAvailability().size())
		{
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.STATUS_PARTIAL_SUCCESS,failedResponce, HttpStatus.OK);
			
		}
		else
		{
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.STATUS_FAILED,failedResponce, HttpStatus.OK);
				
		}
		}catch(Exception e) {
			log.info("Unable to process {}  business {} for {} due to {} ",logKey,businessId,request,e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),DispatchControllerConstants.STATUS_FAILED,e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<ApiResponseDto> ticketSequenceListByTechId(TechIdDto technicianId, String businessId) {
			 log.info("For businessId: {} Inside ticketSequenceListByTechId Method ",businessId);
			
		        ApiResponseDto responseDto = new ApiResponseDto();
		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DispatchControllerConstants.DEFAULT_DATETIME_FORMAT);
		        Criteria criteria;
		    	
		    			
		        try {
		            if (technicianId == null || technicianId.getTechnicianId() == null) {
		            	log.info("For businessId: {} Request Received for ticketSequenceListByTechId Method is Invalid ",businessId);
		                responseDto.setStatus(DispatchControllerConstants.STATUS_CODE_BAD_REQUEST);
		                responseDto.setMessage(DispatchControllerConstants.RESPONSE_BAD_REQUEST);
		                responseDto.setResponseData(new ArrayList<>());
		                return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
		            }
		             
		           //if loop : if dates are balnk in request
		            if (StringUtils.isEmpty(technicianId.getStartDate()) && StringUtils.isEmpty(technicianId.getEndDate())) {
						/*
						 * LocalDate localDate=LocalDate.now(); LocalDateTime localDateTimeStart=
						 * LocalDateTime.parse(localDate+DispatchControllerConstants.
						 * START_DATETIME_APPEND, formatter); LocalDateTime localDateTimeEnd=
						 * LocalDateTime.parse(localDate+DispatchControllerConstants.
						 * END_DATETIME_APPEND, formatter);
						 */	
		    			/*
						 * criteria =
						 * Criteria.where(DispatchControllerConstants.FIELD_AGENTTECHNICIANID).is(
						 * technicianId.getTechnicianId())
						 * .and(DispatchControllerConstants.TIMESTAMP).gte(localDateTimeStart).lte(
						 * localDateTimeEnd);
						 */
		    			
		    			criteria = Criteria.where(DispatchControllerConstants.FIELD_AGENTTECHNICIANID).is(technicianId.getTechnicianId());
			                 
		            	
		            }
		            //pass todays date as startDate and enddate
		            else
		            {
		            	String startDate = technicianId.getStartDate();
			            String endDate = technicianId.getEndDate();

			            LocalDateTime startDateTime = LocalDateTime.parse(startDate, formatter);
			            LocalDateTime endDateTime = LocalDateTime.parse(endDate, formatter);
			            
			            criteria = Criteria.where(DispatchControllerConstants.FIELD_AGENTTECHNICIANID).is(technicianId.getTechnicianId())
			                    .and(DispatchControllerConstants.TIMESTAMP).gte(startDateTime).lte(endDateTime);
			            
		            }
		            
		            MatchOperation matchOperation = Aggregation.match(criteria);

		           
					
		            // Add the match operation to the aggregation pipeline
		            TypedAggregation<AgentASTicketResponce> aggregation = Aggregation.newAggregation(
		            		AgentASTicketResponce.class, matchOperation,
		            		Aggregation.sort(Sort.by(DispatchControllerConstants.PROCESS_ID).descending()),
		 					Aggregation.limit(1)
		            );
		          
		            
					
		            // Execute the aggregation query
		            List<AgentASTicketResponce> results = mongoTemplate.aggregate(aggregation,
		            		AgentASTicketResponce.class).getMappedResults();
		         
		           
		            if (results.isEmpty()) {
		            	log.info("For businessId: {} No data found for the given technicianId and date range",businessId);
		                responseDto.setStatus(DispatchControllerConstants.STATUS_CODE_OK);
		                responseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
		                responseDto.setResponseData(new ArrayList<>());
		                return new ResponseEntity<>(responseDto, HttpStatus.OK);
		            }
		            
		            //TicketSequence ts = new TicketSequence();
		            List<TicketSequenceMain> ticketListMain= new ArrayList<>();
		           
		            List<TicketSequence> previousDayTicketList = results.get(0).getAgent().getTicketList();

		            

					for(TicketSequence ticket : previousDayTicketList){

						//if(ticket.getTicketDueDateAndTime().isAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))) {

							if((StringUtils.equalsIgnoreCase(ticket.getGlobalStatus(), DispatchControllerConstants.STATUS_ASSIGNED)) ||
									(StringUtils.equalsIgnoreCase(ticket.getGlobalStatus(), DispatchControllerConstants.STATUS_COMPLETE)
									&& ticket.getCompletionDateTime().isAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))))
							{
							  TicketSequenceMain t= new TicketSequenceMain();
			            	  t.setTicketExternalId(ticket.getTicketNumber());
			            	  t.setIsFirstTicket(ticket.getIsFirstTicket());
			            	  t.setConversationId(ticket.getConversationId());
			            	  ticketListMain.add(t);
							}
						//}

					}
					
					LocalDateTime startDateTime = LocalDateTime.now().minusDays(90).withHour(0).withMinute(0).withSecond(0);
					LocalDateTime endDateTime = LocalDateTime.now().plusDays(90).withHour(0).withMinute(0).withSecond(0);
					
					//ticketDcSolver code :start
					LocalDateTime ldt_start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
					
					List<Ticket> dbResponse = ticketDataRepo.findByTechnicianIdAndTimestampBetweenList(
							technicianId.getTechnicianId(), startDateTime, endDateTime,new String[] { DispatchControllerConstants.STATUS_ASSIGNED,
									DispatchControllerConstants.STATUS_UNASSIGNED,
									DispatchControllerConstants.STATUS_MISSING_INFO,
									DispatchControllerConstants.STATUS_RESCHEDULE,
									DispatchControllerConstants.STATUS_CANCELLED},DispatchControllerConstants.STATUS_COMPLETE, ldt_start);
					
					
					 
					 List<String> ticketIdList=ticketListMain.stream().map(TicketSequenceMain::getTicketExternalId).collect(Collectors.toList());				
					
					dbResponse.forEach(assignedTicket->{
						
						if((StringUtils.equalsIgnoreCase(assignedTicket.getGlobalStatus(), DispatchControllerConstants.STATUS_ASSIGNED)) ||
								(StringUtils.equalsIgnoreCase(assignedTicket.getGlobalStatus(), DispatchControllerConstants.STATUS_COMPLETE)
								&& assignedTicket.getCompletionDateTime().isAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))))
						{
							
									if(!ticketIdList.contains(assignedTicket.getTicketNumber()))
									{
										 TicketSequenceMain t= new TicketSequenceMain();
						            	  t.setTicketExternalId(assignedTicket.getTicketNumber());
						            	  t.setIsFirstTicket(assignedTicket.getIsFirstTicket());
						            	  t.setConversationId(assignedTicket.getConversationId());
						            	  ticketListMain.add(t);
									}
						 
						}
						
					});
					
					//ticketDcSolver code :end
					
		          
					/*
					 * for(TicketSequence ts : results.get(0).getAgent().getTicketList()) {
					 * TicketSequenceMain t= new TicketSequenceMain();
					 * t.setTicketExternalId(ts.getTicketNumber());
					 * t.setIsFirstTicket(ts.getIsFirstTicket());
					 * t.setConversationId(ts.getConversationId()); ticketListMain.add(t); }
					 */
		           
		            AgentTSMain agentMain=new AgentTSMain();
		            agentMain.setTechnicianId(results.get(0).getAgent().getTechnicianId());
		            agentMain.setTicketList(ticketListMain);
		          
		            if (results.isEmpty()) {
		            	log.info("For businessId: {} No data found for the given technicianId and date range",businessId);
		                responseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
		                responseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
		                return new ResponseEntity<>(responseDto, HttpStatus.OK);
		            } else {
		            	
		                responseDto.setStatus(DispatchControllerConstants.STATUS_SUCCESS);
		                responseDto.setMessage(DispatchControllerConstants.STATUS_SUCCESS);
		                responseDto.setResponseData(agentMain);
		                return new ResponseEntity<>(responseDto, HttpStatus.OK);
		            }
		        } catch (DateTimeParseException e) {
		        	log.info("For businessId: {} Invalid date format for ticketSequenceListByTechId Method ",businessId);
		            responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
		            responseDto.setMessage("Invalid date format");
		            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
		        } catch (Exception e) {
		        	log.info("For businessId: {} Unable to process the request for ticketSequenceListByTechId {} ", businessId,e);
		            responseDto.setStatus(DispatchControllerConstants.STATUS_FAILED);
		            responseDto.setMessage(DispatchControllerConstants.STATUS_FAILED);
		            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
		        }
		    }

	@Override
	public ResponseEntity<ResponseDTO> updateTechnicianDetailsToDC(Agent requestAgent) {
		
		String logKey = DispatchControllerConstants.UPDATE_TECHNICIAN_DETAILS_TO_DC;
		String businessId = BusinessContext.getTenantId();

		try {
			
			 Agent agent = technicianDataRepo.findByTechnicianId(requestAgent.getTechnicianId());
			 if(agent == null)
			 {
					log.info("Technician not present");
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.RESPONSE_TECHNICIAN_NOT_FOUND,"", HttpStatus.OK);
					
				}else {
					agent.setFirstName(requestAgent.getFirstName()!=null && !requestAgent.getFirstName().equals("") ? requestAgent.getFirstName():agent.getFirstName());
					agent.setLastName(requestAgent.getLastName()!=null && !requestAgent.getLastName().equals("") ? requestAgent.getLastName():agent.getLastName());
					agent.setEmailId(requestAgent.getEmailId()!=null && !requestAgent.getEmailId().equals("") ? requestAgent.getEmailId():agent.getEmailId());
					agent.setCuid(requestAgent.getCuid()!=null && !requestAgent.getCuid().equals("") ? requestAgent.getCuid():agent.getCuid());
					agent.setAddress(requestAgent.getAddress()!=null && !requestAgent.getAddress().equals("") ? requestAgent.getAddress():agent.getAddress());
					agent.setIsActive(requestAgent.getIsActive()!=null && !requestAgent.getIsActive().equals("") ? requestAgent.getIsActive():agent.getIsActive());
					agent.setSupervisorName(requestAgent.getSupervisorName()!=null && !requestAgent.getSupervisorName().equals("") ? requestAgent.getSupervisorName():agent.getSupervisorName());
					agent.setSupervisorId(requestAgent.getSupervisorId()!=null && !requestAgent.getSupervisorId().equals("") ? requestAgent.getSupervisorId():agent.getSupervisorId());
					
					agent.setTechnicianExplevel(requestAgent.getTechnicianExplevel()!=0 ? requestAgent.getTechnicianExplevel():agent.getTechnicianExplevel());
					
					agent.setTechnicianAvailability(requestAgent.getTechnicianAvailability()!=null && !requestAgent.getTechnicianAvailability().equals("") ? requestAgent.getTechnicianAvailability():agent.getTechnicianAvailability());
					agent.setPolygonId(requestAgent.getPolygonId()!=null && !requestAgent.getPolygonId().equals("") ? requestAgent.getPolygonId():agent.getPolygonId());
					agent.setTenure(requestAgent.getTenure()!=null && !requestAgent.getTenure().equals("") ? requestAgent.getTenure():agent.getTenure());
					agent.setPhoneNumber(requestAgent.getPhoneNumber()!=null && !requestAgent.getPhoneNumber().equals("") ? requestAgent.getPhoneNumber():agent.getPhoneNumber());
					agent.setJobTitle(requestAgent.getJobTitle()!=null && !requestAgent.getJobTitle().equals("") ? requestAgent.getJobTitle():agent.getJobTitle());
					
					agent.setLocation(requestAgent.getLocation()!=null && requestAgent.getLocation().getLatitude() !=0 && requestAgent.getLocation().getLongitude() !=0 ? requestAgent.getLocation():agent.getLocation());
				
					agent.setSkills(requestAgent.getSkills()!=null && !requestAgent.getSkills().equals("") ? requestAgent.getSkills():agent.getSkills());
					
					agent.setDefaultAvailableTime(requestAgent.getDefaultAvailableTime()!=0 ? requestAgent.getDefaultAvailableTime():agent.getDefaultAvailableTime());
					agent.setAvailableTime(requestAgent.getAvailableTime()!=0 ? requestAgent.getAvailableTime():agent.getAvailableTime());
					agent.setCalculatedAvailableTime(requestAgent.getCalculatedAvailableTime()!=0 ? requestAgent.getCalculatedAvailableTime():agent.getCalculatedAvailableTime());
					
					agent.setCity(requestAgent.getCity()!=null && !requestAgent.getCity().equals("") ? requestAgent.getCity():agent.getCity());
					agent.setCounty(requestAgent.getCounty()!=null && !requestAgent.getCounty().equals("") ? requestAgent.getCounty():agent.getCounty());
					agent.setState(requestAgent.getState()!=null && !requestAgent.getState().equals("") ? requestAgent.getState():agent.getState());
					agent.setVehicleType(requestAgent.getVehicleType()!=null && !requestAgent.getVehicleType().equals("") ? requestAgent.getVehicleType():agent.getVehicleType());
					
					agent.setTotalWorkHourGlobal(requestAgent.getTotalWorkHourGlobal()!=0 ? requestAgent.getTotalWorkHourGlobal():agent.getTotalWorkHourGlobal());
					
					agent.setAgentType(requestAgent.getAgentType()!=null && !requestAgent.getAgentType().equals("") ? requestAgent.getAgentType():agent.getAgentType());
					
					agent.setAdditionalInfo(requestAgent.getAdditionalInfo() !=null && requestAgent.getAdditionalInfo().entrySet() != null && !requestAgent.getAdditionalInfo().entrySet().isEmpty()?requestAgent.getAdditionalInfo():agent.getAdditionalInfo());
					
					agent.setAssignmentStatus(requestAgent.getAssignmentStatus()!=null && !requestAgent.getAssignmentStatus().equals("") ? requestAgent.getAssignmentStatus():agent.getAssignmentStatus());
					agent.setAvailabilityStatus(requestAgent.getAvailabilityStatus()!=null && !requestAgent.getAvailabilityStatus().equals("") ? requestAgent.getAvailabilityStatus():agent.getAvailabilityStatus());
					
					agent.setTechnicianScore(requestAgent.getTechnicianScore()!=0 ? requestAgent.getTechnicianScore():agent.getTechnicianScore());
					
					agent.setCertificationList(requestAgent.getCertificationList() != null && !requestAgent.getCertificationList().isEmpty()? requestAgent.getCertificationList():agent.getCertificationList());
					agent.setTicketList(requestAgent.getTicketList() != null && !requestAgent.getTicketList().isEmpty()?requestAgent.getTicketList():agent.getTicketList());
					
					agent.setRemainingWorkTime(requestAgent.getRemainingWorkTime()!=0 ? requestAgent.getRemainingWorkTime():agent.getRemainingWorkTime());
					agent.setEvaluatedDistance(requestAgent.getEvaluatedDistance()!=0.0 && requestAgent.getEvaluatedDistance()!=0 ? requestAgent.getEvaluatedDistance():agent.getEvaluatedDistance());
					
					agent.setAutoAssignment(requestAgent.getAutoAssignment()!=null && !requestAgent.getAutoAssignment().equals("") ? requestAgent.getAutoAssignment():agent.getAutoAssignment());
					
				    technicianDataRepo.save(agent);
				}
			 return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
			
		}catch(Exception e) {
			log.info("{} Unable to Update External technician Details, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR,e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@Override
	public ResponseEntity<ResponseDTO> updateSupervisorDetailsToDC(SupervisorPolygonMapping request) {
		
		String logKey = DispatchControllerConstants.UPDATE_SUPERVISOR_DETAILS_TO_DC;
		String businessId = BusinessContext.getTenantId();

		try {
			
			SupervisorPolygonMapping supervisor = supervisorDataRepo.findBySupervisorId(request.getSupervisorId());
			 if(supervisor == null)
				{
					log.info("Supervisor not present");
					return dispatchControllerSupportUtils.generateResponse(HttpStatus.BAD_REQUEST.value(),DispatchControllerConstants.RESPONSE_SUPERVISOR_NOT_FOUND,"", HttpStatus.OK);
					
				}else {
					supervisor.setFirstName(request.getFirstName()!=null && !request.getFirstName().equals("") ? request.getFirstName():supervisor.getFirstName());
					supervisor.setLastName(request.getLastName()!=null && !request.getLastName().equals("") ? request.getLastName():supervisor.getLastName());
					supervisor.setEmailId(request.getEmailId()!=null && !request.getEmailId().equals("") ? request.getEmailId():supervisor.getEmailId());
					supervisor.setIsActive(request.getIsActive()!=null && !request.getIsActive().equals("") ? request.getIsActive():supervisor.getIsActive());
					supervisor.setManagerName(request.getManagerName()!=null && !request.getManagerName().equals("") ? request.getManagerName():supervisor.getManagerName());
					supervisor.setManagerId(request.getManagerId()!=null && !request.getManagerId().equals("") ? request.getManagerId():supervisor.getManagerId());
					
					supervisor.setSupervisorExplevel(request.getSupervisorExplevel()!=0 ? request.getSupervisorExplevel():supervisor.getSupervisorExplevel());
					
					supervisor.setSupervisorAvailability(request.getSupervisorAvailability()!=null && !request.getSupervisorAvailability().equals("") ? request.getSupervisorAvailability():supervisor.getSupervisorAvailability());
					supervisor.setSupervisorPolygonId(request.getSupervisorPolygonId()!=null && !request.getSupervisorPolygonId().equals("") ? request.getSupervisorPolygonId():supervisor.getSupervisorPolygonId());
					
					supervisor.setPolygonList(request.getPolygonList() != null && !request.getPolygonList().isEmpty() ? request.getPolygonList():supervisor.getPolygonList());
					
					supervisor.setPhoneNumber(request.getPhoneNumber()!=null && !request.getPhoneNumber().equals("") ? request.getPhoneNumber():supervisor.getPhoneNumber());
					
					supervisor.setJobTitle(request.getJobTitle()!=null && !request.getJobTitle().equals("") ? request.getJobTitle():supervisor.getJobTitle());
					supervisor.setLocation(request.getLocation()!=null && request.getLocation().getLatitude() !=0 && request.getLocation().getLongitude() !=0 ? request.getLocation():supervisor.getLocation());
				
					supervisor.setCity(request.getCity()!=null && !request.getCity().equals("") ? request.getCity():supervisor.getCity());
					supervisor.setCounty(request.getCounty()!=null && !request.getCounty().equals("") ? request.getCounty():supervisor.getCounty());
					supervisor.setState(request.getState()!=null && !request.getState().equals("") ? request.getState():supervisor.getState());
					
					supervisor.setAdditionalInfo(request.getAdditionalInfo() !=null && request.getAdditionalInfo().entrySet() != null && !request.getAdditionalInfo().entrySet().isEmpty()?request.getAdditionalInfo():supervisor.getAdditionalInfo());
					
					supervisor.setAvailabilityStatus(request.getAvailabilityStatus() !=null && !request.getAvailabilityStatus().equals("")? request.getAvailabilityStatus():supervisor.getAvailabilityStatus());
					supervisor.setTicketList(request.getTicketList() != null && ! request.getTicketList().isEmpty()?request.getTicketList():supervisor.getTicketList());
					
					supervisor.setAutoAssignment(request.getAutoAssignment()!=null && !request.getAutoAssignment().equals("") ? request.getAutoAssignment():supervisor.getAutoAssignment());
					
					supervisorDataRepo.save(supervisor);
				}
			 return dispatchControllerSupportUtils.generateResponse(HttpStatus.OK.value(),DispatchControllerConstants.RESPONSE_OK,DispatchControllerConstants.RESPONSE_SUBMITTED, HttpStatus.OK);
			
		}catch(Exception e) {
			log.info("{} Unable to Update External technician Details, for businessId : {} , due to {}",logKey, businessId,e.getMessage());
			return dispatchControllerSupportUtils.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR,e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@Override
	@Async("asyncExecutor")
	public void getEmployeeQualityScoreAndServiceData(String businessId, String businessToken) {
		LocalDateTime startTime = LocalDateTime.now();
		log.info("Buisness Id :{} : Request Recived for AutoUpdateTicketScore API started at : {} ", businessId,startTime);
		try {
			
			BusinessContext.setTenantId(businessId);
			BusinessTokenContext.setBusinessToken(businessToken);

			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
	        String formattedDate = startTime.format(formatter);
	        
	        List<SystemConfigDC> systemConfigDCList = new ArrayList<>();
			Map<String,SystemConfigDC> systemConfigDCMap = new HashMap<>();
			try {
				systemConfigDCList = systemConfigDCRepository.findByConfigRoleAndIsActive(DispatchControllerConstants.ROLE_DC_CORE ,DispatchControllerConstants.FLAG_Y);
				systemConfigDCList.forEach(config->{
					systemConfigDCMap.put(config.getConfigProperty(), config);
				});
			}catch(Exception e) {
				log.info("Unable to fetch System DC Config List due to {}",e.getMessage());
			}
			
			String fileNames=DispatchControllerConstants.SFTP_FILE_NAME;
			
			try {
				if(!CollectionUtils.isEmpty(systemConfigDCMap)) {

					if(systemConfigDCMap.containsKey(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_FILE_NAME)) {
						fileNames =  systemConfigDCMap.get(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_FILE_NAME).getConfigValue();
						log.info("Connecting to db systemConfig to fetch SFTP_FILE_NAME : {} : "+ fileNames);
					}
				}else {
					fileNames=DispatchControllerConstants.SFTP_FILE_NAME;
					log.info("fetch from system constant SFTP_FILE_NAME : "+ fileNames);
					
				}
			}catch(Exception e) {
				log.info("Unable to get Default System Config Id due to {}",e.getMessage());

				fileNames=DispatchControllerConstants.SFTP_FILE_NAME;
				log.info("exception from fetching db , fetch from system constant DEFAULT_SFTP_USERNAME : "+ fileNames);
			}
	        
	        
	        String employeeScoreFile = fileNames+formattedDate+".csv";
	        log.info("Sftp file name recevied to fetch data : {} ",employeeScoreFile);
	        
//			String employeeScoreFile = "employee_score_test _11082023.csv";
			List<Map<String, String>> employeeScoreData = sftpFileService
					.getEmployeeQualityAndServiceData(employeeScoreFile);

			List<Map<String, String>> employeeTenureData = sftpFileService
					.getServiceData();
//			System.out.println("List of employeeScoreData :" + employeeScoreData);
//			System.out.println("List of employeeTenureData :" + employeeTenureData);
			List<Agent> technicianIds = technicianDataRepo.findByIsActiveAndAgentType("Y", "Actual");
			List<String> agentTechnicianIds = new ArrayList<>();

			AgentTicketRiskScore ticketData = new AgentTicketRiskScore();
			ticketData.setCreatedDate(LocalDateTime.now());
			
			List<UpdatedTicketScoreData> updatedTicketScoreDataList = new ArrayList<>();
			
			for (Agent agent : technicianIds) {
				agentTechnicianIds.add(agent.getTechnicianId());
			}

//			findMatchingTechnicians(employeeScoreData, employeeTenureData);
			log.info("List of agentTechnicianIds :" + agentTechnicianIds);
			
			for (String technicianId : agentTechnicianIds) {
//				UpdatedTicketScoreData scoreData= new UpdatedTicketScoreData();
//				System.out.println("TechnicianIds :" + technicianId);

				Optional<Map<String, String>> data1 = employeeScoreData.stream()
						.filter(data -> technicianId.equals(data.get("TechnicianId"))).findFirst();

//				System.out.println("data1 :" + data1.toString());

				Optional<Map<String, String>> data2 = employeeTenureData.stream()
						.filter(data -> technicianId.equals(data.get("technicianId"))).findFirst();
//				System.out.println("data2 :" + data2.toString());
//				System.out.println("TechnicianIds :" + technicianId);
//
				if (data1.isPresent() && data2.isPresent()) {
//					System.out.println("data1 is present :" + data1.toString());
//					System.out.println("data2 is present :" + data2.toString());

					Pattern pattern = Pattern.compile("continuousServiceDate=(\\S+),");
					Pattern patternSecond = Pattern.compile("mostRecentHireDate=(\\S+),");
					String continuousServiceDate = null;
					String mostRecetHireDate = null;
					int tenureDeduction = 0;
					Matcher matcher = pattern.matcher(data2.toString());
					Matcher matcherSecond = patternSecond.matcher(data2.toString());
					if (matcher.find()) {
						continuousServiceDate = matcher.group(1);
//						System.out.println("Continuous_Service_Date: " + continuousServiceDate);
					}
					if (continuousServiceDate == null || StringUtils.isEmpty(continuousServiceDate)) {
						mostRecetHireDate = matcherSecond.group(1);
						log.info("Continuous_Service_Date not found. So fetching mostRecetHireDate :"+ mostRecetHireDate);

						continuousServiceDate = mostRecetHireDate;
					}

					if (continuousServiceDate == null || StringUtils.isEmpty(continuousServiceDate)) {
						log.info("Continuous_Service_Date & Most_Recent_Hire_Date not found. Considering tenureDeduction = 0; ");
						tenureDeduction = 0;
					} else {
						log.info("Now calculating the tenureDedution");
						tenureDeduction = calculateTenureDeduction(continuousServiceDate);
					}

					log.info("tenureDeduction :" + tenureDeduction);

					String employeeQualityScore = null;
					Pattern patternEmployeeQualityScore = Pattern.compile("employee_quality_score=(\\S+),");
					Matcher matcherEmployeeQualityScore = patternEmployeeQualityScore.matcher(data1.toString());
//					System.out.println("matcherEmployeeQualityScore: " + matcherEmployeeQualityScore);
					if (matcherEmployeeQualityScore.find()) {
						employeeQualityScore = matcherEmployeeQualityScore.group(1);
						log.info("employee_quality_score: " + employeeQualityScore);
					}
					
					if (employeeQualityScore == null || StringUtils.isEmpty(employeeQualityScore)) {
						log.info("employee_quality_score not found. Considering employee_quality_score as default= 7");
						employeeQualityScore = "7";
					} 
					int fetchedEmployeeQualityScore = Integer.parseInt(employeeQualityScore);
					int technicianRiskScore = fetchedEmployeeQualityScore - tenureDeduction;
//					System.out.println("employeeQualityScore :" + fetchedEmployeeQualityScore);
//					System.out.println("technicianRiskScore :" + technicianRiskScore);

					log.info("Adjust technician risk score if >10 or <0  :" + technicianRiskScore);
					technicianRiskScore = Math.max(0, Math.min(10, technicianRiskScore));

					log.info("Update or store the technician risk score : {} ",technicianRiskScore);
					Agent agents = technicianDataRepo.findByTechnicianId(technicianId);
					
					UpdatedTicketScoreData ticketScoreData= new UpdatedTicketScoreData();
					ticketScoreData.setTechnicianId(technicianId);
					ticketScoreData.setContinuousServiceDate(continuousServiceDate);
					ticketScoreData.setEmployeeQualityScore(fetchedEmployeeQualityScore);
					ticketScoreData.setMostRecentHireDate(mostRecetHireDate);
					ticketScoreData.setPreTicketRiskScore(agents.getTechnicianScore());
					ticketScoreData.setPostTicketRiskScore(technicianRiskScore);
					
					updatedTicketScoreDataList.add(ticketScoreData);
					
					Query query = new Query(Criteria.where("technicianId").is(technicianId));
		            Update update = new Update().set("technicianScore", technicianRiskScore);
		            mongoTemplate.updateFirst(query, update, Agent.class);
				}

			}
			ticketData.setUpdatedTicketScoreData(updatedTicketScoreDataList);
			agentTicketRiskScoreRepo.save(ticketData);
			log.info("Updated technician risk score data : {} ",ticketData);
		} catch (Exception e) {

		} finally {
			BusinessContext.clear();
			BusinessTokenContext.clear();

			long timeDiff = Duration.between(startTime, LocalDateTime.now()).toMillis();
			log.info("Buisness Id :{} : Request Recived for AutoUpdateTicketScore API Completed in : {} ", businessId,
					timeDiff);
		}
	}

	private int calculateTenureDeduction(String continuousServiceDate) {

//		System.out.println("continuousServiceDate :" + continuousServiceDate);

		LocalDate serviceDate = LocalDate.parse(continuousServiceDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

//		System.out.println("serviceDate :" + serviceDate);

		long daysSinceHire = ChronoUnit.DAYS.between(serviceDate, LocalDate.now());

		log.info("Number of daysSinceHire :" + daysSinceHire);

		// Calculating tenure deduction
		if (daysSinceHire <= 90) {
			return 4;
		} else if (daysSinceHire <= 180) {
			return 2;
		} else if (daysSinceHire <= 365) {
			return 1;
		} else {
			return 0;
		}
	}

	public List<Map<String, String>> findMatchingTechnicians(List<Map<String, String>> fileName1Data,
			List<Map<String, String>> fileName2Data) {

		List<Map<String, String>> matchingTechnicians = new ArrayList<>();

		for (Map<String, String> data1 : fileName1Data) {
			String technicianId1 = data1.get("TechnicianId");
			for (Map<String, String> data2 : fileName2Data) {
				String technicianId2 = data2.get("TechnicianId");
				if (technicianId1.equals(technicianId2)) {
					matchingTechnicians.add(data1);
					break;
				}
			}
		}
		System.out.println("matchingTechnicians : " + matchingTechnicians);

		return matchingTechnicians;
	}
	}