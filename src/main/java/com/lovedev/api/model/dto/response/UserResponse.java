package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lovedev.api.model.enums.Role;
import com.lovedev.api.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * User response DTO with multi-role support
 * Contains user information returned in API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User information response with multi-role support")
public class UserResponse {

    @Schema(
            description = "User unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    private UUID id;

    @Schema(
            description = "User email address",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "User first name",
            example = "John"
    )
    private String firstName;

    @Schema(
            description = "User last name",
            example = "Doe"
    )
    private String lastName;

    @Schema(
            description = "User full name (first + last)",
            example = "John Doe"
    )
    private String fullName;

    @Schema(
            description = "User phone number",
            example = "+1234567890"
    )
    private String phoneNumber;

    @Schema(
            description = "User address",
            example = "123 Main St, New York, NY 10001"
    )
    private String address;

    @Schema(
            description = "User date of birth",
            example = "1990-01-15"
    )
    private LocalDate dateOfBirth;

    @Schema(
            description = "Profile picture URL",
            example = "https://example.com/avatars/user123.jpg"
    )
    private String profilePictureUrl;

    @Schema(
            description = "User biography/description",
            example = "Software developer passionate about creating amazing applications"
    )
    private String bio;

    @Schema(
            description = "User roles (can have multiple)",
            example = "[\"USER\", \"EMPLOYEE\", \"MANAGER\"]"
    )
    private Set<Role> roles;

    @Schema(
            description = "Primary role (highest privilege role)",
            example = "MANAGER",
            allowableValues = {"USER", "EMPLOYEE", "MANAGER", "ADMIN"}
    )
    private Role primaryRole;

    @Schema(
            description = "User account status",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "INACTIVE", "BANNED"}
    )
    private UserStatus status;

    @Schema(
            description = "Is email verified?",
            example = "true"
    )
    private Boolean emailVerified;

    @Schema(
            description = "Last login timestamp",
            example = "2024-10-09T10:30:00"
    )
    private LocalDateTime lastLoginAt;

    @Schema(
            description = "Account creation timestamp",
            example = "2024-01-01T10:00:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Last update timestamp",
            example = "2024-10-09T10:30:00"
    )
    private LocalDateTime updatedAt;

    /**
     * Check if user is active
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && Boolean.TRUE.equals(emailVerified);
    }

    /**
     * Check if user has admin role
     */
    public boolean isAdmin() {
        return roles != null && roles.contains(Role.ADMIN);
    }

    /**
     * Check if user has manager role
     */
    public boolean isManager() {
        return roles != null && roles.contains(Role.MANAGER);
    }

    /**
     * Check if user has employee role
     */
    public boolean isEmployee() {
        return roles != null && roles.contains(Role.EMPLOYEE);
    }

    /**
     * Check if user has user role
     */
    public boolean isUser() {
        return roles != null && roles.contains(Role.USER);
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(Role... rolesToCheck) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (Role role : rolesToCheck) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has all of the specified roles
     */
    public boolean hasAllRoles(Role... rolesToCheck) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (Role role : rolesToCheck) {
            if (!roles.contains(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get role display names as comma-separated string
     */
    public String getRolesDisplay() {
        if (roles == null || roles.isEmpty()) {
            return "No roles";
        }
        return roles.stream()
                .map(Role::name)
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse("No roles");
    }

    /**
     * Get primary role display name
     */
    public String getPrimaryRoleDisplay() {
        return primaryRole != null ? primaryRole.name() : "NONE";
    }

    /**
     * Get status display name
     */
    public String getStatusDisplay() {
        return status != null ? status.name() : "UNKNOWN";
    }

    /**
     * Get role count
     */
    public int getRoleCount() {
        return roles != null ? roles.size() : 0;
    }
}