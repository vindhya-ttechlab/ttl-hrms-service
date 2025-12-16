package com.ttl.userportal.repository;

import com.ttl.userportal.entity.RolePermissionMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionMapRepository extends JpaRepository<RolePermissionMap, Integer> {
    
    boolean existsByRoleIdAndPermissionId(Integer roleId, Integer permissionId);
    
    void deleteByRoleIdAndPermissionId(Integer roleId, Integer permissionId);
    
    @Query("SELECT rpm.permissionId FROM RolePermissionMap rpm " +
           "WHERE rpm.roleId IN :roleIds AND rpm.isActive = true")
    List<Integer> findPrivilegeIdsByRoleIds(@Param("roleIds") List<Integer> roleIds);
    
    @Query("SELECT p.privilegeName FROM Privilege p " +
           "INNER JOIN RolePermissionMap rpm ON p.privilegeId = rpm.permissionId " +
           "WHERE rpm.roleId IN :roleIds AND rpm.isActive = true AND p.isActive = true " +
           "AND p.resource = :resource")
    List<String> findPrivilegeNamesByRoleIdsAndResource(
            @Param("roleIds") List<Integer> roleIds, 
            @Param("resource") String resource);
    
    List<RolePermissionMap> findByRoleIdAndIsActiveTrue(Integer roleId);
}

