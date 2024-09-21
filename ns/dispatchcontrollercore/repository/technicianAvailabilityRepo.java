package com.straviso.ns.dispatchcontrollercore.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.straviso.ns.dispatchcontrollercore.entity.TechnicianAvailability;

public interface technicianAvailabilityRepo extends MongoRepository<TechnicianAvailability, String> 
{

	boolean existsByTechnicianIdAndCalenderDateBetween(String technicianId, LocalDateTime localDateTimeStart,
			LocalDateTime localDateTimeEnd);
	
	List<TechnicianAvailability> findByTechnicianIdAndCalenderDateBetween(String technicianId,
			LocalDateTime localDateTimeStart, LocalDateTime localDateTimeEnd);

}
