package com.lovedev.api.repository;

import com.lovedev.api.model.entity.User;
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

    /**
     * Search users with filters (UPDATED - removed role parameter)
     * Keyword searches in: firstName, lastName, email
     */
    @Query(value = "SELECT * FROM users u " +
            "WHERE (u.deleted_at IS NULL) " +
            "AND (:keyword IS NULL " +
            "     OR LOWER(u.first_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(u.last_name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR u.status = CAST(:status AS VARCHAR)) " +
            "AND (:emailVerified IS NULL OR u.email_verified = :emailVerified) " +
            "ORDER BY u.created_at DESC",
            nativeQuery = true)
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("status") UserStatus status,
                           @Param("emailVerified") Boolean emailVerified,
                           Pageable pageable);

    /**
     * Find users by role name (NEW - uses join with roles table)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.roles r " +
            "WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Find users with specific role (NEW)
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findUsersByRole(@Param("roleName") String roleName, Pageable pageable);

    /**
     * Count users by role (NEW)
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);

    /**
     * Check if user has specific role (NEW)
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM User u " +
            "JOIN u.roles r WHERE u.id = :userId AND r.name = :roleName")
    boolean hasRole(@Param("userId") UUID userId, @Param("roleName") String roleName);

    /**
     * Find users with multiple roles (NEW)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.roles r " +
            "WHERE r.name IN :roleNames")
    Page<User> findByRoleNames(@Param("roleNames") java.util.Set<String> roleNames, Pageable pageable);

    /**
     * Find users with specific permission (NEW)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.roles r " +
            "JOIN r.permissions p " +
            "WHERE p.name = :permissionName")
    Page<User> findByPermission(@Param("permissionName") String permissionName, Pageable pageable);

    /**
     * Count users with specific permission (NEW)
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
            "JOIN u.roles r " +
            "JOIN r.permissions p " +
            "WHERE p.name = :permissionName")
    Long countByPermission(@Param("permissionName") String permissionName);

    /**
     * Check if user has specific permission (NEW)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM User u " +
            "JOIN u.roles r " +
            "JOIN r.permissions p " +
            "WHERE u.id = :userId AND p.name = :permissionName")
    boolean hasPermission(@Param("userId") UUID userId, @Param("permissionName") String permissionName);

    /**
     * Find active users with specific status
     */
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.emailVerified = true AND u.deletedAt IS NULL")
    Page<User> findActiveUsersByStatus(@Param("status") UserStatus status, Pageable pageable);

    /**
     * Search users with advanced filters including role (NEW)
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.roles r " +
            "WHERE (:keyword IS NULL OR " +
            "      LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "      LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "      LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:emailVerified IS NULL OR u.emailVerified = :emailVerified) AND " +
            "(:roleName IS NULL OR r.name = :roleName) AND " +
            "u.deletedAt IS NULL")
    Page<User> searchUsersWithRole(@Param("keyword") String keyword,
                                   @Param("status") UserStatus status,
                                   @Param("emailVerified") Boolean emailVerified,
                                   @Param("roleName") String roleName,
                                   Pageable pageable);

    /**
     * Find users by email domain (useful for organization filtering)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%@', :domain))")
    Page<User> findByEmailDomain(@Param("domain") String domain, Pageable pageable);

    /**
     * Count active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE' AND u.emailVerified = true AND u.deletedAt IS NULL")
    Long countActiveUsers();

    /**
     * Count users by status
     */
    Long countByStatus(UserStatus status);
}