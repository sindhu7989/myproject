package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Entity

@Table(name = "ts_technician_daily_task")

@ToString
@Data

public class TechnicianWorkHour {

 

        @Id

        private int id;

 

        @Column(name = "tech_empid", nullable = false)

        private String techEmpId;

 

        @Column(name = "supervisorId", nullable = false)

        private String supervisorId;

 

        @Column(name = "availableTime", nullable = false)

        private String availableTime;

 

        @Column(name = "calenderDate", nullable = false)

        private LocalDate calendarDate;
        

 

        @Column(name = "projectTime", nullable = false)

        private String projectTime;

 

        @Column(name = "onCallStartDateTime", nullable = false)

        private String onCallStartDateTime;

 

        @Column(name = "onCallEndDateTime", nullable = false)

        private String onCallEndDateTime = "00:00";

 

        @Column(name = "isOnCall", nullable = false)

        private String isOnCall = "00:00";

 

        @Column(name = "isWeekend", nullable = false)

        private String isWeekend;

 

        @Column(name = "availabilityStatus", nullable = false)

        private String availabilityStatus;

 

}