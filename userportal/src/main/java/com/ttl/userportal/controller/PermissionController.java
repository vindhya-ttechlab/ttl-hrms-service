package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.service.PermissionService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "http://localhost:3000")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;
    @GetMapping("/resource/{resource}")
    public ResponseEntity<Map<String, Object>> getUserPermissionsForResource(
            @PathVariable String resource,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<String> permissions = permissionService.getUserPermissionsForResource(
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
    @GetMapping("/check")
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

            boolean hasPermission = permissionService.hasPermission(
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
}

