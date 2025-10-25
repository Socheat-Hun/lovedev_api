package com.lovedev.api.repository;

import com.lovedev.api.model.entity.Notification;
import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndStatusOrderByCreatedAtDesc(User user, NotificationStatus status, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.status = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.status = 'UNREAD' ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :readAt WHERE n.user = :user AND n.status = 'UNREAD'")
    void markAllAsReadByUser(@Param("user") User user, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :expiryDate")
    void deleteExpiredNotifications(@Param("expiryDate") LocalDateTime expiryDate);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user")
    void deleteAllByUser(@Param("user") User user);
}