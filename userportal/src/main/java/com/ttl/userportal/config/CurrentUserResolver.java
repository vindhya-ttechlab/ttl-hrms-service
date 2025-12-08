package com.ttl.userportal.config;

import com.ttl.userportal.entity.Users;
import com.ttl.userportal.repository.UserRoleMapRepository;
import com.ttl.userportal.security.CustomUserDetails;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

/**
 * Custom argument resolver for @CurrentUser annotation
 * Extracts user information from SecurityContext and converts to custom UserDetails model
 */
@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserRoleMapRepository userRoleMapRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class) &&
               parameter.getParameterType().equals(com.ttl.userportal.util.model.UserDetails.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            System.out.println("DEBUG: No authentication found in SecurityContext");
            return null;
        }
        
        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            System.out.println("DEBUG: Principal is not CustomUserDetails, type: " + 
                authentication.getPrincipal().getClass().getSimpleName());
            return null;
        }
        
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Users user = customUserDetails.getUser();
        
        System.out.println("DEBUG: Resolving UserDetails for user: " + user.getEmail() + " (ID: " + user.getId() + ")");
        
        // Convert Users entity to custom UserDetails model
        UserDetails userDetails = new UserDetails();
        userDetails.setUser_id(user.getId().longValue());
        userDetails.setUser_name(user.getName());
        userDetails.setFirst_name(user.getName()); // Assuming name contains first name
        userDetails.setEmail(user.getEmail());
        userDetails.setContact_no(user.getPhone());
        userDetails.setDesignation(user.getPosition());
        userDetails.setDepartment(user.getDepartment());
        userDetails.setEmployee(user.getEmpCode());
        userDetails.setReporting_manager_id(user.getManager() != null ? user.getManager().longValue() : null);
        userDetails.setUser_status(user.getStatus() != null ? user.getStatus().name() : "Active");
        
        // Get user's role IDs
        List<Integer> roleIds = userRoleMapRepository.findActiveRoleIdsByUserId(user.getId());
        userDetails.setRoleIds(roleIds);
        
        // Set primary role ID (first role, or default to 1 for Employee)
        Integer primaryRoleId = roleIds.isEmpty() ? 1 : roleIds.get(0);
        userDetails.setPrimaryRoleId(primaryRoleId);
        
        System.out.println("DEBUG: Created UserDetails with ID: " + userDetails.getUser_id() + 
                          ", Name: " + userDetails.getUser_name() + 
                          ", Email: " + userDetails.getEmail() +
                          ", Primary Role ID: " + primaryRoleId);
        
        return userDetails;
    }
}
