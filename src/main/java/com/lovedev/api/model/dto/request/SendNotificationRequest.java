package com.lovedev.api.model.dto.request;

import com.lovedev.api.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SendNotificationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private NotificationType type = NotificationType.INFO;

    private String data;

    private String actionUrl;
}