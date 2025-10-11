package com.lovedev.api.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignPermissionsRequest {

    @NotEmpty(message = "At least one permission is required")
    private Set<String> permissionNames;
}