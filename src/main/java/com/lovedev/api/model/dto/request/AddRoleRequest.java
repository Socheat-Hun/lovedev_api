package com.lovedev.api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddRoleRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;
}