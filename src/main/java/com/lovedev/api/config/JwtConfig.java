package com.lovedev.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtConfig {

    /**
     * Secret key for JWT signing
     */
    private String secret;

    /**
     * JWT token expiration time in milliseconds (default: 15 minutes)
     */
    private Long expiration = 900000L;

    /**
     * Refresh token expiration time in milliseconds (default: 7 days)
     */
    private Long refreshExpiration = 604800000L;

    /**
     * Token issuer
     */
    private String issuer = "lovedev-api";

    /**
     * Token audience
     */
    private String audience = "lovedev-client";

    /**
     * Get expiration in seconds
     */
    public Long getExpirationInSeconds() {
        return expiration / 1000;
    }

    /**
     * Get refresh expiration in seconds
     */
    public Long getRefreshExpirationInSeconds() {
        return refreshExpiration / 1000;
    }
}