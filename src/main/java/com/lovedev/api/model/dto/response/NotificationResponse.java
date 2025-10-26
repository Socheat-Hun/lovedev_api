package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lovedev.api.model.enums.NotificationStatus;
import com.lovedev.api.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification response")
public class NotificationResponse {

    @Schema(description = "Notification ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Notification title", example = "Welcome to LoveDev")
    private String title;

    @Schema(description = "Notification body/message content", example = "Your account has been activated successfully")
    private String body;

    @Schema(description = "Notification message", example = "Your account has been activated successfully")
    private String message;

    @Schema(description = "Notification type", example = "SUCCESS")
    private NotificationType type;

    @Schema(description = "Notification status", example = "UNREAD")
    private NotificationStatus status;

    @Schema(description = "Action URL", example = "/dashboard")
    private String actionUrl;

    @Schema(description = "Image URL", example = "https://example.com/image.png")
    private String imageUrl;

    @Schema(description = "Additional data payload")
    private String dataPayload;

    @Schema(description = "Is notification sent via FCM", example = "true")
    private Boolean isSent;

    @Schema(description = "Sent timestamp", example = "2024-10-09T10:30:00")
    private LocalDateTime sentAt;

    @Schema(description = "Read timestamp", example = "2024-10-09T10:35:00")
    private LocalDateTime readAt;

    @Schema(description = "Creation timestamp", example = "2024-10-09T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Expiration timestamp", example = "2024-11-09T10:30:00")
    private LocalDateTime expiresAt;

    @Schema(description = "Is notification expired", example = "false")
    private Boolean isExpired;

    @Schema(description = "Is notification unread", example = "true")
    private Boolean isUnread;
}