package com.straviso.ns.dispatchcontrollercore.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatchControllerConstants {

	public static final String FLAG_Y = "Y";
	public static final String FLAG_N = "N";
	
	public static final String YES = "Yes";
	public static final String NO = "No";

	public static final String GET_TICKET_COUNT_BY_STATUS_REQUEST = "Get Ticket Count By Status Request";
	public static final String GET_TICKET_COUNT_BY_ACTION_REQUEST = "Get Ticket Count By Action Request";
	public static final String GET_TICKET_COUNT_REQUEST = "Get Ticket Count Request";
	public static final String GET_TOP_TECHNICIAN_COUNT_REQUEST = "Get Top Technician Count Request";
	public static final String GET_TOP_TICKET_COUNT_REQUEST = "Get Top Ticket Count Request";
	public static final String GET_TECHNICIAN_COUNT_REQUEST = "Get Technician Count Request";
	public static final String GET_TOTAL_MANAGER_COUNT_REQUEST = "Get Total Manager Count Request";
	public static final String GET_TOTAL_SUPERVISER_COUNT_REQUEST = "Get Total Superwiser Count Request";
	public static final String GET_USERHIERARCHY_REQUEST = "Get UserHierarchy Request";
	public static final String GET_TICKET_COUNT_BY_STATEWISE_REQUEST="Get Ticket Count By Dashboard Request";
	public static final String GET_TICKET_DATA_REQUEST="Get Ticket Data By Ticket Number and Conversation id  Request";
	public static final String GET_TICKET_DATA_JSON_REQUEST="Get TicketData Json By Ticket Number and Conversation id  Request";
	

	//External API LogKeys
	public static final String SEND_TICKET_DETAILS_TO_DC = "Send Ticket Details to DC Request";
	public static final String ADD_TECHNICIAN_DETAILS_TO_DC = "Add Technician Details to DC Request";
	public static final String MY_TEAM_WORKLOAD = "My Team Workload Request";
	public static final String PTO_TOMMORROW_FEED = "ptoTomorrowFeed DC Request";
	public static final String PTO_WEEKLY_FEED = "ptoWeeklyFeed DC Request";
	public static final String UPDATE_TECHNICIAN_DETAILS_TO_DC = "Update Technician Details to DC Request";
	
	// Status
	public static final String STATUS_IN_PROGRESS = "In Progress";
	public static final String STATUS_COMPLETED = "Completed";
	public static final String STATUS_FAILED = "Failed";
	public static final String STATUS_SUCCESS = "Success";
	public static final String STATUS_ASSIGNED = "Assigned";
	public static final String STATUS_UNASSIGNED = "UnAssigned";
	public static final String STATUS_BACKTOQUEUE = "BackToQueue";
	public static final String STATUS_MISSING_INFO = "MissingInfo"; 
	public static final String STATUS_CANCELLED = "Cancelled";
	public static final String STATUS_PASTDUE = "PastDue";
	public static final String STATUS_TRANSFERED = "Transfered";
	public static final String STATUS_ALREADY = "Already";
	public static final String STATUS_FROM_AND_TO_TECHNICIAN_ARE_SAME = "FromAndToTechniciansAreSame";
	public static final String STATUS_RESCHEDULE = "Reschedule";
	public static final String STATUS_OPEN = "Open";
	public static final String STATUS_COMPLETE = "Complete";
	public static final String STATUS_MISSINGINFO = "Missing Info";
	public static final String STATUS_PARTIAL_SUCCESS = "PartialSuccess";
	public static final String STATUS_PAST_DUE = "PastDue";
	
	//Ticket Types
	public static final String TICKET_TYPE_EMERGENCY = "Emergency";
	public static final String TICKET_TYPE_NORMAL = "Normal";
	public static final String TICKET_TYPE_AFTERHRS = "AfterHrs";

	public static final String STATUS_TECHNICIAN_NOT_AVAILABLE = "Technician Not Available";
	public static final String STATUS_TO_TECHNICIAN_NOT_AVAILABLE = "toTechnician Not Available";
	public static final String STATUS_FROM_TECHNICIAN_NOT_AVAILABLE = " : Given ticket is not assigned ";
	public static final String STATUS_TICKET_CANCELLED = "Ticket has been already cancelled";
	public static final String STATUS_TICKET_ALREADY_TRANSFERED = "Ticket is already Transfered";
	public static final String STATUS_TICKET_IS_UNASSIGNED = "Ticket is UnAssigned"; 
	public static final String STATUS_TICKET_DATA_NOT_FOUND = "Ticket Record not found"; 
	public static final String STATUS_BACKTOQUEUE_SUCCESSFUL = "BackToQueue done Successfully";
	
	public static final String STATUS_TICKET_ALREADY_COMPLTED_CANCELLED = "Ticket is already Completed/Cancelled";
	
	

	// Response

	public static final String RESPONSE_OK = "Ok";
	public static final String RESPONSE_NOT_FOUND = "Not Found";
	public static final String RESPONSE_INTER_SERVER_ERROR = "Internal Server Error";
	public static final String RESPONSE_INCOMPLETE_REQUEST = "Incomplete Request";
	public static final String RESPONSE_BAD_REQUEST = "Bad Request";
	public static final String RESPONSE_SUBMITTED = "Submitted";
	public static final String RESPONSE_UPDATED = "Updated";
	public static final String RESPONSE_RECORD_ALREADY_PRESENT = "Record Already Present";
	public static final String RESPONSE_SUPERVISOR_NOT_FOUND = "Requested Supervisor Not Found";
	public static final String RESPONSE_TECHNICIAN_NOT_FOUND = "Requested Technician Not Found";
	public static final String SUPERVISOR_NOT_FOUND = "Supervisor Not Found";
	public static final String TICKETS_TRANSFERRED ="Tickets transferred successfully";


	// Collection

	public static final String TICKET_COLLECTION = "ticketDCSolver";
	public static final String TECHNICIAN_DC_SOLVER_COLLECTION = "technicianDCSolver";
	public static final String TECHNICIAN_SEARCH_COLLECTION = "technicianSearchCollection";
	public static final String TICKET_SEARCH_COLLECTION = "ticketSearchCollection";
	public static final String USER_HIERARCHY = "userHierarchy";
	public static final String TECHNICIAN_ASSIGNMENT_SOLUTION = "technicianAssignmentSolution";
	public static final String LUMEN_COLLECTION_NAME_ENTITY_DATA = "TicketMaster";
	public static final String COLLECTION_NAME_TICKET_AUDIT_TRAILS = "TicketAuditTrails";
	public static final String LUMEN_COLLECTION_NAME_SUBTICKETMASTER = "SubTicketMaster";

	// Collection field constant

	public static final String FIELD_CREATED_DATETIME = "createdDateTime";
	public static final String FIELD_GLOBAL_STATUS = "globalStatus";
	public static final String FIELD_ACTION_STATUS = "actionOnTicket";
	public static final String FIELD_TICKET_NO = "ticketNumber";
	public static final String FIELD_CITY = "city";
	public static final String FIELD_CUSTOMER_CITY = "customerCity";
	public static final String FIELD_AVAILABILITY_STATUS = "availabilityStatus";
	public static final String FIELD_TECHINICAIN_AVAILABILITY_STATUS = "technicianAvailability";
	public static final String FIELD_TOTAL = "Total";
	public static final String FIELD_ISACTIVE = "isActive";
	public static final String FIELD_AGENT_TYPE= "agentType";
	public static final String FIELD_CONFIG_ID = "_id";
	public static final String FIELD_CONSTRAINT_ID = "constraintId";
	public static final String FIELD_CONSTRAINT_NAME = "constraintName";
	public static final String FIELD_CONSTRAINT_TYPE = "constraintType";
	public static final String FIELD_CONSTRAINT_SCORE = "constraintScore";
	public static final String FIELD_CREATED_DATE = "createdDate";
	public static final String FIELD_UPDATED_DATE = "updatedDate";
	public static final String FIELD_CREATED_BY = "createdBy";
	public static final String FIELD_UPDATED_BY = "updatedBy";
	public static final String FIELD_CONSTRAINT_TITLE = "constraintTitle";
	public static final String FIELD_TICKETLIST_IS_ASSIST_TICKET = "ticketList.isAssistTicket";
	public static final String FIELD_COMPLETION_DATE_TIME = "completionDateTime";
	
	public static final String FILTER_TICKET_STATUS = "Status";
	
	public static final String LUMEN_COLLECTION_ID="_id";
	public static final String LUMEN_COLLECTION_SHAPEFILE="ShapeFile";
	public static final String LUMEN_COLLECTION_ADDITIONALBODY="AdditionalBody";
	public static final String LUMEN_COLLECTION_BODY="Body";

	public static final String LUMEN_COLLECTION_TICKET_NUMBER = "ticketExternalId";
	public static final String LUMEN_COLLECTION_CONVERSATION_ID = "conversationId";
	public static final String LUMEN_COLLECTION_ASSIGNMENTDETAILS = "assignmentDetails";
	public static final String LUMEN_COLLECTION_TICKET_ASSIGNED_TECHNICIAN_ID= "assignedTechnicianId";
	public static final String LUMEN_COLLECTION_TICKET_STATUS= "ticketStatus";
	public static final String LUMEN_COLLECTION_TICKET_STAGE= "ticketStage";
	public static final String LUMEN_COLLECTION_ORIGINAL_TICKET_CONTENT="originalTicketContent";
	public static final String LUMEN_COLLECTION_ADDITIONAL_INFO="additionalInfo";
	public static final String LUMEN_COLLECTION_TICKET_SUPERVISOR_ID= "supervisorId";
	
	
	public static final String LUMEN_COLLECTION_GLOBAL_STATUS = "assignmentDetails.ticketStatus";
	public static final String LUMEN_COLLECTION_ACTION_ON_TICKET = "assignmentDetails.actionOnTicket";
	public static final String LUMEN_COLLECTION_TECHNICIAN_ID = "assignmentDetails.technicianId";
	public static final String LUMEN_COLLECTION_TECHNICIAN_FIRST_NAME = "assignmentDetails.technicianFirstName";
	public static final String LUMEN_COLLECTION_TECHNICIAN_LAST_NAME = "assignmentDetails.technicianLastName";
	public static final String LUMEN_COLLECTION_TECHNICIAN_EMAIL_ID = "assignmentDetails.technicianEmailId";
	public static final String LUMEN_COLLECTION_SUPERVISOR_NAME = "assignmentDetails.supervisorName";
	public static final String LUMEN_COLLECTION_SUPERVISOR_ID = "assignmentDetails.supervisorId";
	public static final String LUMEN_COLLECTION_SUPERVISORPOLYGON_ID = "assignmentDetails.supervisorPolygonId";
	public static final String LUMEN_COLLECTION_ASSIGNMENT_DATE_TIME = "assignmentDetails.assignmentDateTime";
	
	public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL ="assignmentDetails.ticketActionTrails";
	
	public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ACTION ="action";
    public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ACTION_BY = "actionBy";
    public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_AUDIT_DATETIME ="auditDateTime";
    public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_PRE_ACTION ="preAction";
    public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_POST_ACTION = "postAction";
    public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_REMARKS = "remarks";
    public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_ERROR = "error";
    public static final String LUMEN_COLLECTION_TICKET_ACTION_TRAIL_TICKET_STAGE = "ticketStage";
    
    public static final String AGENT_AVAILABILITY_CALENDAR_DATE = "calendarDate";
    
    //Audit Messages
    public static final String AUDIT_MESSAGE_AFTER_HRS_ASSIGNMENT = "After Hrs Ticket Assignment at Assignment Stage";
    public static final String AUDIT_MESSAGE_DC_SOLVER = "Scheduled Assignment through Assignment Engine";
    public static final String AUDIT_MESSAGE_TRANSFERRED = "Transferred By ";
    public static final String AUDIT_MESSAGE_CANCELLED = "Cancelled By ";
    public static final String AUDIT_MESSAGE_BACKTOQUEUE = "Moved Back To Queue By ";
   
    //Ticket Stages
  	public static final String STAGE_ASSIGNMENT = "Assignment";
  	public static final String TICKET_STAGE_ASSIGNMENT_STARTED= "AssignmentStarted";
  	public static final String TICKET_STAGE_ASSIGNMENT_COMPLETED= "AssignmentCompleted";
  	public static final String TICKET_STAGE_ENRICHMENT_COMPLETED= "EnrichmentCompleted";
    
	//AssignmentSolution collection constant fields
	public static final String FIELD_AGENTTECHNICIANID = "agent.technicianId";
	public static final String FIELD_AGENT_AGENTTYPE = "agent.agentType";
	public static final String FIELD_AGENT_ISACTIVE = "agent.isActive";
	public static final String FIELD_AGENT_TICKETLIST_TICKETDUEDATETIME = "agent.ticketList.ticketDueDateAndTime";
	public static final String AS_TICKET_NO = "agent.ticketList.ticketNumber";
	public static final String FIELD_TECHNICIANID = "technicianId";
	public static final String TECHNICIAN_FIRSTNAME = "technicianFirstName";
	public static final String TECHNICIAN_LASTNAME = "technicianLastName";
	public static final String TECHNICIAN_EMAILID = "technicianEmailId";
	public static final String SUPERVISOR_NAME = "supervisorName";
	public static final String SUPERVISORID = "supervisorId";
	public static final String ACTION = "ticketActionTrail.action";
	public static final String ACTION_BY = "ticketActionTrail.actionBy";
	public static final String ACTION_ON = "ticketActionTrail.actionOn";
	public static final String PRE_ACTION = "ticketActionTrail.preAction";
	public static final String POST_ACTION = "ticketActionTrail.postAction";
	public static final String AS_TICKETLIST = "agent.ticketList";
	public static final String TICKETLIST = "ticketList";
	public static final String TICKETLIST_TICKET_NO = "ticketList.ticketNumber";
	public static final String COUNTY_NAME="name";
	public static final String COUNTY_TICKET_COUNT="value";
	public static final String STATE_NAME="name";
	public static final String STATES_TICKET_COUNT="value";
	public static final String STATES_DRILLDOWN="drilldown";
	public static final String STATES_DATA="data";
	public static final String EMERGENCY="Emergency";
	public static final String TIMESTAMP="timestamp";
	
	public static final String MASTER_TICKET_EXTERNALID="masterTicketExternalId";
	public static final String EMERGENCY_FLAG="emergencyFlag";
	public static final String AFTER_HOURS="afterHours";
	public static final String IS_ASSIST_TICKET="isAssistTicket";
	public static final String MULTI_TECHNICIAN_TICKET="multiTechnicianTicket";
	public static final String IS_FIRST_TICKET="isFirstTicket";
	
	
	
	//ticketdcsolver collection constant fields
	public static final String CONVERSATIONID="conversationId";
	public static final String TICKETNUMBER="ticketNumber";
	public static final String TICKETNUMBER_811="ticketNumber811";
	public static final String POLYGON_ID="polygonId";
	public static final String TICKETETA="ticketETA";
	public static final String CERTIFICATION_REQUIRED="certificationRequired";
	public static final String CERTIFICATION_LIST="certificationList";
	public static final String TICKETSCORE="ticketScore";
	public static final String TICKETDUEDATEANDTIME="ticketDueDateAndTime";
	public static final String CREATEDDATEANDTIME="createDateAndTime";
	public static final String CREATEDDATETIME="createdDateTime";
	
	
	public static final String SUPERVISORNAME="supervisorName";
	public static final String LOCATION="location";
	public static final String LOCATION_LATITUDE="location.latitude";
	public static final String LOCATION_LONGITUDE="location.longitude";
	public static final String TICKETTYPE="ticketType";
	public static final String TICKETSTATUS="ticketStatus";
	public static final String TICKETPRIORITY="ticketPriority";
	public static final String WORKTYPE="workType";
	public static final String WORKCITY="workCity";
	public static final String WORKSTATE="workState";
	public static final String WORKCOUNTY="workCounty";
	public static final String WORKSTREET="workStreet";
	public static final String WORKADDRESS="workAddress";
	public static final String WORKZIP="workZip";
	public static final String TECHNICIANID="technicianId";
	public static final String TECHNICNAFIRSTNAME="technicianFirstName";
	public static final String TECHNICIANLASTNAME="technicianLastName";
	public static final String TECHNICIANEMAILID="technicianEmailId";
	
	public static final String TICKET_UPDATED="Ticket Updated";
	public static final String ENRICHMENT_BOT=" Enrichment Bot";
	
	public static final Integer DEFAULT_AVAILABLETIME = 600; // IN Mintues (10 Hrs Default Technician WorkHour)
	public static final long DEFAULT_EMERGENCY_ASSIGNMENT_THREAD_SLEEP = 20000;  // in milliseconds

	public static final String FIELD_FIRSTNAME="firstName";
	public static final String FIELD_LASTNAME="lastName";
	public static final String FIELD_EMAILID="emailId";	
	public static final String FIELD_JOBTITLE="jobTitle";
	public static final String FIELD_COUNTY="county";
	public static final String FIELD_STATE="state";
	public static final String FIELD_VEHICLETYPE="vehicleType";
	public static final String FIELD_TECHNICIANSCORE="technicianScore";
	public static final String ADD_SUPERVISOR_DETAILS_TO_DC = "Add Supervisor Details to DC Request";
	public static final String UPDATE_SUPERVISOR_DETAILS_TO_DC = "Update Supervisor Details to DC Request";
	
	public static final String FIELD_MANAGERNAME="managerName";
	public static final String FIELD_MANAGERID="managerId";
	public static final String FIELD_SUPERVISOREXPLEVEL="supervisorExplevel";
	public static final String FIELD_SUPERVISORAVAILABILITY="supervisorAvailability";
	public static final String FIELD_SUPERVISORPOLYGONID="supervisorPolygonId";
	public static final String FIELD_POLYGONLIST="polygonList";
	public static final String FIELD_AVAILABILITYSTATUS="availabilityStatus";
	
	public static final String GET_TECHNICIAN_DATA_JSON_REQUEST="Get TechnicianData Json By TechnicianID Request";
	public static final String GET_SUPERVISOR_DATA_JSON_REQUEST="Get SupervisorData Json By SupervisorID Request";
	
	
	public static final String SUPERVISOR_POLYGONMAPPING_DCSOLVER = "supervisorPolygonMappingDCSolver";



	
	public static final String FIELD_AVAILABLETIME="availableTime";

	public static final String SUPERVISORPOLYGONID="supervisorPolygonId";




	public static final String FIELD_ASSIGNMENTDATETIME= "assignmentDateTime";
	
	//UserHierrarchy filed names 
		public static final String SUPERVISOR = "Supervisor";
		public static final String MANAGER = "Manager";
		public static final String DESIGNATION = "userHierarchy.data.designation";
		public static final String FIELD_ID = "_id";

	public static final String DEFAULT_TIMEZONE = "America/Chicago";
	public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_DATE_FORMAT = "MM-dd-yyyy";
	public static final String DEFAULT_TIME_FORMAT = "HH:mm";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DEFAULT_TECHNICIAN_ID = "99909090";
	
	public static final String WORD_COUNT = "count";
	public static final String WORD_TOTAL = "Total";
	public static final String TECHNICIAN_NOT_AVAILABLE = "Technician is not available";
	public static final String NO_DATA_AVAILABLE = "No data available";
	public static final String FAILED_EXECUTE_QUERY = "Unable to execute Query";

	public static final String STATUS_TRUE = "True";
	public static final String STATUS_FALSE = "False";
	public static final String AGENT_TYPE_ACTUAL = "Actual";
	public static final String AGENT_TYPE_VIRTUAL = "Virtual";
	public static final String TICKET_NOT_AVAILABLE = "Ticket is not available";
	public static final String STATUS_AVAILABLE = "Available";
	public static final String STATUS_ACTIVE = "Y";
	public static final String APP_STATUS_TRUE = "true";
	public static final String APP_STATUS_FALSE = "false";
	
	public static final String WORD_ACTIVE = "Active";

	public static final Integer DEFAULT_PAGE_SIZE = 20;
	public static final Integer DEFAULT_PAGE_NUMBER = 0;

	public static final String SEARCH_COLLECTION_TICKET = "ticket";
	public static final String SEARCH_COLLECTION_TECHNICIAN = "technician";

	public static final String INVALID_SEARCH_STRING = "Invalid Search String";

	public static final String OPERATOR_EQUALS = "equals";
	public static final String OPERATOR_NOT_EQUALS = "not equals";
	public static final String OPERATOR_CONTAINS = "contains";
	public static final String OPERATOR_IN = "in";


	public static final String TICKETS_UPDATED = "Tickets Updated";
	public static final String TICKETS_NOT_UPDATED = "Tickets are not Updated";
	public static final String TICKETS_CANCELLED = "Ticket Cancellation Successful";
	public static final String TICKETS_NOT_CANCELLED = "Agent details not found";
	
	
	public static final String STATUS_TICKET_NOT_AVAILABLE = "Ticket Not Available";
	public static final String STATUS_TICKET_UNASSIGNED = "Ticket is already UnAssigned";
	
	public static final String START_DATETIME_APPEND = " 00:00:00";
	public static final String START_DATETIME_APPEND_PTO = " 06:00:02";
	public static final String END_DATETIME_APPEND = " 23:59:59";
	
	public static final String STATUS_CODE_OK = "200";
	public static final String STATUS_CODE_INTERNAL_SERVER_ERROR = "500";
	public static final String STATUS_CODE_BAD_REQUEST = "404";
	
	

	public static final String TO = "To";
	public static final String BY = "By";
	public static final String CALL_TYPE = "Dynamic";
	public static final String SOURCE_APP = "DispatchController";
	
	public static final String TRANSFER_IN_DC = "Transfer API Called from DC Request";
	public static final String TRANSFER_IN_APK = "Transfer API Called from APK Request";
	
	public static final String TICKET_SEQUENCE_lIST= "Ticket Sequence List By TechId";

	public static final List<String> longFieldList = new ArrayList<>();

	static {
		longFieldList.add("polygonId");
		longFieldList.add("agent.polygonId");
		longFieldList.add("agent.technicianExplevel");

	}
	
	
	public static final List<String> dateFieldList = new ArrayList<>();

	static {
		dateFieldList.add("assignmentDateTime");
		dateFieldList.add("ticketDueDateAndTime");
		dateFieldList.add("createdDateTime");
		dateFieldList.add("timestamp");

	}
	
	public static final List<String> TICKETSTATUSLIST = new ArrayList<>();

	static {
		TICKETSTATUSLIST.add(STATUS_ASSIGNED);
		TICKETSTATUSLIST.add(STATUS_UNASSIGNED);
		TICKETSTATUSLIST.add(STATUS_MISSING_INFO);

	}
	
	public static final List<String> TICKETACTIONLIST = new ArrayList<>();

	static {
		TICKETACTIONLIST.add(STATUS_TRANSFERED);
		TICKETACTIONLIST.add(STATUS_CANCELLED);
		TICKETACTIONLIST.add(STATUS_BACKTOQUEUE);

	}

	public static final String USER_TYPE_USER = "User";
	public static final String RESPONSE_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String FILE_DATETIME_FORMAT = "yyyyMMddHHmmssSSS";

	public static final String ASSIGNMENT_STATUS_UNDER_ASSIGNED = "UnderAssigned";
	public static final String ASSIGNMENT_STATUS_OVER_ASSIGNED = "OverAssigned";
	public static final String ASSIGNMENT_STATUS_IDEAL_ASSIGNED = "IdealAssignment";


	public static final String RS_DETAILS_ROAD_CLASS = "road_class"; 
	public static final String RS_DETAILS_ROAD_ENVIRONMENT = "road_environment"; 
	public static final String RS_DETAILS_MAX_SPEED = "max_speed"; 
	public static final String RS_DETAILS_AVG_SPEED = "average_speed"; 

	public static final String PROFILE_CAR = "car";
	public static final String LOCALE_EN_US = "en_US";
	public static final String FLAG_FALSE = "false"; 

	public static final long DEFAULT_AGENT_TRAVEL_TIME = 1800000;   // in MilliSeconds

	public static List<String> getRSDetailsList(){
		return RS_DETAILS_LIST;
	}



	protected static final List<String> RS_DETAILS_LIST = new ArrayList<>();
	static {
		RS_DETAILS_LIST.add(RS_DETAILS_ROAD_CLASS);
		RS_DETAILS_LIST.add(RS_DETAILS_ROAD_ENVIRONMENT);
		RS_DETAILS_LIST.add(RS_DETAILS_MAX_SPEED);
		RS_DETAILS_LIST.add(RS_DETAILS_AVG_SPEED);



	}

	public static final String RS_SNAP_PREV_FERRY = "ferry"; 

	protected static final List<String> RS_SNAP_PREV_LIST = new ArrayList<>();

	public static final String FROM_SUPERVISOR_NOT_PRESENT = "From SupervisorId not present";

	public static final String TO_SUPERVISOR_NOT_PRESENT = "To SupervisorId not present";
	
	public static final String AGENT_AVAILABILITY_STATUS_MULTIDAY_TICKET = "MultiDayTicket";

	public static final String AGENT_AVAILABILITY_STATUS_SPECIAL_ASSIGNMENT = "SpecialAssignment";

	
	// Ticket Stage
	public static final String ACTION_ASSIGNMENT = "Assignment";
	
	//Keywords
	public static final String KEYWORD_SYSTEM_AFTER_HRS_TICKET_ASSIGNMENT = "SystemDCAfterHrsAssignment";
	public static final String KEYWORD_SYSTEM_INITIAL_ASSIGNMENT = "SystemDCInitialAssignment";
	static {
		RS_SNAP_PREV_LIST.add(RS_SNAP_PREV_FERRY);
	}

	public static List<String> getRSSnapPreventionsList(){
		return RS_SNAP_PREV_LIST;
	}
	
	
	//FCM Notification Title & Message

    public static final String FCM_SUPERVISOR_TITLE = "Emergency Ticket: {ticketId}";
    public static final String FCM_SUPERVISOR_MESSAGE = "Emergency Ticket {ticketId} has been assigned to {technicianFullName}";
    public static final String FCM_TECHNICIAN_TITLE = "Emergency Ticket: {ticketId}";
    public static final String FCM_TECHNICIAN_MESSAGE = "You have been assigned an Emergency Ticket: {ticketId}";
    public static final String FCM_PLACEHOLDER_TICKETID = "{ticketId}";
    public static final String FCM_PLACEHOLDER_TECHNICIANFULLNAME = "{technicianFullName}";
    //Job Title

    public static final String JOB_TITLE_TECHNICIAN = "Technician";
    public static final String JOB_TITLE_SUPERVISOR = "Supervisor";
	public static final String ALL = "All";
	public static final String GAS = "Gas";
	public static final String CALENDERDATE = "calenderDate";
	public static final String FIELD_PROJECTTIME = "projectTime";
	public static final String ONCALL_STARTDATETIME = "onCallStartDateTime";
	public static final String ONCALL_ENDDATETIME = "onCallEndDateTime";
	public static final String ISONCALL = "isOnCall";
	public static final String JOBTITLE = "jobTitle";
	public static final String TECHNICIAN_NAME = "technicianName";
	public static final String FIRST_TICKET_ASSIGNMENT = "firstTicketAssigment";
	
	protected static final List<String> PAST_DUE_FETCH_STATUS_LIST = new ArrayList<>();
    static {

        PAST_DUE_FETCH_STATUS_LIST.add(STATUS_PASTDUE);

        PAST_DUE_FETCH_STATUS_LIST.add(STATUS_UNASSIGNED);

    }
    public static List<String> getPastDueFetchStatusList(){
        return PAST_DUE_FETCH_STATUS_LIST;

    }
	
	
	public static final String AUTHORIZATION_CONSTANT = "Authorization";
	public static final String BEARER_CONSTANT = "Bearer ";
	public static final String BASIC_CONSTANT = "Basic ";
	public static final String JWT_TOKEN_CONSTANT = "JWTtoken";
	public static final String FAILED = "Failed";
	public static final Integer DEFAULT_NUMBER_OF_THREADS = 100;
	
	public static final Integer RESPONSE_INTER_SERVER_ERROR_CODE = 500;
	
	
	public static final Integer HOURS_INT = 24;
	public static final String TIME_FORMAT_T = "T";
	public static final String TIME_FORMAT_Z = ".000Z";
	public static final String ZERO_STRING = "0";
	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'-05:00'";
	public static final String DATETIME_FORMAT_FILE_NAME = "MMddyyyyHHmmss";
	public static final String PARSE_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final Long ELASTIC_OFFSET = 0L;
	public static final String TIME_ZONE_UTC = "UTC";
	public static final String TIME_ZONE_CST = "America/Chicago";
	public static final String[] TICKET_EXPORT_COLUMNS = {"ticket_number","ticket_id","tickettype","work_type","ticket_score","technician_score","est_ticket_duration","emergency","assignment_date","due_date","closed_date","tech_name","usr_id","emp_id","supervisor","sup_group","district","state","received_date","City","route_index","start_type","start_lat","start_lon","eval_miles","available_capacity","assignment_eta","ticket_number811"};
	public static final String[] COCKPIT_TICKET_EXPORT_COLUMNS = {"Ticket Id","Work Type","Ticket Score","TIme Estimate(Minutes)","Work Address","Work Street","Work Zip","Work City","Work State","Ticket Type","Status","Technician Name","Technician Id","Supervisor Name","Supervisor Id","Due Date","Created Date","Assignment DateTime","Ticket Number811"};
	public static final String[] TICKET_TECHNICIAN_EXPORT_COLUMNS = {"ticket_id","tickettype","work_type","ticket_score","ticket_status","technician_score","est_ticket_duration","emergency","ticket_created_date","assignment_date","due_date","closed_date","received_date","tech_id","tech_name","usr_id","emp_id","sup_id","supervisor","sup_group","district","state","City","route_index","start_type","start_lat","start_lon","eval_miles","available_capacity","assignment_eta","ticket_number811","skill","ticket_priority","drive_time","total_drive_time","distance"};
	
	public static final String NO_RECORD = "No Record";
	public static final Integer RESPONSE_NO_DATA_FOUND_CODE = 404;
	public static final String FILE_TOO_LARGE = "File Too Large";
	public static final Integer RESPONSE_FILE_TOO_LARGE_CODE = 413;
	public static final String FILE_EXTENSION_CSV = ".csv";
	public static final Integer RESPONSE_OK_CODE = 200;
	public static final String FILE_NOT_FOUND = "File Not Found";
	public static final Integer SERVICE_CODE_103 = 103;
	public static final String SERVICE_CODE_103_MESSAGE = "Update";
	public static final Integer MAX_RECORD_FETCH_LIMIT = 500000;
	
	//Response Status Code
		public static final Integer STATUS_OK = 200;
		public static final Integer STATUS_CODE_SUBMITTED = 202;
		public static final Integer STATUS_BAD_REQUEST = 400;
		public static final Integer STATUS_CODE_NOT_FOUND = 404;
		public static final Integer STATUS_CODE_INTERNAL_ERROR = 500;
		public static final String OPERATOR_IS = "Is";
		public static final String OPERATOR_IS_NOT = "Is Not";

		//Buzz Notification Constants

	    public static final String BUZZ_PN_SENDER_ID = "System DC Emergency Assignment";

	    public static final String BUZZ_PN_CREATION_APPLICATION = "SystemDCS";

	    public static final String BUZZ_PN_SUPERVISOR_PROCESS_NAME = "EmergencyNotificationSupervisor";

	    public static final String BUZZ_PN_SUPERVISOR_PROCESS_CODE = "NM0004";

	    public static final String BUZZ_PN_SUPERVISOR_TEMPLATE_NAME = "Emergency Ticket Supervisor";

	 

	    public static final String BUZZ_PN_TECHNICIAN_PROCESS_NAME = "EmergencyNotificationTechnician";

	    public static final String BUZZ_PN_TECHNICIAN_PROCESS_CODE = "NM0005";

	    public static final String BUZZ_PN_TECHNICIAN_TEMPLATE_NAME = "Emergency Ticket Technician";

	    public static final String BUZZ_PN_FCM_FLAG = "1";

	    

	    public static final String BUZZ_PN_PLACEHOLDER_TICKETID = "ticketId";

	    public static final String BUZZ_PN_PLACEHOLDER_TECHNICIANFULLNAME = "technicianFullName";

	    

	    public static final String BUZZ_CHANNEL_PN = "PN";

	    public static final List<String> BUZZ_NOTIFICATION_CHANNELS = new ArrayList<>();
	    
	  //Robo Caller Bot Request

		

		public static final String LUMEN_BOT_PARAMETER_TICKET_CONVERSATION_ID = "ticketConversationId";

		public static final String LUMEN_BOT_PARAMETER_TICKET_EXTERNAL_ID = "ticketExternalId";

		public static final String LUMEN_BOT_PARAMETER_TECHNICIAN_ID = "technicianId";

		public static final String LUMEN_BOT_PARAMETER_TECHNICIAN_NAME = "technicianName";

		public static final String LUMEN_BOT_PARAMETER_TECHNICIAN_PHONE_NUMBER = "technicianPhoneNumber";

		public static final String LUMEN_BOT_PARAMETER_SUPERVISOR_ID = "supervisorId";

		public static final String LUMEN_BOT_PARAMETER_SUPERVISOR_NAME = "supervisorName";

		public static final String LUMEN_BOT_PARAMETER_SUPERVISOR_PHONE_NUMBER = "supervisorPhoneNumber";

		

		public static final String LUMEN_BOT_AFTER_HRS= "AfterHrs Ticket";

		public static final String LUMEN_BOT_ORIGIN_ORIGINATING_APP_SELF_TRIGGERED = "SelfTriggered";

		public static final String LUMEN_BOT_ORIGIN_CHANNEL_SM = "SM";

	    static {

	        BUZZ_NOTIFICATION_CHANNELS.add(BUZZ_CHANNEL_PN);

	    }

	    

	    public static List<String> getBuzzNotificationChannels(){

	        return BUZZ_NOTIFICATION_CHANNELS;

	    }
	  //Buzz Notification Constants
	    public static final String BUZZ_PN_SUPERVISOR_PROCESS_NAME_UNASSIGNED = "EmergencyUnAssignedNotification";

		public static final String BUZZ_PN_SUPERVISOR_PROCESS_CODE_UNASSIGNED = "NM0014";

		public static final String BUZZ_PN_SUPERVISOR_TEMPLATE_NAME_UNASSIGNED = "Emergency UnAssigned Ticket";

		public static final String PROCESS_ID="dcSolverProcessId";
		public static final String DC_TICKET_SAVE = "DC Ticket Save";
		public static final String REQUEST_RECEIVED_FROM_ENRICHMENT_BOT = "Request received from enrichment bot";

	
		public static List<String> EXCLUDE_GLOBAL_STATUS_LIST = new ArrayList<>();
		
		static {

			EXCLUDE_GLOBAL_STATUS_LIST.add(STATUS_PASTDUE);
			EXCLUDE_GLOBAL_STATUS_LIST.add(STATUS_COMPLETE);

	    }

		public static List<String> getExcludeGlobalStatusList(){
			return EXCLUDE_GLOBAL_STATUS_LIST;
		}

		public static List<String> INCLUDE_GLOBAL_STATUS_LIST = new ArrayList<>();
		
		static {

			INCLUDE_GLOBAL_STATUS_LIST.add(STATUS_ASSIGNED);
			INCLUDE_GLOBAL_STATUS_LIST.add(STATUS_UNASSIGNED);
			INCLUDE_GLOBAL_STATUS_LIST.add(STATUS_MISSING_INFO);
			INCLUDE_GLOBAL_STATUS_LIST.add(STATUS_RESCHEDULE);
			INCLUDE_GLOBAL_STATUS_LIST.add(STATUS_CANCELLED);

	    }

		public static List<String> getIncludeGlobalStatusList(){
			return INCLUDE_GLOBAL_STATUS_LIST;
		}
		
		public static List<String> INCLUDE_OPEN_GLOBAL_STATUS_LIST = new ArrayList<>();
		
		static {

			INCLUDE_OPEN_GLOBAL_STATUS_LIST.add(STATUS_ASSIGNED);
			INCLUDE_OPEN_GLOBAL_STATUS_LIST.add(STATUS_RESCHEDULE);

	    }

		public static List<String> getIncludeOpenGlobalStatusList(){
			return INCLUDE_OPEN_GLOBAL_STATUS_LIST;
		}

//		HttpServeletRequest-to print transactionId 
		public static final String TRANSCATIONID = "transactionId";
		public static final String TICKET_STORED = "Ticket Stored";
		public static final String STICKY_ROUTING = "StickyRouting";
		public static final String SYSTEM_DC = "SystemDCC";	
		
		public static final String STATUS_ENROUTE = "Enroute";
		public static final String STATUS_ONSITE = "Onsite";
		public static final String STATUS_JOBCLOSEOUT = "Jobcloseout";
		public static final String STATUS_PAUSE = "Pause";
		
//		SFTP CONFIG CONSTANT
		public static final String SFTP_HOST = "172.176.229.42";
		public static final int SFTP_PORT = 22;
		public static final String SFTP_USERNAME = "adminstraviso";
		public static final String SFTP_PASSWORD = "TsAdmin2018@!";
		public static final String SFTP_REMOTE_DIRECTORY = "/home/usic/Qliksense";
		public static final String SFTP_FILE_NAME="employee_scores_";
		
		//DC Services
		public static final String ROLE_DC_CORE = "DCCore";
		
//		SYS_CONFIG_
		public static final String SYS_CONFIG_DEFAULT_SFTP_HOST = "sftpHostName";
		public static final int SYS_CONFIG_DEFAULT_SFTP_PORT = 22;
		public static final String SYS_CONFIG_DEFAULT_SFTP_USERNAME = "sftpUsername";
		public static final String SYS_CONFIG_DEFAULT_SFTP_PASSWORD = "sftpPassword";
		public static final String SYS_CONFIG_DEFAULT_SFTP_REMOTE_DIRECTORY = "sftpPath";
		public static final String SYS_CONFIG_DEFAULT_WORKDAY_URL="workDayUrl";
		public static final String SYS_CONFIG_DEFAULT_CONTINOUS_SERVICE_DATE="Continuous_Service_Date";
		public static final String SYS_CONFIG_DEFAULT_MOST_RECENT_HIRE_DATE="Most_Recet_Hire_Date";
		public static final String SYS_CONFIG_DEFAULT_EMPLOYEE_QUALITIY_SCORE="employee_quality_score";
		public static final String SYS_CONFIG_DEFAULT_SFTP_FILE_NAME="sftpFileName";
			
	}
