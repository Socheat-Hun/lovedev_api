package com.lovedev.api.repository;

import com.lovedev.api.model.entity.NotificationSettings;
import com.lovedev.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, UUID> {

    Optional<NotificationSettings> findByUser(User user);

    boolean existsByUser(User user);
}