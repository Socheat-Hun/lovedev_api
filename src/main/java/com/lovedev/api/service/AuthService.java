package com.lovedev.api.service;

import com.lovedev.api.exception.BadRequestException;
import com.lovedev.api.exception.EmailAlreadyExistsException;
import com.lovedev.api.exception.ResourceNotFoundException;
import com.lovedev.api.exception.TokenException;
import com.lovedev.api.mapper.UserMapper;
import com.lovedev.api.model.dto.request.*;
import com.lovedev.api.model.dto.response.AuthResponse;
import com.lovedev.api.model.dto.response.UserResponse;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Get USER role (default role for new users)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default USER role not found. Please contact administrator."));

        // Create user
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.INACTIVE);
        user.setEmailVerified(false);

        // Assign USER role
        user.setRoles(new HashSet<>());
        user.addRole(userRole);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusHours(24));

        user = userRepository.save(user);
        log.info("New user registered: {} with role: {}", user.getEmail(), userRole.getName());

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken, user.getFirstName());

        // Log audit
        auditService.logAction(user, AuditAction.REGISTER, "User registered successfully");

        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.builder()
                .user(userResponse)
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new TokenException("Invalid verification token"));

        if (user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenException("Verification token has expired");
        }

        if (user.getEmailVerified()) {
            throw new BadRequestException("Email already verified");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);

        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        // Log audit
        auditService.logAction(user, AuditAction.VERIFY_EMAIL, "Email verified successfully");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new BadRequestException("Your account has been banned");
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        RefreshToken refreshToken = tokenService.createRefreshToken(user);

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        // Log audit
        auditService.logAction(user, AuditAction.LOGIN, "User logged in successfully");

        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getJwtExpirationMs(),
                userResponse
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = tokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        // Generate new access token
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtTokenProvider.getJwtExpirationMs(),
                userResponse
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
        log.info("User logged out");
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Generate password reset token
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));

        userRepository.save(user);
        log.info("Password reset requested for user: {}", user.getEmail());

        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getFirstName());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new TokenException("Invalid password reset token"));

        if (user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);

        userRepository.save(user);
        log.info("Password reset for user: {}", user.getEmail());

        // Revoke all refresh tokens
        tokenService.revokeAllUserTokens(user);

        // Log audit
        auditService.logAction(user, AuditAction.RESET_PASSWORD, "Password reset successfully");
    }
}