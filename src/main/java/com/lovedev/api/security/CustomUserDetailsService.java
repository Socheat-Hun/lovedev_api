package com.lovedev.api.security;

import com.lovedev.api.exception.ResourceNotFoundException;
import com.lovedev.api.model.entity.User;
import com.lovedev.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Custom UserDetailsService implementation for loading user-specific data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        // Try to parse as UUID first (for token-based auth)
        try {
            UUID userId = UUID.fromString(username);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + username));

            log.debug("User found by ID: {}", user.getEmail());
            return CustomUserDetails.build(user);
        } catch (IllegalArgumentException e) {
            // If not a UUID, treat as email
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

            log.debug("User found by email: {}", user.getEmail());
            return CustomUserDetails.build(user);
        }
    }

    /**
     * Load user by ID (used for JWT authentication)
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        log.debug("Loading user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return CustomUserDetails.build(user);
    }
}