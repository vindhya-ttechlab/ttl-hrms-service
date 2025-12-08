package com.ttl.userportal.controller;

import com.ttl.userportal.dto.LeaveTypeDTO;
import com.ttl.userportal.service.LeaveTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-types")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveTypeController {
    
    @Autowired
    private LeaveTypeService leaveTypeService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllLeaveTypes(
            @RequestParam(required = false) Boolean includeInactive) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<LeaveTypeDTO> leaveTypes;
            if (includeInactive != null && includeInactive) {
                leaveTypes = leaveTypeService.getAllLeaveTypesIncludingInactive();
            } else {
                leaveTypes = leaveTypeService.getAllLeaveTypes();
            }
            response.put("data", leaveTypes);
            response.put("message", "Leave types retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve leave types");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLeaveTypeById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            LeaveTypeDTO leaveType = leaveTypeService.getLeaveTypeById(id);
            response.put("data", leaveType);
            response.put("message", "Leave type retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve leave type");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createLeaveType(@RequestBody LeaveTypeDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            LeaveTypeDTO created = leaveTypeService.createLeaveType(dto);
            response.put("data", created);
            response.put("message", "Leave type created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to create leave type");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateLeaveType(
            @PathVariable Integer id, 
            @RequestBody LeaveTypeDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            LeaveTypeDTO updated = leaveTypeService.updateLeaveType(id, dto);
            response.put("data", updated);
            response.put("message", "Leave type updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to update leave type");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLeaveType(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            leaveTypeService.deleteLeaveType(id);
            response.put("message", "Leave type deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to delete leave type");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

