package com.ttl.userportal.repository;

import com.ttl.userportal.entity.UserRoleMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleMapRepository extends JpaRepository<UserRoleMap, Integer> {
    
    List<UserRoleMap> findByUserId(Integer userId);
    
    List<UserRoleMap> findByUserIdAndIsActiveTrue(Integer userId);
    
    List<UserRoleMap> findByRoleId(Integer roleId);
    
    Optional<UserRoleMap> findByUserIdAndRoleId(Integer userId, Integer roleId);
    
    boolean existsByUserIdAndRoleId(Integer userId, Integer roleId);
    
    void deleteByUserIdAndRoleId(Integer userId, Integer roleId);
    
    @Query("SELECT urm.roleId FROM UserRoleMap urm WHERE urm.userId = :userId AND urm.isActive = true")
    List<Integer> findActiveRoleIdsByUserId(@Param("userId") Integer userId);
    
    @Query("SELECT urm.userId FROM UserRoleMap urm WHERE urm.roleId = :roleId AND urm.isActive = true")
    List<Integer> findActiveUserIdsByRoleId(@Param("roleId") Integer roleId);
}

