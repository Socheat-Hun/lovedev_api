package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simplified Notification Settings Response that matches the Entity
 * This DTO only includes fields that actually exist in the NotificationSettings entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification settings response")
public class NotificationSettingsResponse {

    @Schema(description = "Settings ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Push notifications enabled", example = "true")
    private Boolean pushEnabled;

    @Schema(description = "Email notifications enabled", example = "true")
    private Boolean emailEnabled;

    @Schema(description = "System notifications enabled", example = "true")
    private Boolean systemNotifications;

    @Schema(description = "Account notifications enabled", example = "true")
    private Boolean accountNotifications;

    @Schema(description = "Security alerts enabled (recommended to keep true)", example = "true")
    private Boolean securityAlerts;

    @Schema(description = "Creation timestamp", example = "2024-10-26T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-10-26T11:30:00")
    private LocalDateTime updatedAt;
}