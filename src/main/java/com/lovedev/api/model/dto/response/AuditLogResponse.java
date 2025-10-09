package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lovedev.api.model.enums.AuditAction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Audit log response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Audit log entry response")
public class AuditLogResponse {

    @Schema(
            description = "Audit log unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(
            description = "User ID who performed the action",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID userId;

    @Schema(
            description = "User name who performed the action",
            example = "John Doe"
    )
    private String userName;

    @Schema(
            description = "Action performed",
            example = "UPDATE",
            allowableValues = {
                    "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT",
                    "REGISTER", "VERIFY_EMAIL", "RESET_PASSWORD",
                    "CHANGE_ROLE", "CHANGE_STATUS", "UPLOAD_AVATAR"
            }
    )
    private AuditAction action;

    @Schema(
            description = "Type of entity affected",
            example = "User"
    )
    private String entityType;

    @Schema(
            description = "ID of the entity affected",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private String entityId;

    @Schema(
            description = "Old values before the change (JSON)",
            example = "{\"role\": \"USER\", \"status\": \"ACTIVE\"}"
    )
    private Map<String, Object> oldValue;

    @Schema(
            description = "New values after the change (JSON)",
            example = "{\"role\": \"ADMIN\", \"status\": \"ACTIVE\"}"
    )
    private Map<String, Object> newValue;

    @Schema(
            description = "IP address of the user",
            example = "192.168.1.1"
    )
    private String ipAddress;

    @Schema(
            description = "User agent (browser/client info)",
            example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    )
    private String userAgent;

    @Schema(
            description = "Additional description of the action",
            example = "User role changed from USER to ADMIN"
    )
    private String description;

    @Schema(
            description = "Timestamp when the action was performed",
            example = "2024-10-09T10:30:00"
    )
    private LocalDateTime createdAt;
}