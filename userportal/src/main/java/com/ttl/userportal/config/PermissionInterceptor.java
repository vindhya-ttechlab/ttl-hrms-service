package com.ttl.userportal.config;

import com.ttl.userportal.security.CustomUserDetails;
import com.ttl.userportal.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // Skip permission check for public endpoints
        String requestPath = request.getRequestURI();
        if (requestPath.contains("/login") || 
            requestPath.contains("/create-user") ||
            requestPath.equals("/users/create-user") ||
            requestPath.equals("/api/users/create-user")) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresPermission annotation = handlerMethod.getMethodAnnotation(RequiresPermission.class);

        if (annotation == null) {
            return true; // No permission check required
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"User not authenticated\"}");
            return false;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getUserId();

        String requiredPermission = annotation.permission();
        String resource = annotation.resource();

        boolean hasPermission = permissionService.hasPermission(userId, requiredPermission, resource);

        if (!hasPermission) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied. Required permission: " + 
                                      requiredPermission + " on resource: " + resource + "\"}");
            return false;
        }

        return true;
    }
}

