package com.ttl.userportal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration for method-level security using @PreAuthorize
 * Enables custom permission checking via hasPermission() expression
 * 
 * Usage in controllers:
 * @PreAuthorize("hasPermission('VIEW', 'LEAVE_TYPE')")
 * @PreAuthorize("hasPermission('WRITE', 'LEAVE_TYPE')")
 * @PreAuthorize("hasPermission('EDIT', 'LEAVE_TYPE')")
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {

    @Autowired
    private CustomPermissionEvaluator permissionEvaluator;

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }
}

