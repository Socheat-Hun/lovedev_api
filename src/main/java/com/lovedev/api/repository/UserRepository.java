package com.lovedev.api.repository;

import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.enums.Role;
import com.lovedev.api.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
//            "(:role IS NULL OR u.role = :role) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:emailVerified IS NULL OR u.emailVerified = :emailVerified)")
    Page<User> searchUsers(@Param("keyword") String keyword,
//                           @Param("role") Role role,
                           @Param("status") UserStatus status,
                           @Param("emailVerified") Boolean emailVerified,
                           Pageable pageable);
}