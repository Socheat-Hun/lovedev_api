package com.lovedev.api.service;

import com.lovedev.api.exception.BadRequestException;
import com.lovedev.api.exception.ResourceNotFoundException;
import com.lovedev.api.exception.UnauthorizedException;
import com.lovedev.api.mapper.UserMapper;
import com.lovedev.api.model.dto.request.*;
import com.lovedev.api.model.dto.response.PageResponse;
import com.lovedev.api.model.dto.response.UserResponse;
import com.lovedev.api.model.entity.Role;
import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.enums.AuditAction;
import com.lovedev.api.model.enums.UserStatus;
import com.lovedev.api.repository.RoleRepository;
import com.lovedev.api.repository.UserRepository;
import com.lovedev.api.security.CustomUserDetails;
import com.lovedev.api.util.SecurityHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final FileStorageService fileStorageService;

    // ============================================
    // Profile Management (Current User)
    // ============================================

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = getCurrentUserEntity();
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = getCurrentUserEntity();

        Map<String, Object> oldValues = captureUserValues(user);

        userMapper.updateUserFromRequest(request, user);
        user = userRepository.save(user);

        Map<String, Object> newValues = captureUserValues(user);

        log.info("User updated: {}", user.getEmail());
        auditService.logAction(user, AuditAction.UPDATE, "User",
                user.getId().toString(), oldValues, newValues, "User profile updated");

        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
        auditService.logAction(user, AuditAction.UPDATE, "Password changed");
    }

    @Transactional
    public UserResponse uploadAvatar(MultipartFile file) {
        User user = getCurrentUserEntity();

        // Delete old avatar if exists
        if (user.getProfilePictureUrl() != null) {
            fileStorageService.deleteFile(user.getProfilePictureUrl());
        }

        // Store new avatar
        String fileName = fileStorageService.storeAvatar(file);
        String fileUrl = fileStorageService.getFileUrl(fileName);

        user.setProfilePictureUrl(fileUrl);
        user = userRepository.save(user);

        log.info("Avatar uploaded for user: {}", user.getEmail());
        auditService.logAction(user, AuditAction.UPLOAD_AVATAR, "Avatar uploaded successfully");

        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteAvatar() {
        User user = getCurrentUserEntity();

        if (user.getProfilePictureUrl() != null) {
            fileStorageService.deleteFile(user.getProfilePictureUrl());
            user.setProfilePictureUrl(null);
            userRepository.save(user);

            log.info("Avatar deleted for user: {}", user.getEmail());
            auditService.logAction(user, AuditAction.UPDATE, "Avatar deleted");
        }
    }

    // ============================================
    // User Management (Admin)
    // ============================================

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(UserSearchRequest searchRequest,
                                                  int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        String keyword = searchRequest.getKeyword();
        if (keyword != null && !keyword.isEmpty()) {
            keyword = keyword.toLowerCase().trim();
        } else {
            keyword = null; // Explicitly set to null
        }

        Page<User> userPage;

        // If role filter is provided, use the role-aware search
        if (searchRequest.getRoleName() != null && !searchRequest.getRoleName().isEmpty()) {
            userPage = userRepository.searchUsersWithRole(
                    keyword,
                    searchRequest.getStatus(),
                    searchRequest.getEmailVerified(),
                    searchRequest.getRoleName(),
                    pageable
            );
        } else {
            // Otherwise use the simpler search without role
            userPage = userRepository.searchUsers(
                    keyword,
                    searchRequest.getStatus(),
                    searchRequest.getEmailVerified(),
                    pageable
            );
        }

        List<UserResponse> userResponses = userMapper.toResponseList(userPage.getContent());

        return PageResponse.<UserResponse>builder()
                .content(userResponses)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .empty(userPage.isEmpty())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Map<String, Object> oldValues = captureUserValues(user);

        userMapper.updateUserFromRequest(request, user);
        user = userRepository.save(user);

        Map<String, Object> newValues = captureUserValues(user);

        User currentUser = getCurrentUserEntity();
        log.info("User {} updated by admin: {}", user.getEmail(), currentUser.getEmail());
        auditService.logAction(currentUser, AuditAction.UPDATE, "User",
                user.getId().toString(), oldValues, newValues, "User profile updated by admin");

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUserStatus(UUID id, UpdateStatusRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserStatus oldStatus = user.getStatus();
        user.setStatus(request.getStatus());
        user = userRepository.save(user);

        User currentUser = getCurrentUserEntity();
        log.info("User {} status changed from {} to {} by {}",
                user.getEmail(), oldStatus, request.getStatus(), currentUser.getEmail());

        Map<String, Object> oldValue = Map.of("status", oldStatus.name());
        Map<String, Object> newValue = Map.of("status", request.getStatus().name());
        auditService.logAction(currentUser, AuditAction.CHANGE_STATUS, "User",
                user.getId().toString(), oldValue, newValue, "User status updated");

        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user); // Soft delete via @SQLDelete

        User currentUser = getCurrentUserEntity();
        log.info("User {} deleted by {}", user.getEmail(), currentUser.getEmail());
        auditService.logAction(currentUser, AuditAction.DELETE, "User",
                user.getId().toString(), null, null, "User deleted");
    }

    @Transactional
    public void deleteUsers(List<UUID> ids) {
        User currentUser = getCurrentUserEntity();

        for (UUID id : ids) {
            try {
                deleteUser(id);
            } catch (ResourceNotFoundException e) {
                log.warn("User not found with id: {}", id);
            }
        }

        log.info("Batch delete: {} users deleted by {}", ids.size(), currentUser.getEmail());
    }

    // ============================================
    // Role Management (Updated for new RBAC)
    // ============================================

    @Transactional
    public UserResponse addRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        if (user.hasRole(role)) {
            throw new BadRequestException("User already has role: " + roleName);
        }

        user.addRole(role);
        user = userRepository.save(user);

        User currentUser = getCurrentUserEntity();
        log.info("Role {} added to user {} by {}", roleName, user.getEmail(), currentUser.getEmail());

        Map<String, Object> newValue = Map.of("role_added", roleName);
        auditService.logAction(currentUser, AuditAction.CHANGE_ROLE, "User",
                user.getId().toString(), null, newValue, "Role added to user");

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse removeRole(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        if (!user.hasRole(role)) {
            throw new BadRequestException("User does not have role: " + roleName);
        }

        // Ensure user has at least one role
        if (user.getRoles().size() == 1) {
            throw new BadRequestException("Cannot remove the last role from user. User must have at least one role.");
        }

        user.removeRole(role);
        user = userRepository.save(user);

        User currentUser = getCurrentUserEntity();
        log.info("Role {} removed from user {} by {}", roleName, user.getEmail(), currentUser.getEmail());

        Map<String, Object> oldValue = Map.of("role_removed", roleName);
        auditService.logAction(currentUser, AuditAction.CHANGE_ROLE, "User",
                user.getId().toString(), oldValue, null, "Role removed from user");

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateRoles(UUID userId, Set<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (roleNames == null || roleNames.isEmpty()) {
            throw new BadRequestException("At least one role is required");
        }

        Set<String> oldRoleNames = user.getRoleNames();

        // Find all roles by names
        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
            newRoles.add(role);
        }

        // Replace all roles
        user.clearRoles();
        newRoles.forEach(user::addRole);
        user = userRepository.save(user);

        User currentUser = getCurrentUserEntity();
        log.info("Roles updated for user {} by {}", user.getEmail(), currentUser.getEmail());

        Map<String, Object> oldValue = Map.of("roles", oldRoleNames);
        Map<String, Object> newValue = Map.of("roles", roleNames);
        auditService.logAction(currentUser, AuditAction.CHANGE_ROLE, "User",
                user.getId().toString(), oldValue, newValue, "User roles updated");

        return userMapper.toResponse(user);
    }

    // Backward compatibility method for single role update
    @Transactional
    public UserResponse updateUserRole(UUID id, UpdateRoleRequest request) {
        // Replace all roles with single role
        return updateRoles(id, Set.of(request.getRoleName()));
    }

    // ============================================
    // Helper Methods
    // ============================================

    private User getCurrentUserEntity() {
        UUID userId = SecurityHelper.getCurrentUserId();

        if (userId == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private Map<String, Object> captureUserValues(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("firstName", user.getFirstName());
        values.put("lastName", user.getLastName());
        values.put("phoneNumber", user.getPhoneNumber());
        values.put("address", user.getAddress());
        values.put("dateOfBirth", user.getDateOfBirth());
        values.put("bio", user.getBio());
        return values;
    }
}