package com.lovedev.api.controller;

import com.lovedev.api.model.dto.request.*;
import com.lovedev.api.model.dto.response.ApiResponse;
import com.lovedev.api.model.dto.response.NotificationResponse;
import com.lovedev.api.model.dto.response.NotificationSettingsResponse;
import com.lovedev.api.model.dto.response.PageResponse;
import com.lovedev.api.model.entity.NotificationSettings;
import com.lovedev.api.service.FCMService;
import com.lovedev.api.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;
    private final FCMService fcmService;

    // ============================================
    // User Notification Endpoints
    // ============================================

    @Operation(summary = "Get user notifications", description = "Get paginated list of user's notifications")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        PageResponse<NotificationResponse> response = notificationService.getUserNotifications(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get notification statistics", description = "Get notification counts (unread, read, total)")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getNotificationStats() {
        Map<String, Long> stats = fcmService.getNotificationStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @Operation(summary = "Mark all notifications as read", description = "Mark all user notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    @Operation(summary = "Delete all notifications", description = "Delete all user notifications")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications() {
        notificationService.deleteAllNotifications();
        return ResponseEntity.ok(ApiResponse.success("All notifications deleted successfully", null));
    }

    // ============================================
    // FCM Token Management
    // ============================================

    @Operation(summary = "Register FCM token", description = "Register device FCM token for push notifications")
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> registerFCMToken(@Valid @RequestBody FCMTokenRequest request) {
        fcmService.registerFCMToken(request);
        return ResponseEntity.ok(ApiResponse.success("FCM token registered successfully", null));
    }

    @Operation(summary = "Remove FCM token", description = "Remove device FCM token")
    @DeleteMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> removeFCMToken(@RequestParam String token) {
        fcmService.removeFCMToken(token);
        return ResponseEntity.ok(ApiResponse.success("FCM token removed successfully", null));
    }

    // ============================================
    // Notification Settings
    // ============================================

    @Operation(summary = "Get notification settings", description = "Get user notification preferences")
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getNotificationSettings() {
        NotificationSettingsResponse settings = fcmService.getNotificationSettings();
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @Operation(summary = "Update notification settings", description = "Update user notification preferences")
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateNotificationSettings(
            @Valid @RequestBody NotificationSettingsRequest request) {
        NotificationSettingsResponse settings = fcmService.updateNotificationSettings(request);
        return ResponseEntity.ok(ApiResponse.success("Notification settings updated successfully", settings));
    }

    // ============================================
    // Admin Endpoints
    // ============================================

    @Operation(summary = "Send notification to user", description = "Send notification to specific user (Admin only)")
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendNotificationToUser(@Valid @RequestBody SendNotificationRequest request) {
        fcmService.sendNotificationToUser(request);
        return ResponseEntity.ok(ApiResponse.success("Notification sent successfully", null));
    }

    @Operation(summary = "Send notification to all users", description = "Send notification to all users (Admin only)")
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendNotificationToAllUsers(@Valid @RequestBody SendBulkNotificationRequest request) {
        fcmService.sendNotificationToAllUsers(request);
        return ResponseEntity.ok(ApiResponse.success("Notification broadcast initiated", null));
    }

    // ============================================
    // Test Endpoints (Admin only)
    // ============================================

    @Operation(summary = "Test send notification", description = "Test sending notification to current user (Admin only)")
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> testNotification() {
        notificationService.sendTestNotification();
        return ResponseEntity.ok(ApiResponse.success("Test notification sent", null));
    }
}