package com.lovedev.api.service;

import com.lovedev.api.exception.ResourceNotFoundException;
import com.lovedev.api.exception.UnauthorizedException;
import com.lovedev.api.mapper.NotificationMapper;
import com.lovedev.api.model.dto.response.NotificationResponse;
import com.lovedev.api.model.dto.response.PageResponse;
import com.lovedev.api.model.entity.Notification;
import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.enums.NotificationStatus;
import com.lovedev.api.model.enums.NotificationType;
import com.lovedev.api.repository.NotificationRepository;
import com.lovedev.api.repository.UserRepository;
import com.lovedev.api.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(int page, int size, String status) {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage;

        if (status != null && !status.isEmpty()) {
            NotificationStatus notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
            notificationPage = notificationRepository.findByUserAndStatusOrderByCreatedAtDesc(user, notificationStatus, pageable);
        } else {
            notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }

        List<NotificationResponse> responses = notificationMapper.toResponseList(notificationPage.getContent());

        return PageResponse.<NotificationResponse>builder()
                .content(responses)
                .pageNumber(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .last(notificationPage.isLast())
                .first(notificationPage.isFirst())
                .empty(notificationPage.isEmpty())
                .hasNext(notificationPage.hasNext())
                .hasPrevious(notificationPage.hasPrevious())
                .build();
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        UUID userId = SecurityHelper.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this notification");
        }

        notification.markAsRead();
        notification = notificationRepository.save(notification);

        log.info("Notification marked as read: {}", notificationId);
        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public void markAllAsRead() {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        notificationRepository.markAllAsReadByUser(user, LocalDateTime.now());
        log.info("All notifications marked as read for user: {}", user.getEmail());
    }

    @Transactional
    public void deleteNotification(UUID notificationId) {
        UUID userId = SecurityHelper.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this notification");
        }

        notificationRepository.delete(notification);
        log.info("Notification deleted: {}", notificationId);
    }

    @Transactional
    public void deleteAllNotifications() {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        notificationRepository.deleteAllByUser(user);
        log.info("All notifications deleted for user: {}", user.getEmail());
    }

    @Transactional
    public void sendTestNotification() {
        UUID userId = SecurityHelper.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .title("Test Notification")
                .body("This is a test notification from LoveDev API")
                .type(NotificationType.INFO)
                .status(NotificationStatus.UNREAD)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Test notification sent to user: {}", user.getEmail());
    }
}