package com.lovedev.api.controller;

import com.lovedev.api.model.dto.request.*;
import com.lovedev.api.model.dto.response.ApiResponse;
import com.lovedev.api.model.dto.response.PageResponse;
import com.lovedev.api.model.dto.response.UserResponse;
import com.lovedev.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin user management endpoints")
public class AdminController {

    private final UserService userService;

    @Operation(summary = "Search users", description = "Search and filter users with pagination")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean emailVerified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        UserSearchRequest searchRequest = new UserSearchRequest();
        searchRequest.setKeyword(keyword);
        if (role != null) {
            searchRequest.setRole(com.lovedev.api.model.enums.Role.valueOf(role.toUpperCase()));
        }
        if (status != null) {
            searchRequest.setStatus(com.lovedev.api.model.enums.UserStatus.valueOf(status.toUpperCase()));
        }
        searchRequest.setEmailVerified(emailVerified);

        PageResponse<UserResponse> response = userService.searchUsers(searchRequest, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get user by ID", description = "Get user details by ID")
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update user", description = "Update user profile by ID")
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @Operation(summary = "Update user role", description = "Update user role (Admin only)")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        UserResponse response = userService.updateUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", response));
    }

    @Operation(summary = "Update user status", description = "Update user status (Admin only)")
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request) {
        UserResponse response = userService.updateUserStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("User status updated successfully", response));
    }

    @Operation(summary = "Delete user", description = "Delete user by ID (Soft delete)")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @Operation(summary = "Delete multiple users", description = "Delete multiple users (Soft delete)")
    @DeleteMapping("/users/batch")
    public ResponseEntity<ApiResponse<Void>> deleteUsers(@RequestBody List<UUID> ids) {
        userService.deleteUsers(ids);
        return ResponseEntity.ok(ApiResponse.success("Users deleted successfully", null));
    }

    @Operation(summary = "Add role to user", description = "Add a single role to user (Admin only)")
    @PostMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> addRole(
            @PathVariable UUID id,
            @Valid @RequestBody AddRoleRequest request) {
        UserResponse response = userService.addRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Role added successfully", response));
    }

    @Operation(summary = "Remove role from user", description = "Remove a single role from user (Admin only)")
    @DeleteMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> removeRole(
            @PathVariable UUID id,
            @Valid @RequestBody RemoveRoleRequest request) {
        UserResponse response = userService.removeRole(id, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully", response));
    }

    @Operation(summary = "Update user roles", description = "Replace all user roles (Admin only)")
    @PutMapping("/users/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolesRequest request) {
        UserResponse response = userService.updateRoles(id, request.getRoles());
        return ResponseEntity.ok(ApiResponse.success("User roles updated successfully", response));
    }
}