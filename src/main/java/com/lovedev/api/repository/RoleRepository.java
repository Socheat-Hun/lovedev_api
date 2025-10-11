package com.lovedev.api.repository;

import com.lovedev.api.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    Set<Role> findByNameIn(Set<String> names);

    @Query("SELECT r FROM Role r WHERE r.isSystemRole = :isSystemRole")
    Set<Role> findByIsSystemRole(@Param("isSystemRole") Boolean isSystemRole);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);
}