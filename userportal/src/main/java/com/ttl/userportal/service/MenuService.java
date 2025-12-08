package com.ttl.userportal.service;

import com.ttl.userportal.dto.MenuItemDTO;
import com.ttl.userportal.entity.MenuItem;
import com.ttl.userportal.entity.Role;
import com.ttl.userportal.repository.MenuItemRepository;
import com.ttl.userportal.repository.MenuRoleMapRepository;
import com.ttl.userportal.repository.RoleRepository;
import com.ttl.userportal.repository.UserRoleMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private MenuRoleMapRepository menuRoleMapRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleMapRepository userRoleMapRepository;
    public List<MenuItem> getMenuItemsByRoleId(Integer roleId) {
        // Verify role exists
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role not found with ID: " + roleId);
        }

        List<Integer> menuIds = menuRoleMapRepository.findMenuIdsByRoleId(roleId);

        List<MenuItem> allMenuItems = menuItemRepository.findAllById(menuIds);
        
        List<Integer> parentIds = allMenuItems.stream()
                .filter(item -> item.getParentId() != null)
                .map(MenuItem::getParentId)
                .distinct()
                .collect(Collectors.toList());
        
        if (!parentIds.isEmpty()) {
            List<MenuItem> parentItems = menuItemRepository.findAllById(parentIds);
            for (MenuItem parent : parentItems) {
                if (allMenuItems.stream().noneMatch(item -> item.getMenuId().equals(parent.getMenuId()))) {
                    allMenuItems.add(parent);
                }
            }
        }

        return allMenuItems.stream()
                .filter(item -> item.getIsActive() != null && item.getIsActive())
                .collect(Collectors.toList());
    }

    public List<MenuItem> getMenuItemsByRole(String roleName) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        return getMenuItemsByRoleId(role.getRoleId());
    }

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    public List<MenuItem> getMenuItemsByUserId(Integer userId) {
        List<Integer> roleIds = userRoleMapRepository.findActiveRoleIdsByUserId(userId);
        
        if (roleIds.isEmpty()) {
            return List.of();
        }
        
        List<Integer> menuIds = roleIds.stream()
                .flatMap(roleId -> menuRoleMapRepository.findMenuIdsByRoleId(roleId).stream())
                .distinct()
                .collect(Collectors.toList());
        
        if (menuIds.isEmpty()) {
            return List.of();
        }
        
        List<MenuItem> allMenuItems = menuItemRepository.findAllById(menuIds);
        
        List<Integer> parentIds = allMenuItems.stream()
                .filter(item -> item.getParentId() != null)
                .map(MenuItem::getParentId)
                .distinct()
                .collect(Collectors.toList());
        
        if (!parentIds.isEmpty()) {
            List<MenuItem> parentItems = menuItemRepository.findAllById(parentIds);
            for (MenuItem parent : parentItems) {
                if (allMenuItems.stream().noneMatch(item -> item.getMenuId().equals(parent.getMenuId()))) {
                    allMenuItems.add(parent);
                }
            }
        }

        return allMenuItems.stream()
                .filter(item -> item.getIsActive() != null && item.getIsActive())
                .collect(Collectors.toList());
    }

    // Convert MenuItem entities to MenuItemDTO with hierarchical structure
    public List<MenuItemDTO> convertToDTO(List<MenuItem> menuItems) {
        if (menuItems == null || menuItems.isEmpty()) {
            return new ArrayList<>();
        }

        // Filter active items and sort by display order
        List<MenuItem> activeItems = menuItems.stream()
                .filter(item -> item.getIsActive() != null && item.getIsActive())
                .sorted(Comparator.comparing(MenuItem::getDisplayOrder, 
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // Create a map of menuId to MenuItemDTO for quick lookup
        Map<Integer, MenuItemDTO> dtoMap = activeItems.stream()
                .collect(Collectors.toMap(
                        MenuItem::getMenuId,
                        item -> new MenuItemDTO(
                                item.getLabel(),
                                item.getIcon(),
                                item.getPath(),
                                new ArrayList<>()
                        )
                ));

        // Build hierarchical structure - process in sorted order to maintain display order
        List<MenuItemDTO> rootItems = new ArrayList<>();
        for (MenuItem item : activeItems) {
            MenuItemDTO dto = dtoMap.get(item.getMenuId());
            if (item.getParentId() == null) {
                // Root level item
                rootItems.add(dto);
            } else {
                // Child item - add to parent's children list
                MenuItemDTO parent = dtoMap.get(item.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        // Remove children field if empty (to match the desired JSON structure)
        rootItems.forEach(item -> {
            if (item.getChildren() != null && item.getChildren().isEmpty()) {
                item.setChildren(null);
            }
        });

        return rootItems;
    }

    public List<MenuItemDTO> getMenuItemsDTOByRoleId(Integer roleId) {
        List<MenuItem> menuItems = getMenuItemsByRoleId(roleId);
        return convertToDTO(menuItems);
    }

    public List<MenuItemDTO> getAllMenuItemsDTO() {
        List<MenuItem> menuItems = getAllMenuItems();
        return convertToDTO(menuItems);
    }

    public List<MenuItemDTO> getMenuItemsDTOByUserId(Integer userId) {
        List<MenuItem> menuItems = getMenuItemsByUserId(userId);
        return convertToDTO(menuItems);
    }
}

