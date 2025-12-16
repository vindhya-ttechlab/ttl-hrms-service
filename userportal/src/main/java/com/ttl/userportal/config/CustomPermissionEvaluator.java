package com.ttl.userportal.config;

import com.ttl.userportal.security.CustomUserDetails;
import com.ttl.userportal.service.PrivilegeService;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PrivilegeService privilegeService;

    public CustomPermissionEvaluator(PrivilegeService privilegeService) {
        this.privilegeService = privilegeService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();
        if (permission instanceof String && targetDomainObject instanceof String) {
            String privilegeName = (String) targetDomainObject; // First arg is privilege name
            String resource = (String) permission; // Second arg is resource
            return privilegeService.hasPrivilege(userId, privilegeName, resource);
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, targetType, permission);
    }
}

