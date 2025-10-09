package com.lovedev.api.model.dto.request;

import com.lovedev.api.model.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddRoleRequest {
    @NotNull(message = "Role is required")
    private Role role;
}