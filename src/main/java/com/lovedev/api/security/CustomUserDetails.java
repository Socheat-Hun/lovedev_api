package com.lovedev.api.security;

import com.lovedev.api.model.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Custom UserDetails implementation with RBAC support
 * Contains user information and permissions needed for authentication
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public CustomUserDetails(
            UUID id,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            boolean enabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
    }

    /**
     * Build CustomUserDetails from User entity
     * Converts both roles and permissions to Spring Security authorities
     */
    public static CustomUserDetails build(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add roles as authorities (with ROLE_ prefix)
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));

            // Add permissions from each role as authorities (without ROLE_ prefix)
            role.getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            });
        });

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.isActive()
        );
    }

    // ============================================
    // UserDetails Interface Methods
    // ============================================

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // ============================================
    // Custom Helper Methods
    // ============================================

    /**
     * Get only role authorities (authorities that start with ROLE_)
     */
    public Set<String> getRoles() {
        Set<String> roles = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (auth.startsWith("ROLE_")) {
                roles.add(auth);
            }
        }
        return roles;
    }

    /**
     * Get only permission authorities (authorities that don't start with ROLE_)
     */
    public Set<String> getPermissions() {
        Set<String> permissions = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (!auth.startsWith("ROLE_")) {
                permissions.add(auth);
            }
        }
        return permissions;
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(String roleName) {
        String roleToCheck = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleToCheck));
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String permissionName) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permissionName));
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... roleNames) {
        for (String roleName : roleNames) {
            if (hasRole(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has any of the specified permissions
     */
    public boolean hasAnyPermission(String... permissionNames) {
        for (String permissionName : permissionNames) {
            if (hasPermission(permissionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if user is manager or higher
     */
    public boolean isManagerOrHigher() {
        return hasAnyRole("ADMIN", "MANAGER");
    }

    // Custom getters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}