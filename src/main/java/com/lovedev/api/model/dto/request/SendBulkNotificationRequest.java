package com.lovedev.api.model.dto.request;

import com.lovedev.api.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendBulkNotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private NotificationType type = NotificationType.INFO;

    private String data;

    private String actionUrl;
}