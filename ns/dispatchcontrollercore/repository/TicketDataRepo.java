package com.straviso.ns.dispatchcontrollercore.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.straviso.ns.dispatchcontrollercore.entity.Ticket;

@Repository
public interface TicketDataRepo extends MongoRepository<Ticket, String> {

	Page<Ticket> findByGlobalStatusAndCreatedDateTimeBetween(String globalStatus, LocalDateTime startDate,
			LocalDateTime endDate, Pageable pageable);

	Page<Ticket> findByCreatedDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	
	Ticket findByTicketNumber(String TicketNumber);
	
	Ticket findByTicketNumberAndConversationId(String TicketNumber,String conversationId);

	boolean existsByTicketNumber(String ticketNumber);

	Page<Ticket> findBySupervisorIdAndCreatedDateTimeBetween(String supervisorId,LocalDateTime withSecond,LocalDateTime TowithSecond, Pageable processPage);

	boolean existsByTicketNumberAndConversationId(String ticketNumber, String conversationId);

	List<Ticket> findByCreatedDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
	
	@Query("{" +
            "'supervisorId': ?0, " +
            "'ticketDueDateAndTime': { $gte: ?1, $lte: ?2 }" +
            "}")
	Page<Ticket> findBySupervisorIdAndDueDateBetween(String supervisorId, LocalDateTime withSecond,LocalDateTime TowithSecond, Pageable processPage);
	
