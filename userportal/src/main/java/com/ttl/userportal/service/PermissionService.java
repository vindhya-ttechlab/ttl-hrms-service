package com.ttl.userportal.service;

import com.ttl.userportal.entity.Permission;
import com.ttl.userportal.entity.RolePermissionMap;
import com.ttl.userportal.repository.PermissionRepository;
import com.ttl.userportal.repository.RolePermissionMapRepository;
import com.ttl.userportal.repository.RoleRepository;
import com.ttl.userportal.repository.UserRoleMapRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionMapRepository rolePermissionMapRepository;

    @Autowired
    private UserRoleMapRepository userRoleMapRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Check if a user has a specific permission for a resource
     * @param userId The user ID
     * @param permissionName The permission name (READ, WRITE, VIEW)
     * @param resource The resource name (e.g., "LEAVE_TYPE", "LEAVE")
     * @return true if user has the permission, false otherwise
     */
    public boolean hasPermission(Integer userId, String permissionName, String resource) {
        // Get user's active role IDs
        List<Integer> roleIds = userRoleMapRepository.findActiveRoleIdsByUserId(userId);
        
        if (roleIds.isEmpty()) {
            log.warn("User {} has no roles assigned", userId);
            return false;
        }

        // Check if any of the user's roles have the required permission
        List<String> permissions = rolePermissionMapRepository.findPermissionNamesByRoleIdsAndResource(roleIds, resource);
        
        boolean hasPermission = permissions.contains(permissionName);
        
        log.debug("Permission check for user {}: permission={}, resource={}, result={}", 
                  userId, permissionName, resource, hasPermission);
        
        return hasPermission;
    }

    /**
     * Get all permissions for a user for a specific resource
     * @param userId The user ID
     * @param resource The resource name
     * @return List of permission names
     */
    public List<String> getUserPermissionsForResource(Integer userId, String resource) {
        List<Integer> roleIds = userRoleMapRepository.findActiveRoleIdsByUserId(userId);
        
        if (roleIds.isEmpty()) {
            return List.of();
        }

        return rolePermissionMapRepository.findPermissionNamesByRoleIdsAndResource(roleIds, resource);
    }

    /**
     * Create a permission if it doesn't exist
     * @param permissionName The permission name
     * @param resource The resource name
     * @param description The description
     * @return The created or existing permission
     */
    @Transactional
    public Permission createPermissionIfNotExists(String permissionName, String resource, String description) {
        return permissionRepository.findByPermissionNameAndResource(permissionName, resource)
                .orElseGet(() -> {
                    Permission permission = new Permission();
                    permission.setPermissionName(permissionName);
                    permission.setResource(resource);
                    permission.setDescription(description);
                    permission.setIsActive(true);
                    permission.setCreatedAt(LocalDateTime.now());
                    permission.setUpdatedAt(LocalDateTime.now());
                    return permissionRepository.save(permission);
                });
    }

    /**
     * Assign a permission to a role
     * @param roleId The role ID
     * @param permissionId The permission ID
     */
    @Transactional
    public void assignPermissionToRole(Integer roleId, Integer permissionId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role with ID " + roleId + " not found");
        }
        
        if (!permissionRepository.existsById(permissionId)) {
            throw new RuntimeException("Permission with ID " + permissionId + " not found");
        }
        
        if (!rolePermissionMapRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            RolePermissionMap map = new RolePermissionMap();
            map.setRoleId(roleId);
            map.setPermissionId(permissionId);
            map.setIsActive(true);
            map.setCreatedAt(LocalDateTime.now());
            map.setUpdatedAt(LocalDateTime.now());
            rolePermissionMapRepository.save(map);
            log.info("Assigned permission ID {} to role ID {}", permissionId, roleId);
        } else {
            log.info("Role ID {} already has permission ID {}", roleId, permissionId);
        }
    }

    /**
     * Remove a permission from a role
     * @param roleId The role ID
     * @param permissionId The permission ID
     */
    @Transactional
    public void removePermissionFromRole(Integer roleId, Integer permissionId) {
        rolePermissionMapRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
        log.info("Removed permission ID {} from role ID {}", permissionId, roleId);
    }
}

