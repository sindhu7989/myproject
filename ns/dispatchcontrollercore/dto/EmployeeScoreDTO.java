package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class EmployeeScoreDTO {
    private String score_id;
    private String employee_id;
    private String employee_quality_score;
    private String Continuous_Service_Date;
}
