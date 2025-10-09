package com.lovedev.api.mapper;

import com.lovedev.api.model.dto.request.RegisterRequest;
import com.lovedev.api.model.dto.request.UpdateUserRequest;
import com.lovedev.api.model.dto.response.AuditLogResponse;
import com.lovedev.api.model.dto.response.UserResponse;
import com.lovedev.api.model.entity.AuditLog;
import com.lovedev.api.model.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "roles", expression = "java(user.getRoleSet())")
    @Mapping(target = "primaryRole", expression = "java(user.getPrimaryRole())")
    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "userName",
            qualifiedByName = "getUserFullName")
    AuditLogResponse toAuditLogResponse(AuditLog auditLog);

    List<AuditLogResponse> toAuditLogResponseList(List<AuditLog> auditLogs);

    @Named("getUserFullName")
    default String getUserFullName(String firstName) {
        return firstName; // Will be replaced with actual full name in service
    }
}