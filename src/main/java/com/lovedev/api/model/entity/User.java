package com.lovedev.api.model.entity;

import com.lovedev.api.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_status", columnList = "status")
})
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.INACTIVE;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Many-to-Many relationship with Roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================
    // Basic Helper Methods
    // ============================================

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE && emailVerified;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // ============================================
    // Role Management Methods
    // ============================================

    /**
     * Get role names
     */
    public Set<String> getRoleNames() {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Add role to user
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }

    /**
     * Remove role from user
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    /**
     * Clear all roles
     */
    public void clearRoles() {
        this.roles.clear();
    }

    /**
     * Set roles (replace all existing roles)
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    /**
     * Check if user has role by name
     */
    public boolean hasRoleName(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(Role... rolesToCheck) {
        return Arrays.stream(rolesToCheck)
                .anyMatch(roles::contains);
    }

    /**
     * Check if user has any of the specified role names
     */
    public boolean hasAnyRoleName(String... roleNames) {
        Set<String> userRoleNames = getRoleNames();
        return Arrays.stream(roleNames)
                .anyMatch(userRoleNames::contains);
    }

    /**
     * Check if user has all of the specified roles
     */
    public boolean hasAllRoles(Role... rolesToCheck) {
        return Arrays.stream(rolesToCheck)
                .allMatch(roles::contains);
    }

    // ============================================
    // Permission Helper Methods
    // ============================================

    /**
     * Get all permissions from all roles
     */
    public Set<Permission> getPermissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Get permission names
     */
    public Set<String> getPermissionNames() {
        return getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Check if user has specific permission by name
     */
    public boolean hasPermission(String permissionName) {
        return roles.stream()
                .anyMatch(role -> role.hasPermission(permissionName));
    }

    /**
     * Check if user has permission for resource and action
     */
    public boolean hasPermission(String resource, String action) {
        return getPermissions().stream()
                .anyMatch(p -> p.matches(resource, action));
    }

    /**
     * Check if user has any of the specified permissions
     */
    public boolean hasAnyPermission(String... permissionNames) {
        Set<String> userPermissions = getPermissionNames();
        return Arrays.stream(permissionNames)
                .anyMatch(userPermissions::contains);
    }

    /**
     * Check if user has all of the specified permissions
     */
    public boolean hasAllPermissions(String... permissionNames) {
        Set<String> userPermissions = getPermissionNames();
        return Arrays.stream(permissionNames)
                .allMatch(userPermissions::contains);
    }

    // ============================================
    // Convenience Role Check Methods
    // ============================================

    /**
     * Get primary role (highest privilege role)
     * Priority: ADMIN > MANAGER > EMPLOYEE > USER
     */
    public Role getPrimaryRole() {
        if (hasRoleName("ROLE_ADMIN")) {
            return roles.stream()
                    .filter(r -> r.getName().equals("ROLE_ADMIN"))
                    .findFirst()
                    .orElse(null);
        }
        if (hasRoleName("ROLE_MANAGER")) {
            return roles.stream()
                    .filter(r -> r.getName().equals("ROLE_MANAGER"))
                    .findFirst()
                    .orElse(null);
        }
        if (hasRoleName("ROLE_EMPLOYEE")) {
            return roles.stream()
                    .filter(r -> r.getName().equals("ROLE_EMPLOYEE"))
                    .findFirst()
                    .orElse(null);
        }
        return roles.stream()
                .filter(r -> r.getName().equals("ROLE_USER"))
                .findFirst()
                .orElse(roles.stream().findFirst().orElse(null));
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRoleName("ROLE_ADMIN");
    }

    /**
     * Check if user is manager
     */
    public boolean isManager() {
        return hasRoleName("ROLE_MANAGER");
    }

    /**
     * Check if user is employee
     */
    public boolean isEmployee() {
        return hasRoleName("ROLE_EMPLOYEE");
    }

    /**
     * Check if user has basic user role
     */
    public boolean isBasicUser() {
        return hasRoleName("ROLE_USER");
    }

    /**
     * Check if user is manager or higher (MANAGER or ADMIN)
     */
    public boolean isManagerOrHigher() {
        return hasAnyRoleName("ROLE_ADMIN", "ROLE_MANAGER");
    }

    /**
     * Check if user is employee or higher (EMPLOYEE, MANAGER, or ADMIN)
     */
    public boolean isEmployeeOrHigher() {
        return hasAnyRoleName("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_EMPLOYEE");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}