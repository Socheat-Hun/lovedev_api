package com.lovedev.api.repository;

import com.lovedev.api.model.entity.User;
import com.lovedev.api.model.entity.UserRole;
import com.lovedev.api.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findByUser(User user);

    List<UserRole> findByUserId(UUID userId);

    void deleteByUserAndRole(User user, Role role);

    void deleteByUserId(UUID userId);

    boolean existsByUserAndRole(User user, Role role);
}