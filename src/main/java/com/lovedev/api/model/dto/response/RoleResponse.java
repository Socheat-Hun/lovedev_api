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
@Schema(description = "Role information response")
public class RoleResponse {

    @Schema(description = "Role ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Role name", example = "ROLE_ADMIN")
    private String name;

    @Schema(description = "Role description", example = "Administrator role with full system access")
    private String description;

    @Schema(description = "Is this a system role?", example = "true")
    private Boolean isSystemRole;

    @Schema(description = "Permission names assigned to this role")
    private Set<String> permissions;

    @Schema(description = "Number of users with this role", example = "5")
    private Integer userCount;

    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-10-09T10:30:00")
    private LocalDateTime updatedAt;
}