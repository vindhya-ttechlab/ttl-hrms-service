package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.LeaveWorkflowDTO;
import com.ttl.userportal.service.LeaveWorkflowService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-workflows")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveWorkflowController {
    
    @Autowired
    private LeaveWorkflowService workflowService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllWorkflows() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<LeaveWorkflowDTO> workflows = workflowService.getAllWorkflows();
            response.put("data", workflows);
            response.put("message", "Workflows retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve workflows");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/default")
    public ResponseEntity<Map<String, Object>> getDefaultWorkflow() {
        Map<String, Object> response = new HashMap<>();
        try {
            LeaveWorkflowDTO workflow = workflowService.getDefaultWorkflow();
            response.put("data", workflow);
            response.put("message", "Default workflow retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve default workflow");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWorkflowById(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            LeaveWorkflowDTO workflow = workflowService.getWorkflowById(id);
            response.put("data", workflow);
            response.put("message", "Workflow retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve workflow");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createWorkflow(
            @RequestBody LeaveWorkflowDTO dto,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer createdBy = userDetails != null ? Math.toIntExact(userDetails.getUser_id()) : null;
            LeaveWorkflowDTO created = workflowService.createWorkflow(dto, createdBy);
            response.put("data", created);
            response.put("message", "Workflow created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to create workflow");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateWorkflow(
            @PathVariable Integer id,
            @RequestBody LeaveWorkflowDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            LeaveWorkflowDTO updated = workflowService.updateWorkflow(id, dto);
            response.put("data", updated);
            response.put("message", "Workflow updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to update workflow");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteWorkflow(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            workflowService.deleteWorkflow(id);
            response.put("message", "Workflow deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to delete workflow");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

