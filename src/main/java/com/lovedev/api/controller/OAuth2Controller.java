package com.lovedev.api.controller;

import com.lovedev.api.model.dto.response.ApiResponse;
import com.lovedev.api.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2", description = "OAuth2 authentication endpoints")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Operation(
            summary = "OAuth2 callback handler",
            description = "Handles OAuth2 authentication callback and returns JWT tokens"
    )
    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<OAuth2Service.OAuth2AuthResult>> handleOAuth2Callback(
            Authentication authentication) {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid authentication type"));
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        try {
            OAuth2Service.OAuth2AuthResult result = oAuth2Service.processOAuth2Login(oAuth2User, registrationId);

            String message = result.isNewUser()
                    ? "Account created successfully via " + registrationId
                    : "Login successful via " + registrationId;

            return ResponseEntity.ok(ApiResponse.success(message, result));

        } catch (Exception e) {
            log.error("OAuth2 callback failed", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("OAuth2 authentication failed: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Get OAuth2 user info",
            description = "Get current OAuth2 user information"
    )
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOAuth2User(
            Authentication authentication) {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated via OAuth2"));
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("provider", provider);
        userInfo.put("attributes", oAuth2User.getAttributes());
        userInfo.put("authorities", oAuth2User.getAuthorities());

        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @Operation(
            summary = "OAuth2 error handler",
            description = "Handles OAuth2 authentication errors"
    )
    @GetMapping("/error")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleOAuth2Error(
            @Parameter(description = "Error code") @RequestParam(required = false) String error,
            @Parameter(description = "Error description") @RequestParam(required = false) String error_description) {

        log.error("OAuth2 error: {} - {}", error, error_description);

        Map<String, Object> errorInfo = new HashMap<>();
        errorInfo.put("error", error != null ? error : "oauth2_error");
        errorInfo.put("description", error_description != null ? error_description : "OAuth2 authentication failed");
        errorInfo.put("redirectUri", redirectUri);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("OAuth2 authentication failed", errorInfo));
    }

    @Operation(
            summary = "Get OAuth2 login URLs",
            description = "Get OAuth2 login URLs for different providers"
    )
    @GetMapping("/login-urls")
    public ResponseEntity<ApiResponse<Map<String, String>>> getOAuth2LoginUrls() {
        Map<String, String> loginUrls = new HashMap<>();

        // Generate OAuth2 authorization URLs
        String baseUrl = "http://localhost:8080"; // Should be configurable

        loginUrls.put("google", baseUrl + "/oauth2/authorization/google");
        loginUrls.put("github", baseUrl + "/oauth2/authorization/github");
        loginUrls.put("facebook", baseUrl + "/oauth2/authorization/facebook");

        return ResponseEntity.ok(ApiResponse.success("OAuth2 login URLs", loginUrls));
    }

    @Operation(
            summary = "Check OAuth2 provider status",
            description = "Check if OAuth2 providers are configured"
    )
    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOAuth2Providers() {
        Map<String, Object> providers = new HashMap<>();

        // You can check if providers are configured by checking environment variables
        Map<String, Boolean> providerStatus = new HashMap<>();
        providerStatus.put("google", isProviderConfigured("GOOGLE_CLIENT_ID"));
        providerStatus.put("github", isProviderConfigured("GITHUB_CLIENT_ID"));
        providerStatus.put("facebook", isProviderConfigured("FACEBOOK_CLIENT_ID"));

        providers.put("providers", providerStatus);
        providers.put("redirectUri", redirectUri);

        return ResponseEntity.ok(ApiResponse.success("OAuth2 providers status", providers));
    }

    @Operation(
            summary = "Unlink OAuth2 provider",
            description = "Unlink OAuth2 provider from user account (if user has password)"
    )
    @DeleteMapping("/unlink/{provider}")
    public ResponseEntity<ApiResponse<String>> unlinkOAuth2Provider(
            @Parameter(description = "OAuth2 provider name") @PathVariable String provider,
            Authentication authentication) {

        // TODO: Implement OAuth2 unlinking logic
        // Check if user has password or other OAuth2 providers before unlinking

        return ResponseEntity.ok(
                ApiResponse.success("OAuth2 provider unlinked: " + provider)
        );
    }

    /**
     * Check if OAuth2 provider is configured
     */
    private boolean isProviderConfigured(String envVar) {
        String value = System.getenv(envVar);
        return value != null && !value.isEmpty() && !value.startsWith("your-");
    }
}