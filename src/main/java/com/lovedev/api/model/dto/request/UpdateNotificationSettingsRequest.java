package com.lovedev.api.model.dto.request;

import lombok.Data;

@Data
public class UpdateNotificationSettingsRequest {

    private Boolean pushEnabled;
    private Boolean pushInfo;
    private Boolean pushSuccess;
    private Boolean pushWarning;
    private Boolean pushError;
    private Boolean pushAnnouncement;
    private Boolean pushReminder;
    private Boolean pushMessage;
    private Boolean pushSystem;
    private Boolean pushPromotion;
    private Boolean pushUpdate;

    private Boolean emailEnabled;
    private Boolean emailDigest;

    private Boolean quietHoursEnabled;
    private String quietHoursStart; // Format: "HH:mm"
    private String quietHoursEnd;   // Format: "HH:mm"
}