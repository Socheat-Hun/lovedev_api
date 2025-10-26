package com.lovedev.api.service;

import com.google.firebase.messaging.*;
import com.lovedev.api.exception.BadRequestException;
import com.lovedev.api.exception.ResourceNotFoundException;
import com.lovedev.api.model.dto.request.FCMTokenRequest;
import com.lovedev.api.model.dto.request.NotificationSettingsRequest;
import com.lovedev.api.model.dto.request.SendBulkNotificationRequest;
import com.lovedev.api.model.dto.request.SendNotificationRequest;
import com.lovedev.api.model.dto.response.NotificationSettingsResponse;
import com.lovedev.api.model.entity.FCMToken;
import com.lovedev.api.model.entity.Notification;
import com.lovedev.api.model.entity.NotificationSettings;
import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.enums.NotificationStatus;
import com.lovedev.api.repository.FCMTokenRepository;
import com.lovedev.api.repository.NotificationRepository;
import com.lovedev.api.repository.NotificationSettingsRepository;
import com.lovedev.api.repository.UserRepository;
import com.lovedev.api.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    private final FCMTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final UserRepository userRepository;

    // ============================================
    // FCM Token Management
    // ============================================

    @Transactional
    public void registerFCMToken(FCMTokenRequest request) {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if token already exists
        Optional<FCMToken> existingToken = fcmTokenRepository.findByTokenAndIsActiveTrue(request.getFcmToken());

        if (existingToken.isPresent()) {
            // Update last used time
            FCMToken token = existingToken.get();
            token.setLastUsedAt(LocalDateTime.now());
            fcmTokenRepository.save(token);
            log.info("FCM token updated for user: {}", user.getEmail());
            return;
        }

        // Create new token
        FCMToken fcmToken = FCMToken.builder()
                .user(user)
                .token(request.getFcmToken())
                .deviceType(request.getDeviceType())
                .deviceId(request.getDeviceId())
                .isActive(true)
                .lastUsedAt(LocalDateTime.now())
                .build();

        fcmTokenRepository.save(fcmToken);
        log.info("FCM token registered for user: {}", user.getEmail());
    }

    @Transactional
    public void removeFCMToken(String token) {
        fcmTokenRepository.deactivateToken(token, LocalDateTime.now());
        log.info("FCM token deactivated: {}", token);
    }

    @Transactional
    public void removeAllUserFCMTokens() {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        fcmTokenRepository.deactivateAllUserTokens(user, LocalDateTime.now());
        log.info("All FCM tokens deactivated for user: {}", user.getEmail());
    }

    // ============================================
    // Notification Settings
    // ============================================

    /**
     * Get notification settings for current user
     * Returns DTO to avoid LazyInitializationException
     */
    @Transactional(readOnly = true)
    public NotificationSettingsResponse getNotificationSettings() {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        NotificationSettings settings = notificationSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        // Convert Entity to DTO
        return mapToResponse(settings);
    }

    /**
     * Update notification settings for current user
     * Returns DTO to avoid LazyInitializationException
     */
    @Transactional
    public NotificationSettingsResponse updateNotificationSettings(NotificationSettingsRequest request) {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        NotificationSettings settings = notificationSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        // Update only provided fields
        if (request.getPushEnabled() != null) {
            settings.setPushEnabled(request.getPushEnabled());
        }
        if (request.getEmailEnabled() != null) {
            settings.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getSystemNotifications() != null) {
            settings.setSystemNotifications(request.getSystemNotifications());
        }
        if (request.getAccountNotifications() != null) {
            settings.setAccountNotifications(request.getAccountNotifications());
        }
        if (request.getSecurityAlerts() != null) {
            settings.setSecurityAlerts(request.getSecurityAlerts());
        }

        settings = notificationSettingsRepository.save(settings);
        log.info("Notification settings updated for user: {}", user.getEmail());

        // Convert Entity to DTO
        return mapToResponse(settings);
    }

    /**
     * Helper method to convert NotificationSettings Entity to Response DTO
     * This prevents LazyInitializationException and circular reference issues
     */
    private NotificationSettingsResponse mapToResponse(NotificationSettings settings) {
        return NotificationSettingsResponse.builder()
                .id(settings.getId())
                .pushEnabled(settings.getPushEnabled())
                .emailEnabled(settings.getEmailEnabled())
                .systemNotifications(settings.getSystemNotifications())
                .accountNotifications(settings.getAccountNotifications())
                .securityAlerts(settings.getSecurityAlerts())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }

    private NotificationSettings createDefaultSettings(User user) {
        NotificationSettings settings = NotificationSettings.builder()
                .user(user)
                .pushEnabled(true)
                .emailEnabled(true)
                .systemNotifications(true)
                .accountNotifications(true)
                .securityAlerts(true)
                .build();

        return notificationSettingsRepository.save(settings);
    }

    // ============================================
    // Send Notifications
    // ============================================

    @Async
    @Transactional
    public void sendNotificationToUser(SendNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Save notification in database
        Notification notification = Notification.builder()
                .user(user)
                .title(request.getTitle())
                .body(request.getBody())
                .type(request.getType())
                .status(NotificationStatus.UNREAD)
                .data(request.getData())
                .actionUrl(request.getActionUrl())
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        // Send push notification via FCM
        sendPushNotification(user, request.getTitle(), request.getBody(), request.getData());

        log.info("Notification sent to user: {}", user.getEmail());
    }

    @Async
    @Transactional
    public void sendNotificationToAllUsers(SendBulkNotificationRequest request) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                Notification notification = Notification.builder()
                        .user(user)
                        .title(request.getTitle())
                        .body(request.getBody())
                        .type(request.getType())
                        .status(NotificationStatus.UNREAD)
                        .data(request.getData())
                        .actionUrl(request.getActionUrl())
                        .sentAt(LocalDateTime.now())
                        .build();

                notificationRepository.save(notification);

                // Send push notification
                sendPushNotification(user, request.getTitle(), request.getBody(), request.getData());

            } catch (Exception e) {
                log.error("Failed to send notification to user: {}", user.getEmail(), e);
            }
        }

        log.info("Bulk notification sent to {} users", users.size());
    }

    private void sendPushNotification(User user, String title, String body, String data) {
        // Check if user has push notifications enabled
        NotificationSettings settings = notificationSettingsRepository.findByUser(user)
                .orElse(null);

        if (settings == null || !settings.getPushEnabled()) {
            log.debug("Push notifications disabled for user: {}", user.getEmail());
            return;
        }

        // Get active FCM tokens
        List<FCMToken> tokens = fcmTokenRepository.findActiveTokensByUser(user);

        if (tokens.isEmpty()) {
            log.debug("No active FCM tokens for user: {}", user.getEmail());
            return;
        }

        for (FCMToken fcmToken : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(fcmToken.getToken())
                        .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putData("data", data != null ? data : "")
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .build())
                        .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                        .setSound("default")
                                        .build())
                                .build())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("FCM message sent successfully: {}", response);

                // Update last used time
                fcmToken.setLastUsedAt(LocalDateTime.now());
                fcmTokenRepository.save(fcmToken);

            } catch (FirebaseMessagingException e) {
                log.error("Failed to send FCM message to token: {}", fcmToken.getToken(), e);

                // Deactivate invalid tokens
                if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                        e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    fcmTokenRepository.deactivateToken(fcmToken.getToken(), LocalDateTime.now());
                    log.info("Deactivated invalid FCM token");
                }
            }
        }
    }

    // ============================================
    // Notification Statistics
    // ============================================

    @Transactional(readOnly = true)
    public Map<String, Long> getNotificationStats() {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long unreadCount = notificationRepository.countByUserAndStatus(user, NotificationStatus.UNREAD);
        Long readCount = notificationRepository.countByUserAndStatus(user, NotificationStatus.READ);
        Long totalCount = unreadCount + readCount;

        Map<String, Long> stats = new HashMap<>();
        stats.put("unread", unreadCount);
        stats.put("read", readCount);
        stats.put("total", totalCount);

        return stats;
    }

    // ============================================
    // Scheduled Tasks
    // ============================================

    @Scheduled(cron = "0 0 3 * * *") // Run at 3 AM daily
    @Transactional
    public void cleanupExpiredNotifications() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(30); // Keep for 30 days
        notificationRepository.deleteExpiredNotifications(expiryDate);
        log.info("Cleaned up expired notifications older than 30 days");
    }

    @Scheduled(cron = "0 0 4 * * *") // Run at 4 AM daily
    @Transactional
    public void deactivateOldFCMTokens() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(90); // Deactivate tokens not used in 90 days
        fcmTokenRepository.deactivateOldTokens(expiryDate, LocalDateTime.now());
        log.info("Deactivated FCM tokens older than 90 days");
    }
}