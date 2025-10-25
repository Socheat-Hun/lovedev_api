package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@Schema(description = "Notification settings response")
public class NotificationSettingsResponse {

    @Schema(description = "Settings ID")
    private UUID id;

    @Schema(description = "Push notifications enabled", example = "true")
    private Boolean pushEnabled;

    @Schema(description = "Info notifications enabled", example = "true")
    private Boolean pushInfo;

    @Schema(description = "Success notifications enabled", example = "true")
    private Boolean pushSuccess;

    @Schema(description = "Warning notifications enabled", example = "true")
    private Boolean pushWarning;

    @Schema(description = "Error notifications enabled", example = "true")
    private Boolean pushError;

    @Schema(description = "Announcement notifications enabled", example = "true")
    private Boolean pushAnnouncement;

    @Schema(description = "Reminder notifications enabled", example = "true")
    private Boolean pushReminder;

    @Schema(description = "Message notifications enabled", example = "true")
    private Boolean pushMessage;

    @Schema(description = "System notifications enabled", example = "true")
    private Boolean pushSystem;

    @Schema(description = "Promotion notifications enabled", example = "false")
    private Boolean pushPromotion;

    @Schema(description = "Update notifications enabled", example = "true")
    private Boolean pushUpdate;

    @Schema(description = "Email notifications enabled", example = "true")
    private Boolean emailEnabled;

    @Schema(description = "Email digest enabled", example = "false")
    private Boolean emailDigest;

    @Schema(description = "Quiet hours enabled", example = "false")
    private Boolean quietHoursEnabled;

    @Schema(description = "Quiet hours start time", example = "22:00")
    private String quietHoursStart;

    @Schema(description = "Quiet hours end time", example = "08:00")
    private String quietHoursEnd;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}