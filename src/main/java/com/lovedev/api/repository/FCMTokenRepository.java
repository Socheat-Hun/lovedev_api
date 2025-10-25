package com.lovedev.api.repository;

import com.lovedev.api.model.entity.FCMToken;
import com.lovedev.api.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, UUID> {

    Optional<FCMToken> findByTokenAndIsActiveTrue(String token);

    List<FCMToken> findByUserAndIsActiveTrue(User user);

    @Query("SELECT f FROM FCMToken f WHERE f.user = :user AND f.isActive = true")
    List<FCMToken> findActiveTokensByUser(@Param("user") User user);

    @Query("SELECT f FROM FCMToken f WHERE f.isActive = true")
    List<FCMToken> findAllActiveTokens();

    @Modifying
    @Query("UPDATE FCMToken f SET f.isActive = false, f.deactivatedAt = :deactivatedAt WHERE f.token = :token")
    void deactivateToken(@Param("token") String token, @Param("deactivatedAt") LocalDateTime deactivatedAt);

    @Modifying
    @Query("UPDATE FCMToken f SET f.isActive = false, f.deactivatedAt = :deactivatedAt WHERE f.user = :user")
    void deactivateAllUserTokens(@Param("user") User user, @Param("deactivatedAt") LocalDateTime deactivatedAt);

    @Modifying
    @Query("UPDATE FCMToken f SET f.isActive = false, f.deactivatedAt = :deactivatedAt WHERE f.lastUsedAt < :expiryDate")
    void deactivateOldTokens(@Param("expiryDate") LocalDateTime expiryDate, @Param("deactivatedAt") LocalDateTime deactivatedAt);

    boolean existsByTokenAndIsActiveTrue(String token);
}