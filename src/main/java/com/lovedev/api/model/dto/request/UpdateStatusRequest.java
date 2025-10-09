package com.lovedev.api.model.dto.request;

import com.lovedev.api.model.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {
    @NotNull(message = "Status is required")
    private UserStatus status;
}