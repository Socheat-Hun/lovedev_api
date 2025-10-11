package com.lovedev.api.repository;

import com.lovedev.api.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    Optional<Permission> findByResourceAndAction(String resource, String action);

    Set<Permission> findByResource(String resource);

    Set<Permission> findByNameIn(Set<String> names);

    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action LIKE :action%")
    Set<Permission> findByResourceAndActionStartsWith(
            @Param("resource") String resource,
            @Param("action") String action
    );
}