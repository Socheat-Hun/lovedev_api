package com.lovedev.api.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response containing tokens and user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response with tokens and user details")
public class AuthResponse {

    @Schema(
            description = "JWT access token for API authentication",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
            description = "Refresh token for obtaining new access tokens",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String refreshToken;

    @Schema(
            description = "Token type",
            example = "Bearer",
            defaultValue = "Bearer"
    )
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(
            description = "Access token expiration time in milliseconds",
            example = "900000"
    )
    private Long expiresIn;

    @Schema(description = "Authenticated user information")
    private UserResponse user;

    /**
     * Create an AuthResponse with tokens and user info
     */
    public static AuthResponse of(String accessToken, String refreshToken, Long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }

    /**
     * Create an AuthResponse with only tokens (for refresh token flow)
     */
    public static AuthResponse ofTokens(String accessToken, String refreshToken, Long expiresIn) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * Create an AuthResponse for registration (without tokens)
     */
    public static AuthResponse ofUser(UserResponse user) {
        return AuthResponse.builder()
                .user(user)
                .build();
    }
}