package com.straviso.ns.dispatchcontrollercore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@NoArgsConstructor

@AllArgsConstructor

public class FCMSendNotificationRequest {

    

    private String userId;

    private String title;

    private String message;

    private String fcmFlag = "1";

 

}
