package com.ttl.userportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ttl.userportal.entity.Privilege;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Integer> {
    
    Optional<Privilege> findByPrivilegeNameAndResource(String permissionName, String resource);
    
    List<Privilege> findByResource(String resource);
    
    List<Privilege> findByIsActiveTrue();
    
    @Query("SELECT p FROM Privilege p WHERE p.resource = :resource AND p.isActive = true")
    List<Privilege> findActivePermissionsByResource(@Param("resource") String resource);
}

