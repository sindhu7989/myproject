package com.straviso.ns.dispatchcontrollercore.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.straviso.ns.dispatchcontrollercore.entity.TechnicianWorkHour;

public interface TechnicianWorkHourRepository extends JpaRepository<TechnicianWorkHour, Integer> {

	List<TechnicianWorkHour> findByCalendarDateAndTechEmpId(LocalDate now, String techEmpId);

	List<TechnicianWorkHour> findByCalendarDate(LocalDate now);

	List<TechnicianWorkHour> findByCalendarDateAndTechEmpId(LocalDate now, String technicianId, Sort sort);
	
	TechnicianWorkHour findByTechEmpIdAndCalendarDate(String techEmpId,LocalDate now);
	
}