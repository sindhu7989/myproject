package com.straviso.ns.dispatchcontrollercore.dto;

import lombok.Data;

@Data
public class EmployeeTenureDTO {
    private String ID;
    private String Legal_Last_Name;
    private String Legal_First_Name;
    private String Preferred_Last_Name;
    private String Preferred_First_Name;
    private String Job_Title;
    private String Continuous_Service_Date;
    private String Work_Email;
}