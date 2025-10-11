package com.lovedev.api.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter for extracting roles from Keycloak JWT tokens
 * Converts Keycloak realm roles and resource access roles to Spring Security GrantedAuthorities
 */
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    /**
     * Extract roles from JWT token and convert to GrantedAuthorities
     *
     * @param jwt The JWT token from Keycloak
     * @return Collection of GrantedAuthority objects
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Extract realm roles
        Collection<String> realmRoles = extractRealmRoles(jwt);
        if (realmRoles != null && !realmRoles.isEmpty()) {
            authorities.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
        }

        // Extract resource access roles (client-specific roles)
        Collection<String> resourceRoles = extractResourceRoles(jwt);
        if (resourceRoles != null && !resourceRoles.isEmpty()) {
            authorities.addAll(resourceRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
        }

        return authorities;
    }

    /**
     * Extract realm roles from JWT token
     * Realm roles are global roles in Keycloak
     *
     * @param jwt The JWT token
     * @return Collection of realm role names
     */
    @SuppressWarnings("unchecked")
    private Collection<String> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (Collection<String>) realmAccess.get("roles");
        }

        return new ArrayList<>();
    }

    /**
     * Extract resource access roles from JWT token
     * Resource access roles are client-specific roles in Keycloak
     *
     * @param jwt The JWT token
     * @return Collection of resource role names
     */
    @SuppressWarnings("unchecked")
    private Collection<String> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        List<String> roles = new ArrayList<>();

        if (resourceAccess != null) {
            // Iterate through each client's roles
            resourceAccess.values().forEach(resource -> {
                if (resource instanceof Map) {
                    Map<String, Object> resourceMap = (Map<String, Object>) resource;
                    if (resourceMap.containsKey("roles")) {
                        Collection<String> resourceRoles = (Collection<String>) resourceMap.get("roles");
                        roles.addAll(resourceRoles);
                    }
                }
            });
        }

        return roles;
    }

    /**
     * Extract roles from a specific client
     *
     * @param jwt The JWT token
     * @param clientId The client ID to extract roles from
     * @return Collection of roles for the specific client
     */
    @SuppressWarnings("unchecked")
    public Collection<String> extractClientRoles(Jwt jwt, String clientId) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess != null && resourceAccess.containsKey(clientId)) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientAccess != null && clientAccess.containsKey("roles")) {
                return (Collection<String>) clientAccess.get("roles");
            }
        }

        return new ArrayList<>();
    }

    /**
     * Extract groups from JWT token
     * Groups can also be used for authorization
     *
     * @param jwt The JWT token
     * @return Collection of group names
     */
    @SuppressWarnings("unchecked")
    public Collection<String> extractGroups(Jwt jwt) {
        Collection<String> groups = jwt.getClaim("groups");
        return groups != null ? groups : new ArrayList<>();
    }

    /**
     * Check if JWT contains a specific role
     *
     * @param jwt The JWT token
     * @param roleName The role name to check
     * @return true if the role exists in the token
     */
    public boolean hasRole(Jwt jwt, String roleName) {
        Collection<GrantedAuthority> authorities = convert(jwt);
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + roleName));
    }

    /**
     * Get preferred username from JWT
     *
     * @param jwt The JWT token
     * @return The preferred username
     */
    public String getPreferredUsername(Jwt jwt) {
        return jwt.getClaim("preferred_username");
    }

    /**
     * Get email from JWT
     *
     * @param jwt The JWT token
     * @return The email address
     */
    public String getEmail(Jwt jwt) {
        return jwt.getClaim("email");
    }

    /**
     * Get full name from JWT
     *
     * @param jwt The JWT token
     * @return The full name (combines given_name and family_name)
     */
    public String getFullName(Jwt jwt) {
        String givenName = jwt.getClaim("given_name");
        String familyName = jwt.getClaim("family_name");

        if (givenName != null && familyName != null) {
            return givenName + " " + familyName;
        } else if (givenName != null) {
            return givenName;
        } else if (familyName != null) {
            return familyName;
        }

        return jwt.getClaim("name");
    }

    /**
     * Check if email is verified
     *
     * @param jwt The JWT token
     * @return true if email is verified
     */
    public boolean isEmailVerified(Jwt jwt) {
        Boolean emailVerified = jwt.getClaim("email_verified");
        return emailVerified != null && emailVerified;
    }
}