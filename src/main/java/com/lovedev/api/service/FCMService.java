package com.lovedev.api.service;

import com.google.firebase.messaging.*;
import com.lovedev.api.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    @Value("${app.firebase.enabled:true}")
    private boolean firebaseEnabled;

    /**
     * Send notification to a single user
     */
    @Async("taskExecutor")
    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        if (!firebaseEnabled) {
            log.info("Firebase is disabled, skipping notification");
            return;
        }

        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("FCM token is null or empty, cannot send notification");
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setColor("#2196F3")
                                    .setChannelId("default")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .setBadge(1)
                                    .build())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent notification: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM notification to token: {}", fcmToken, e);

            // Handle invalid token
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT ||
                    e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("Invalid or unregistered FCM token: {}", fcmToken);
                // Token should be removed from database
            }
        }
    }

    /**
     * Send notification to a user
     */
    @Async("taskExecutor")
    public void sendNotificationToUser(User user, String title, String body, Map<String, String> data) {
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            sendNotification(user.getFcmToken(), title, body, data);
        } else {
            log.warn("User {} has no FCM token registered", user.getEmail());
        }
    }

    /**
     * Send notification to multiple users
     */
    @Async("taskExecutor")
    public void sendNotificationToMultipleUsers(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        if (!firebaseEnabled || fcmTokens == null || fcmTokens.isEmpty()) {
            log.warn("Cannot send notifications: Firebase disabled or empty token list");
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("Successfully sent {} notifications, {} failures",
                    response.getSuccessCount(), response.getFailureCount());

            // Log failures
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.error("Failed to send notification to token {}: {}",
                                fcmTokens.get(i), responses.get(i).getException().getMessage());
                    }
                }
            }

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast FCM notification", e);
        }
    }

    /**
     * Send topic notification
     */
    @Async("taskExecutor")
    public void sendTopicNotification(String topic, String title, String body, Map<String, String> data) {
        if (!firebaseEnabled) {
            log.info("Firebase is disabled, skipping topic notification");
            return;
        }

        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : new HashMap<>())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent topic notification to {}: {}", topic, response);

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send topic notification to {}", topic, e);
        }
    }

    /**
     * Subscribe user to topic
     */
    public void subscribeToTopic(String fcmToken, String topic) {
        if (!firebaseEnabled || fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        try {
            FirebaseMessaging.getInstance().subscribeToTopic(List.of(fcmToken), topic);
            log.info("Successfully subscribed token to topic: {}", topic);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to subscribe to topic {}", topic, e);
        }
    }

    /**
     * Unsubscribe user from topic
     */
    public void unsubscribeFromTopic(String fcmToken, String topic) {
        if (!firebaseEnabled || fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(fcmToken), topic);
            log.info("Successfully unsubscribed token from topic: {}", topic);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to unsubscribe from topic {}", topic, e);
        }
    }

    /**
     * Send welcome notification after registration
     */
    public void sendWelcomeNotification(User user) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "welcome");
        data.put("userId", user.getId().toString());
        data.put("action", "open_dashboard");

        sendNotificationToUser(
                user,
                "Welcome to LoveDev! 🎉",
                String.format("Hi %s! Your account has been created successfully.", user.getFirstName()),
                data
        );
    }

    /**
     * Send email verification notification
     */
    public void sendEmailVerificationNotification(User user) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "email_verification");
        data.put("userId", user.getId().toString());

        sendNotificationToUser(
                user,
                "Verify Your Email 📧",
                "Please check your email to verify your account.",
                data
        );
    }

    /**
     * Send email verified notification
     */
    public void sendEmailVerifiedNotification(User user) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "email_verified");
        data.put("userId", user.getId().toString());
        data.put("action", "open_dashboard");

        sendNotificationToUser(
                user,
                "Email Verified! ✅",
                "Your email has been verified. You can now access all features!",
                data
        );
    }
}