package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    
    Optional<Permission> findByPermissionNameAndResource(String permissionName, String resource);
    
    List<Permission> findByResource(String resource);
    
    List<Permission> findByIsActiveTrue();
    
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.isActive = true")
    List<Permission> findActivePermissionsByResource(@Param("resource") String resource);
}

