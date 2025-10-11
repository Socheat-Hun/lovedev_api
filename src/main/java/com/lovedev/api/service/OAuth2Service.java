package com.lovedev.api.service;

import com.lovedev.api.exception.ResourceNotFoundException;
import com.lovedev.api.model.entity.RefreshToken;
import com.lovedev.api.model.entity.Role;
import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.enums.AuditAction;
import com.lovedev.api.model.enums.UserStatus;
import com.lovedev.api.repository.RoleRepository;
import com.lovedev.api.repository.UserRepository;
import com.lovedev.api.security.CustomUserDetails;
import com.lovedev.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling OAuth2 authentication (Google, GitHub, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final AuditService auditService;

    /**
     * Process OAuth2 login/registration
     * If user exists, login. If not, create new user with USER role.
     */
    @Transactional
    public OAuth2AuthResult processOAuth2Login(OAuth2User oAuth2User, String registrationId) {
        // Extract user info from OAuth2 provider
        String email = extractEmail(oAuth2User, registrationId);
        String firstName = extractFirstName(oAuth2User, registrationId);
        String lastName = extractLastName(oAuth2User, registrationId);
        String profilePictureUrl = extractProfilePicture(oAuth2User, registrationId);

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not provided by OAuth2 provider");
        }

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createOAuth2User(email, firstName, lastName, profilePictureUrl, registrationId));

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());

        // Update profile picture if available and user doesn't have one
        if (profilePictureUrl != null && user.getProfilePictureUrl() == null) {
            user.setProfilePictureUrl(profilePictureUrl);
        }

        user = userRepository.save(user);

        // Generate tokens
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = tokenService.createRefreshToken(user);

        log.info("OAuth2 login successful for user: {} via {}", email, registrationId);

        // Log audit
        auditService.logAction(user, AuditAction.LOGIN,
                String.format("OAuth2 login via %s", registrationId));

        return OAuth2AuthResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtTokenProvider.getJwtExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .isNewUser(user.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5)))
                .build();
    }

    /**
     * Create new user from OAuth2 data with default USER role
     */
    private User createOAuth2User(String email, String firstName, String lastName,
                                  String profilePictureUrl, String provider) {
        // Get default USER role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default USER role not found. Please contact administrator."));

        // Create user
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password for OAuth2 users
                .firstName(firstName != null ? firstName : "User")
                .lastName(lastName != null ? lastName : "")
                .profilePictureUrl(profilePictureUrl)
                .status(UserStatus.ACTIVE) // OAuth2 users are active by default
                .emailVerified(true) // OAuth2 email is pre-verified
                .roles(new HashSet<>())
                .build();

        // Assign USER role
        user.addRole(userRole);

        user = userRepository.save(user);

        log.info("New OAuth2 user created: {} via {}", email, provider);

        // Log audit
        auditService.logAction(user, AuditAction.REGISTER,
                String.format("User registered via OAuth2 (%s)", provider));

        return user;
    }

    // ============================================
    // OAuth2 Data Extraction Methods
    // ============================================

    /**
     * Extract email from OAuth2 user attributes
     */
    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        switch (registrationId.toLowerCase()) {
            case "google":
                return (String) attributes.get("email");

            case "github":
                // GitHub may not provide email in basic scope
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Fallback: use login + @github.com
                    String login = (String) attributes.get("login");
                    email = login + "@github.users.noreply.github.com";
                }
                return email;

            case "facebook":
                return (String) attributes.get("email");

            default:
                return (String) attributes.get("email");
        }
    }

    /**
     * Extract first name from OAuth2 user attributes
     */
    private String extractFirstName(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        switch (registrationId.toLowerCase()) {
            case "google":
                return (String) attributes.get("given_name");

            case "github":
                String name = (String) attributes.get("name");
                if (name != null && name.contains(" ")) {
                    return name.split(" ")[0];
                }
                return name != null ? name : (String) attributes.get("login");

            case "facebook":
                String fbName = (String) attributes.get("name");
                if (fbName != null && fbName.contains(" ")) {
                    return fbName.split(" ")[0];
                }
                return fbName;

            default:
                return (String) attributes.get("given_name");
        }
    }

    /**
     * Extract last name from OAuth2 user attributes
     */
    private String extractLastName(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        switch (registrationId.toLowerCase()) {
            case "google":
                return (String) attributes.get("family_name");

            case "github":
                String name = (String) attributes.get("name");
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ");
                    return parts.length > 1 ? parts[parts.length - 1] : "";
                }
                return "";

            case "facebook":
                String fbName = (String) attributes.get("name");
                if (fbName != null && fbName.contains(" ")) {
                    String[] parts = fbName.split(" ");
                    return parts.length > 1 ? parts[parts.length - 1] : "";
                }
                return "";

            default:
                return (String) attributes.get("family_name");
        }
    }

    /**
     * Extract profile picture URL from OAuth2 user attributes
     */
    private String extractProfilePicture(OAuth2User oAuth2User, String registrationId) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        switch (registrationId.toLowerCase()) {
            case "google":
                return (String) attributes.get("picture");

            case "github":
                return (String) attributes.get("avatar_url");

            case "facebook":
                Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
                if (picture != null) {
                    Map<String, Object> data = (Map<String, Object>) picture.get("data");
                    if (data != null) {
                        return (String) data.get("url");
                    }
                }
                return null;

            default:
                return (String) attributes.get("picture");
        }
    }

    // ============================================
    // Result DTO
    // ============================================

    @lombok.Data
    @lombok.Builder
    public static class OAuth2AuthResult {
        private String accessToken;
        private String refreshToken;
        private Long expiresIn;
        private UUID userId;
        private String email;
        private boolean isNewUser;
    }
}