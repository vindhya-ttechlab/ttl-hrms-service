package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.*;
import com.ttl.userportal.entity.Role;
import com.ttl.userportal.entity.Users;
import com.ttl.userportal.service.PrivilegeService;
import com.ttl.userportal.service.UserService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController 
{

    @Autowired
    private UserService userService;

    @Autowired
    private PrivilegeService privilegeService;

    @PostMapping("/create-user")
    public ResponseEntity<Map<String, Object>> createUser(
            @ModelAttribute CreateUserRequest createUserRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (createUserRequest == null) {
                response.put("error", "Request body is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            Users createdUser = userService.createUser(createUserRequest);
            response.put("message", "User created successfully. Password has been auto-generated and sent to the user's email.");
            response.put("userId", createdUser.getId());
            response.put("email", createdUser.getEmail());
            response.put("passwordSentViaEmail", true);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>>loginUser(@RequestBody LoginRequest loginRequest)
    {
        Map<String,Object>response=new HashMap<>();
        try{
            LoginResponse result=userService.loginUser(loginRequest);
            response.put("DATA",result);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        }catch (Exception e)
        {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/employee-data")
    public ResponseEntity<Map<String,Object>>userDetails(@CurrentUser UserDetails userDetails)
    {
        Map<String,Object>response=new HashMap<>();
        try{
            EmployeeDTO result=userService.userDetails(userDetails);
            response.put("DATA",result);
            return ResponseEntity.ok(response);
        }catch (Exception e)
        {
            response.put("error", e.getMessage());
            response.put("details", "Failed to fetch employee data for user: " + userDetails.getUser_name());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    @GetMapping("/list")
    public List<EmployeeDetails> getEmployees(@CurrentUser UserDetails userDetails,@RequestParam(required = false) String search) {
        if (search == null || search.trim().isEmpty()) {
            return userService.getAllEmployees();
        } else {
            return userService.searchEmployees(search.trim());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest changePasswordRequest,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null || userDetails.getUser_id() == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            boolean success = userService.changePassword(
                    userDetails.getUser_id().intValue(), 
                    changePasswordRequest);
            
            if (success) {
                response.put("message", "Password changed successfully");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to change password");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Check if user has a specific permission for a resource
     * Used by frontend to determine UI visibility
     * Protected with Spring Security - user must be authenticated
     */
    @RequestMapping(value = "/api/permissions/check", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> checkPermission(
            @RequestParam String permission,
            @RequestParam String resource,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("hasPermission", false);
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            boolean hasPermission = privilegeService.hasPrivilege(
                    userDetails.getUser_id().intValue(), permission, resource);
            
            response.put("hasPermission", hasPermission);
            response.put("permission", permission);
            response.put("resource", resource);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("hasPermission", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all permissions for a user for a specific resource
     * Used by frontend to determine UI visibility
     * Protected with Spring Security - user must be authenticated
     */
    @RequestMapping(value = "/api/permissions/resource/{resource}", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserPermissionsForResource(
            @PathVariable String resource,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<String> permissions = privilegeService.getUserPrivilegesForResource(
                    userDetails.getUser_id().intValue(), resource);

            response.put("data", permissions);
            response.put("resource", resource);
            response.put("message", "Permissions retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve permissions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/get-roles")
    public ResponseEntity<Map<String, Object>> getAllRoles() {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Role> roles = userService.getAllRoles();

            response.put("status", "SUCCESS");
            response.put("message", "Roles fetched successfully");
            response.put("data", roles);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Failed to fetch roles");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

}
