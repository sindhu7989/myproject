package com.straviso.ns.dispatchcontrollercore.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class TicketActionTrail {

    private String action;
    private String actionBy;
    private LocalDateTime actionOn;
    private String preAction;
    private String postAction;

 

}