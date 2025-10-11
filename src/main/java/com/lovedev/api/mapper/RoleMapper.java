package com.lovedev.api.mapper;

import com.lovedev.api.model.dto.request.CreateRoleRequest;
import com.lovedev.api.model.dto.response.RoleResponse;
import com.lovedev.api.model.entity.Role;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isSystemRole", constant = "false")
    Role toEntity(CreateRoleRequest request);

    @Mapping(target = "permissions", expression = "java(role.getPermissionNames())")
    @Mapping(target = "userCount", expression = "java(role.getUsers().size())")
    RoleResponse toResponse(Role role);

    List<RoleResponse> toResponseList(List<Role> roles);
}