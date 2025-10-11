package com.lovedev.api.model.dto.request;

import com.lovedev.api.model.enums.UserStatus;
import lombok.Data;

@Data
public class UserSearchRequest {

    /**
     * Search keyword (searches in firstName, lastName, email)
     */
    private String keyword;

    /**
     * Filter by user status
     */
    private UserStatus status;

    /**
     * Filter by email verification status
     */
    private Boolean emailVerified;

    /**
     * Filter by role name (optional)
     * Example: "ROLE_ADMIN", "ROLE_USER"
     */
    private String roleName;
}