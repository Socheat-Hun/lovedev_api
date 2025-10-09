package com.lovedev.api.model.dto.request;

import com.lovedev.api.model.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateRolesRequest {
    @NotEmpty(message = "At least one role is required")
    private Set<Role> roles;
}