package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.MenuItemDTO;
import com.ttl.userportal.service.MenuService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/menu-items")
@CrossOrigin(origins = "http://localhost:3000")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getMenuItemsForCurrentUser(
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Integer roleId = userDetails.getPrimaryRoleId() != null ? userDetails.getPrimaryRoleId() : 1;
            
            List<MenuItemDTO> menuItems = menuService.getMenuItemsDTOByRoleId(roleId);
            response.put("data", menuItems);
            response.put("message", "Menu items retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve menu items for current user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<Map<String, Object>> getMenuItemsByRoleId(
            @PathVariable Integer roleId, 
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MenuItemDTO> menuItems = menuService.getMenuItemsDTOByRoleId(roleId);
            response.put("data", menuItems);
            response.put("message", "Menu items retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve menu items for role ID: " + roleId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMenuItems() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MenuItemDTO> menuItems = menuService.getAllMenuItemsDTO();
            response.put("data", menuItems);
            response.put("message", "All menu items retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve menu items");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getMenuItemsByUserId(@PathVariable Integer userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MenuItemDTO> menuItems = menuService.getMenuItemsDTOByUserId(userId);
            response.put("data", menuItems);
            response.put("message", "Menu items retrieved successfully for user: " + userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve menu items for user: " + userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

