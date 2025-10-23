package com.lovedev.api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FCMTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String fcmToken;

    private String deviceType; // "android", "ios", "web"
    private String deviceId;
}