package com.ttl.userportal.service;

import com.ttl.userportal.entity.Privilege;
import com.ttl.userportal.entity.RolePermissionMap;
import com.ttl.userportal.repository.PrivilegeRepository;
import com.ttl.userportal.repository.RolePermissionMapRepository;
import com.ttl.userportal.repository.RoleRepository;
import com.ttl.userportal.repository.UserRoleMapRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing privileges and role-privilege mappings
 * Privileges are actions (VIEW, READ, WRITE, EDIT) on resources (LEAVE_TYPE, LEAVE, etc.)
 */
@Slf4j
@Service
public class PrivilegeService {

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private RolePermissionMapRepository rolePermissionMapRepository;

    @Autowired
    private UserRoleMapRepository userRoleMapRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Check if a user has a specific privilege for a resource
     * @param userId The user ID
     * @param privilegeName The privilege name (VIEW, READ, WRITE, EDIT)
     * @param resource The resource name (e.g., "LEAVE_TYPE", "LEAVE")
     * @return true if user has the privilege, false otherwise
     */
    public boolean hasPrivilege(Integer userId, String privilegeName, String resource) {
        // Get user's active role IDs
        List<Integer> roleIds = userRoleMapRepository.findActiveRoleIdsByUserId(userId);
        
        if (roleIds.isEmpty()) {
            log.warn("User {} has no roles assigned", userId);
            return false;
        }

        // Check if any of the user's roles have the required privilege
        List<String> privileges = rolePermissionMapRepository.findPrivilegeNamesByRoleIdsAndResource(roleIds, resource);
        
        boolean hasPrivilege = privileges.contains(privilegeName);
        
        log.debug("Privilege check for user {}: privilege={}, resource={}, result={}", 
                  userId, privilegeName, resource, hasPrivilege);
        
        return hasPrivilege;
    }

    /**
     * Get all privileges for a user for a specific resource
     * @param userId The user ID
     * @param resource The resource name
     * @return List of privilege names
     */
    public List<String> getUserPrivilegesForResource(Integer userId, String resource) {
        List<Integer> roleIds = userRoleMapRepository.findActiveRoleIdsByUserId(userId);
        
        if (roleIds.isEmpty()) {
            return List.of();
        }

        return rolePermissionMapRepository.findPrivilegeNamesByRoleIdsAndResource(roleIds, resource);
    }

    /**
     * Create a privilege if it doesn't exist
     * @param privilegeName The privilege name (VIEW, READ, WRITE, EDIT)
     * @param resource The resource name (LEAVE_TYPE, LEAVE, etc.)
     * @param description The description
     * @return The created or existing privilege
     */
    @Transactional
    public Privilege createPrivilegeIfNotExists(String privilegeName, String resource, String description) {
        return privilegeRepository.findByPrivilegeNameAndResource(privilegeName, resource)
                .orElseGet(() -> {
                    Privilege privilege = new Privilege();
                    privilege.setPrivilegeName(privilegeName);
                    privilege.setResource(resource);
                    privilege.setDescription(description);
                    privilege.setIsActive(true);
                    privilege.setCreatedAt(LocalDateTime.now());
                    privilege.setUpdatedAt(LocalDateTime.now());
                    return privilegeRepository.save(privilege);
                });
    }

    /**
     * Assign a privilege to a role
     * @param roleId The role ID
     * @param privilegeId The privilege ID
     */
    @Transactional
    public void assignPrivilegeToRole(Integer roleId, Integer privilegeId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role with ID " + roleId + " not found");
        }
        
        if (!privilegeRepository.existsById(privilegeId)) {
            throw new RuntimeException("Privilege with ID " + privilegeId + " not found");
        }
        
        if (!rolePermissionMapRepository.existsByRoleIdAndPermissionId(roleId, privilegeId)) {
            RolePermissionMap map = new RolePermissionMap();
            map.setRoleId(roleId);
            map.setPermissionId(privilegeId);
            map.setIsActive(true);
            map.setCreatedAt(LocalDateTime.now());
            map.setUpdatedAt(LocalDateTime.now());
            rolePermissionMapRepository.save(map);
            log.info("Assigned privilege ID {} to role ID {}", privilegeId, roleId);
        } else {
            log.info("Role ID {} already has privilege ID {}", roleId, privilegeId);
        }
    }

    /**
     * Remove a privilege from a role
     * @param roleId The role ID
     * @param privilegeId The privilege ID
     */
    @Transactional
    public void removePrivilegeFromRole(Integer roleId, Integer privilegeId) {
        rolePermissionMapRepository.deleteByRoleIdAndPermissionId(roleId, privilegeId);
        log.info("Removed privilege ID {} from role ID {}", privilegeId, roleId);
    }
}

