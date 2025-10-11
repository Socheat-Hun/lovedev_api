package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Permission information response")
public class PermissionResponse {

    @Schema(description = "Permission ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Permission name", example = "user:update")
    private String name;

    @Schema(description = "Permission description", example = "Update any user profile")
    private String description;

    @Schema(description = "Resource", example = "user")
    private String resource;

    @Schema(description = "Action", example = "update")
    private String action;

    @Schema(description = "Roles that have this permission")
    private Set<String> roles;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
}