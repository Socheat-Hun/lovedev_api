package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification statistics response")
public class NotificationStatsResponse {

    @Schema(description = "Total notifications count", example = "150")
    private Long totalCount;

    @Schema(description = "Unread notifications count", example = "25")
    private Long unreadCount;

    @Schema(description = "Read notifications count", example = "120")
    private Long readCount;

    @Schema(description = "Archived notifications count", example = "5")
    private Long archivedCount;

    @Schema(description = "Has unread notifications", example = "true")
    private Boolean hasUnread;
}