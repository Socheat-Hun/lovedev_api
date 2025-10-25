package com.lovedev.api.model.dto.request;

import lombok.Data;

@Data
public class NotificationSettingsRequest {

    private Boolean pushEnabled;

    private Boolean emailEnabled;

    private Boolean systemNotifications;

    private Boolean accountNotifications;

    private Boolean securityAlerts;
}