package com.lovedev.api.controller;

import com.lovedev.api.model.dto.response.ApiResponse;
import com.lovedev.api.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Notifications", description = "Push notification management")
public class NotificationController {

    private final FCMService fcmService;

    @Operation(summary = "Send test notification", description = "Send test notification to current user")
    @PostMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> sendTestNotification() {
        // Implementation depends on your requirements
        return ResponseEntity.ok(ApiResponse.success("Test notification sent"));
    }

    @Operation(summary = "Send broadcast notification", description = "Send notification to all users (Admin only)")
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> sendBroadcast(
            @RequestParam String title,
            @RequestParam String body,
            @RequestBody(required = false) Map<String, String> data) {

        fcmService.sendTopicNotification("all_users", title, body, data);
        return ResponseEntity.ok(ApiResponse.success("Broadcast notification sent"));
    }
}