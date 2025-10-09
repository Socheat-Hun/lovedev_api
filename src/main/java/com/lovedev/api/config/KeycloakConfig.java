package com.lovedev.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Keycloak configuration properties
 * This is a simple configuration class for Keycloak OAuth2 integration
 * Uses Spring Security OAuth2 (no deprecated Keycloak adapters)
 */
@Configuration
@ConfigurationProperties(prefix = "app.keycloak")
@Data
public class KeycloakConfig {

    /**
     * Keycloak server URL
     */
    private String serverUrl = "http://localhost:8080";

    /**
     * Keycloak realm name
     */
    private String realm = "lovedev";

    /**
     * Keycloak client ID
     */
    private String clientId = "lovedev-api";

    /**
     * Keycloak client secret
     */
    private String clientSecret;

    /**
     * Enable Keycloak integration
     */
    private boolean enabled = true;

    /**
     * Get full auth server URL
     */
    public String getAuthServerUrl() {
        return serverUrl + "/realms/" + realm;
    }

    /**
     * Get token endpoint
     */
    public String getTokenEndpoint() {
        return getAuthServerUrl() + "/protocol/openid-connect/token";
    }

    /**
     * Get user info endpoint
     */
    public String getUserInfoEndpoint() {
        return getAuthServerUrl() + "/protocol/openid-connect/userinfo";
    }

    /**
     * Get logout endpoint
     */
    public String getLogoutEndpoint() {
        return getAuthServerUrl() + "/protocol/openid-connect/logout";
    }

    /**
     * Get authorization endpoint
     */
    public String getAuthorizationEndpoint() {
        return getAuthServerUrl() + "/protocol/openid-connect/auth";
    }

    /**
     * Get JWKS URI
     */
    public String getJwksUri() {
        return getAuthServerUrl() + "/protocol/openid-connect/certs";
    }

    /**
     * Get issuer URI
     */
    public String getIssuerUri() {
        return getAuthServerUrl();
    }
}