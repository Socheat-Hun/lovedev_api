package com.lovedev.api.model.dto.request;

import com.lovedev.api.model.enums.Role;
import com.lovedev.api.model.enums.UserStatus;
import lombok.Data;

@Data
public class UserSearchRequest {
    private String keyword; // Search in name, email
    private Role role;
    private UserStatus status;
    private Boolean emailVerified;
}