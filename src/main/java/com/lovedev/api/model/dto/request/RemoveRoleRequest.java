package com.lovedev.api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RemoveRoleRequest {

    @NotBlank(message = "Role name is required")
    private String roleName;
}