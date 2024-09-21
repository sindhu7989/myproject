package com.straviso.ns.dispatchcontrollercore.dto.request;

import java.util.HashMap;

import java.util.List;

import java.util.Map;

 

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;

 

import lombok.AllArgsConstructor;

import lombok.Data;

import lombok.NoArgsConstructor;

 

@Data

@NoArgsConstructor

@AllArgsConstructor

public class BuzzSendNotificationRequest {

    

    private String senderId = DispatchControllerConstants.BUZZ_PN_SENDER_ID;

    private String receiverId;

    private String creationApplication = DispatchControllerConstants.BUZZ_PN_CREATION_APPLICATION;

    private String ignoreIncomingPreferences = DispatchControllerConstants.FLAG_N;

    private String isDynamicMessage = DispatchControllerConstants.FLAG_Y;

    private String fcmFlag = DispatchControllerConstants.BUZZ_PN_FCM_FLAG;

    private List<String> channels =  DispatchControllerConstants.getBuzzNotificationChannels();

    private String processName;

    private String processCode;

    private String templateName;

    private Map<String,String> tokens = new HashMap<>();

 

}
