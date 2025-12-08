package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.LeaveApprovalDTO;
import com.ttl.userportal.service.LeaveApprovalService;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-approvals")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveApprovalController {
    
    @Autowired
    private LeaveApprovalService approvalService;
    
    @GetMapping("/leave/{leaveId}")
    public ResponseEntity<Map<String, Object>> getApprovalsByLeaveId(@PathVariable Long leaveId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<LeaveApprovalDTO> approvals = approvalService.getApprovalsByLeaveId(leaveId);
            response.put("data", approvals);
            response.put("message", "Approvals retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve approvals");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingApprovals(
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Integer approverId = Math.toIntExact(userDetails.getUser_id());
            List<LeaveApprovalDTO> approvals = approvalService.getPendingApprovalsForApprover(approverId);
            response.put("data", approvals);
            response.put("message", "Pending approvals retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to retrieve pending approvals");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveLeave(
            @RequestBody Map<String, Object> request,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long leaveId = Long.valueOf(request.get("leaveId").toString());
            Integer stepId = Integer.valueOf(request.get("stepId").toString());
            String comment = request.get("comment") != null ? request.get("comment").toString() : null;
            Integer approverId = Math.toIntExact(userDetails.getUser_id());
            
            LeaveApprovalDTO approval = approvalService.approveOrRejectLeave(
                    leaveId, approverId, "APPROVED", comment, stepId);
            response.put("data", approval);
            response.put("message", "Leave approved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to approve leave");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping("/reject")
    public ResponseEntity<Map<String, Object>> rejectLeave(
            @RequestBody Map<String, Object> request,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Long leaveId = Long.valueOf(request.get("leaveId").toString());
            Integer stepId = Integer.valueOf(request.get("stepId").toString());
            String comment = request.get("comment") != null ? request.get("comment").toString() : null;
            Integer approverId = Math.toIntExact(userDetails.getUser_id());
            
            LeaveApprovalDTO approval = approvalService.approveOrRejectLeave(
                    leaveId, approverId, "REJECTED", comment, stepId);
            response.put("data", approval);
            response.put("message", "Leave rejected successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to reject leave");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping("/initialize/{leaveId}")
    public ResponseEntity<Map<String, Object>> initializeWorkflow(
            @PathVariable Long leaveId,
            @RequestParam Integer workflowId) {
        Map<String, Object> response = new HashMap<>();
        try {
            approvalService.initializeApprovalWorkflow(leaveId, workflowId);
            response.put("message", "Workflow initialized successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("message", "Failed to initialize workflow");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