	@Query("{" +
            "'ticketDueDateAndTime': { $gte: ?0, $lte: ?1 }" +
            "}")
	Page<Ticket> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	
	@Query("{" +
			"'globalStatus': {$nin : ?0}, " +
			"'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?1, $lte: ?2 }" +
            "}")
	Page<Ticket> findBy(String[] statusPastdue,LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	
	@Query("{" +
			 "'globalStatus': ?0, " +
			 "'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?1, $lte: ?2 }" +
            "}")
	Page<Ticket> findByGlobalStatusAndDueDateBetween(String globalStatus, LocalDateTime startDate,
			LocalDateTime endDate, Pageable pageable);
	
	Long countBySupervisorIdAndGlobalStatusAndCreatedDateTimeBetween(String supervisorId, String statusAssigned,
			LocalDateTime startDate, LocalDateTime endDate);
	
	  @Query("{"
	            + " 'ticketDueDateAndTime': {$gte: ?1, $lte: ?2},"
	            + " 'isAssistTicket':'No',"
	            + " '$or': ["
	            + "   {"
	            + "     '$and': ["
	            + "       {'globalStatus': ?4},"
	            + "       {'completionDateTime': {$gte: ?5}}"
	            + "     ]"
	            + "   },"
	            + "   {'globalStatus': {$in: ?3}}"
	            + " ],"
	            + " 'technicianId': ?0"
	            + "}")
	Page<Ticket> findByTechnicianIdAndTimestampBetween(
			String technicianId , LocalDateTime startDateTime,
			LocalDateTime endDateTime,String[] strings, String statusComplete, LocalDateTime ldt_start, Pageable pageable);

	  @Query("{"
	            + " 'ticketDueDateAndTime': {$gte: ?1, $lte: ?2},"
	            + " 'isAssistTicket':'No',"
	            + " '$or': ["
	            + "   {"
	            + "     '$and': ["
	            + "       {'globalStatus': ?4},"
	            + "       {'completionDateTime': {$gte: ?5}}"
	            + "     ]"
	            + "   },"
	            + "   {'globalStatus': {$in: ?3}}"
	            + " ],"
	            + " 'technicianId': ?0"
	            + "}")
	List<Ticket> findByTechnicianIdAndTimestampBetweenList(
			String technicianId , LocalDateTime startDateTime,
			LocalDateTime endDateTime,String[] strings, String statusComplete, LocalDateTime ldt_start);
	@Query("{" +
			"'globalStatus': ?0, " +
            "'supervisorId': ?1, " +
            "'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?2, $lte: ?3 }" +
            "}")
	Page<Ticket> findByGlobalStatusAndSupervisorIdAndDueDateBetween(String globalStatus, String supervisorId, LocalDateTime startDate,
			LocalDateTime endDate, Pageable processPage);

	Page<Ticket> findByGlobalStatus(String globalStatus, Pageable pageable);

	Page<Ticket> findBySupervisorId(String supervisorId, Pageable processPage);

	Page<Ticket> findByGlobalStatusAndSupervisorIdAndIsAssistTicket(String ticketStatus, String supervisorId,String isAssistTicket, Pageable processPage);

	Page<Ticket> findByGlobalStatusNot(String statusPastdue, Pageable pageable);

	@Query("{" +
			"'globalStatus': {$nin : ?1}, " +
			"'isAssistTicket':'No' "+
			"'supervisorId': ?0 " +
            "}")
	Page<Ticket> findBySupervisorIdAndGlobalStatusNot(String supervisorId, String[] statusPastdue, Pageable processPage);

	@Query("{" +
			"'globalStatus': {$nin : ?0}, " +
            "'supervisorId': ?1, " +
            "'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?2, $lte: ?3 }" +
            "}")
	Page<Ticket> findBySupervisorIdAndDueDateBetweenAndGlobalStatusNot(String[] strings, String supervisorId,
			LocalDateTime startDate, LocalDateTime endDate, Pageable processPage);

	Ticket findByMasterTicketExternalIdAndIsAssistTicket(String masterTicketExternalId, String no);

	Page<Ticket> findByMasterTicketExternalIdAndIsAssistTicket(String masterTicketExternalId, String no,
			Pageable processPage);

	//Ticket findByTicketNumberAndConversationId(String ticketNumber, String conversationId);

	boolean existsByTicketNumber(String ticketNumber, String conversationId);

	Page<Ticket> findByGlobalStatusIn(String[] strings, Pageable pageable);

	@Query("{" +
		   "'globalStatus': {$in : ?0}, " +
		   "'isAssistTicket':'No' "+
           "'ticketDueDateAndTime': { $gte: ?1, $lte: ?2 }" +
           "}")
	Page<Ticket> findByGlobalStatusInAndDueDateBetween(String[] strings, LocalDateTime startDate, LocalDateTime endDate,
			Pageable pageable);
	
	@Query("{" +
			"'globalStatus': {$in : ?0}, " +
            "'supervisorId': ?1, " +
            "'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?2, $lte: ?3 }" +
            "}")
	Page<Ticket> findByGlobalStatusInAndSupervisorIdAndDueDateBetween(String[] strings, String supervisorId,
			LocalDateTime startDate, LocalDateTime endDate, Pageable processPage);

	Page<Ticket> findByGlobalStatusInAndSupervisorIdAndIsAssistTicket(String[] strings, String supervisorId,String isAssistTicket, Pageable processPage);
	
	@Query("{" +
			"'globalStatus': {$nin : ?0}, " +
            "'createdDateTime': { $gte: ?1, $lte: ?2 }" +
            "}")
	Page<Ticket> findByCreatedDateTimeBetweenAndGlobalStatus(String[] statusPastdue,LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	
	@Query("{" +
			   "'globalStatus': {$in : ?0}, " +
	           "'createdDateTime': { $gte: ?1, $lte: ?2 }" +
	           "}")
		Page<Ticket> findByGlobalStatusInAndcreatedDateTime(String[] strings, LocalDateTime startDate, LocalDateTime endDate,
				Pageable pageable);
	
	@Query("{" +
			"'globalStatus': ?0, " +
           "'createdDateTime': { $gte: ?1, $lte: ?2 }" +
           "}")
	Page<Ticket> findByGlobalStatusAndcreatedDateTime(String globalStatus, LocalDateTime startDate,
			LocalDateTime endDate, Pageable pageable);

	Page<Ticket> findByGlobalStatusInAndIsAssistTicket(String[] strings,String isAssistTicket, Pageable pageable);

	Page<Ticket> findByGlobalStatusNotAndIsAssistTicket(String statusPastdue, String isAssistTicket, Pageable pageable);

	Page<Ticket> findByGlobalStatusAndIsAssistTicket(String globalStatus, String isAssistTicket, Pageable pageable);

	@Query("{" +
			   "'globalStatus': {$in : ?0}, " +
			   "'isAssistTicket':'No' "+
	           "'ticketDueDateAndTime': { $gte: ?3, $lte: ?4 }" +
	           "'completionDateTime': { $gte: ?2 }" +
	           "}")
	Page<Ticket> findByGlobalStatusAndIsAssistTicketAndCompletionDateTimeGreaterThanTest(String globalStatus, String no,
			LocalDateTime ldt_start, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

	Page<Ticket> findByGlobalStatusNotAndIsAssistTicketAndCompletionDateTimeGreaterThan(String statusPastdue, String no,
			LocalDateTime ldt_start, Pageable pageable);

	@Query("{" +
			"'globalStatus': {$nin : ?0}, " +
			 "'isAssistTicket':'No' "+
            "}")
	Page<Ticket> findByGlobalStatusNotAndIsAssistTicketWithoutCompleteStatus(String[] strings, String no,
			Pageable pageable);

	@Query("{" +
			"'globalStatus': {$nin : ?0}, " +
			"'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?1, $lte: ?2 }" +
            "}")
	List<Ticket> findByWithoutComplete(String[] strings, LocalDateTime startDate, LocalDateTime endDate);

	@Query("{" +
			"'globalStatus': ?0, " +
			"'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?1, $lte: ?2 }" +
            "'completionDateTime': { $gte: ?3 }" +
            "}")
	List<Ticket> findByCompleteDateTimeGreatherThan(String statusComplete, LocalDateTime startDate,
			LocalDateTime endDate, LocalDateTime ldt_start);
	
		

	@Query("{" +
			"'globalStatus':  ?0, " +
            "'supervisorId': ?1, " +
            "'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?2, $lte: ?3 }" +
            "'completionDateTime': { $gte: ?4 }" +
            "}")
	Page<Ticket> findByGlobalStatusInAndSupervisorIdAndDueDateBetweenAndCompleteDateTimeGreatherThan(
			String ticketStatus, String supervisorId, LocalDateTime startDate, LocalDateTime endDate,
			LocalDateTime ldt_start, Pageable processPage);
	
	@Query("{" +
			"'globalStatus': {$in : ?1}, " +
			"'isAssistTicket':'No' "+
			"'supervisorId': ?0 " +
			 "'completionDateTime': { $gte: ?2 }" +
            "}")
	Page<Ticket> findBySupervisorIdAndGlobalStatusAndCompletionDateTimeGreaterThan(String supervisorId, String[] statusPastdue,LocalDateTime ldt_start, Pageable processPage);

	@Query("{" +
			"'globalStatus': {$in : ?0}, " +
            "'supervisorId': ?1, " +
            "'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?2, $lte: ?3 }" +
            "'completionDateTime': { $gte: ?4 }" +
            "}")
	Page<Ticket> findBySupervisorIdAndDueDateBetweenAndGlobalStatusAndCompletionDateTimeGreaterThan(String[] strings,
			String supervisorId, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime ldt_start,
			Pageable processPage);

	 
	// @Query("{  'ticketDueDateAndTime': { $gte: ?3, $lte: ?4 }   },{ $or: [ { 'globalStatus': { $in: ?0 } }]},{$and:[ { 'globalStatus': ?1, 'completionDateTime': { $gt: ?2 }]}")
	 @Query("{"
	            + " 'ticketDueDateAndTime': {$gte: ?3, $lte: ?4},"
	            + " 'isAssistTicket':'No',"
	            + " '$or': ["
	            + "   {"
	            + "     '$and': ["
	            + "       {'globalStatus': ?1},"
	            + "       {'completionDateTime': {$gte: ?2}}"
	            + "     ]"
	            + "   },"
	            + "   {'globalStatus': {$in: ?0}}"
	            + " ]"
	            + "}")  
	Page<Ticket> findByGlobalStatusInOrGlobalStatusAndCompletionDateTimeGreaterThanAndTicketDueDateBetween(String[] strings,
			String statusComplete, LocalDateTime ldt_start, LocalDateTime startDate, LocalDateTime endDate,
			Pageable pageable);

	 @Query("{" +
				"'globalStatus': ?0, " +
				"'isAssistTicket':'No' "+
	            "'completionDateTime': { $gte: ?2 }" +
	            "}")
	Page<Ticket> findByGlobalStatusAndIsAssistTicketAndCompletionDateTimeGreaterThan(String statusComplete, String no,
			LocalDateTime ldt_start, Pageable pageable);

	
	@Query("{" +
			"'globalStatus': ?0, " +
			"'isAssistTicket':'No' "+
            "'ticketDueDateAndTime': { $gte: ?3, $lte: ?4 }" +
            "'completionDateTime': { $gte: ?2 }" +
            "}")
	Page<Ticket> findByGlobalStatusAndIsAssistTicketAndCompletionDateTime(String globalStatus, String no,
			LocalDateTime ldt_start, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

	 
	 @Query("{"
			    + " 'isAssistTicket':'No',"
	            + " '$or': ["
	            + "   {"
	            + "     '$and': ["
	            + "       {'globalStatus': ?1},"
	            + "       {'completionDateTime': {$gte: ?2}}"
	            + "     ]"
	            + "   },"
	            + "   {'globalStatus': {$in: ?0}}"
	            + " ]"
	           
	            + "}")
	 Page<Ticket> findByGlobalStatusInOrGlobalStatusAndCompletionDateTimeGreaterThan(String[] strings,
			String statusComplete, LocalDateTime ldt_start, Pageable pageable);
	 
	 @Query("{ $or: [ { 'globalStatus': { $in: ?0 } }]},{$and:[ { 'globalStatus': ?1, 'completionDateTime': { $gt: ?2 }]} },{'supervisorId':?3},'isAssistTicket':'No'")
		Page<Ticket> findByGlobalStatusInOrGlobalStatusAndCompletionDateTimeGreaterThanAndSuperviorId(String[] strings,
				String statusComplete, LocalDateTime ldt_start,String supId, Pageable pageable);

	  @Query("{"
	            + " 'ticketDueDateAndTime': {$gte: ?3, $lte: ?4},"
	            + " 'isAssistTicket':'No',"
	            + " '$or': ["
	            + "   {"
	            + "     '$and': ["
	            + "       {'globalStatus': ?1},"
	            + "       {'completionDateTime': {$gte: ?2}}"
	            + "     ]"
	            + "   },"
	            + "   {'globalStatus': {$in: ?0}}"
	            + " ],"
	            + " 'supervisorId': ?5"
	            + "}")
	Page<Ticket> findByGlobalStatusInOrGlobalStatusWithQuery(String[] strings, String statusComplete,
			LocalDateTime ldt_start, LocalDateTime startDate, LocalDateTime endDate,String supId, Pageable processPage);
		
	
}