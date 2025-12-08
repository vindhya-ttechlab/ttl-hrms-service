package com.ttl.userportal.service;

import com.ttl.userportal.dto.LeaveApprovalDTO;
import com.ttl.userportal.entity.LeaveApproval;
import com.ttl.userportal.entity.LeaveEntity;
import com.ttl.userportal.entity.LeaveWorkflow;
import com.ttl.userportal.entity.LeaveWorkflowStep;
import com.ttl.userportal.entity.Users;
import com.ttl.userportal.repository.LeaveApprovalRepository;
import com.ttl.userportal.repository.LeaveRepository;
import com.ttl.userportal.repository.LeaveWorkflowRepository;
import com.ttl.userportal.repository.LeaveWorkflowStepRepository;
import com.ttl.userportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveApprovalService {
    
    @Autowired
    private LeaveApprovalRepository approvalRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;
    
    @Autowired
    private LeaveWorkflowRepository workflowRepository;
    
    @Autowired
    private LeaveWorkflowStepRepository stepRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public LeaveApprovalDTO approveOrRejectLeave(Long leaveId, Integer approverId, 
                                                 String status, String comment, Integer stepId) {
        LeaveEntity leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found with ID: " + leaveId));
        
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("Invalid approval status. Must be APPROVED or REJECTED");
        }
        
        // Find or create approval record
        LeaveApproval approval = approvalRepository
                .findByLeaveIdAndStepIdAndApprovalStatus(leaveId, stepId, "PENDING")
                .orElse(new LeaveApproval());
        
        approval.setLeaveId(leaveId);
        approval.setStepId(stepId);
        approval.setApproverId(approverId);
        approval.setApprovalStatus(status);
        approval.setComment(comment);
        approval.setApprovedDate(LocalDateTime.now());
        approval.setUpdatedAt(LocalDateTime.now());
        
        if (approval.getApprovalId() == null) {
            approval.setCreatedAt(LocalDateTime.now());
            // Get workflow from leave if available
            if (leave.getApprover() != null) {
                // Try to find workflow
                approval.setWorkflowId(null); // Can be set based on business logic
            }
        }
        
        LeaveApproval saved = approvalRepository.save(approval);
        
        // Update leave status
        if ("REJECTED".equals(status)) {
            leave.setLeaveStatus("REJECTED");
            leave.setManagerComment(comment);
        } else {
            // Check if there are more steps
            List<LeaveWorkflowStep> remainingSteps = getRemainingSteps(leaveId, stepId);
            if (remainingSteps.isEmpty()) {
                leave.setLeaveStatus("APPROVED");
            } else {
                leave.setLeaveStatus("PENDING");
            }
        }
        leaveRepository.save(leave);
        
        return convertToDTO(saved);
    }
    
    public List<LeaveApprovalDTO> getApprovalsByLeaveId(Long leaveId) {
        return approvalRepository.findByLeaveIdOrderByCreatedAtAsc(leaveId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<LeaveApprovalDTO> getPendingApprovalsForApprover(Integer approverId) {
        return approvalRepository.findByApproverIdAndApprovalStatus(approverId, "PENDING")
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void initializeApprovalWorkflow(Long leaveId, Integer workflowId) {
        LeaveWorkflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + workflowId));
        
        List<LeaveWorkflowStep> steps = stepRepository
                .findByWorkflowWorkflowIdOrderByStepOrder(workflowId);
        
        // Create pending approval records for each step
        for (LeaveWorkflowStep step : steps) {
            Integer approverId = determineApproverId(step, leaveId);
            
            LeaveApproval approval = new LeaveApproval();
            approval.setLeaveId(leaveId);
            approval.setWorkflowId(workflowId);
            approval.setStepId(step.getStepId());
            approval.setApproverId(approverId);
            approval.setApprovalStatus("PENDING");
            approval.setCreatedAt(LocalDateTime.now());
            approval.setUpdatedAt(LocalDateTime.now());
            
            approvalRepository.save(approval);
        }
    }
    
    private Integer determineApproverId(LeaveWorkflowStep step, Long leaveId) {
        LeaveEntity leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        
        Users user = userRepository.findById(leave.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        switch (step.getApproverType()) {
            case "MANAGER":
                return user.getManager();
            case "USER":
                return step.getApproverUserId();
            case "ROLE":
                // Logic to find user with specific role - simplified for now
                return step.getApproverUserId();
            case "HR":
                // Logic to find HR user - simplified for now
                return step.getApproverUserId();
            default:
                return user.getManager();
        }
    }
    
    private List<LeaveWorkflowStep> getRemainingSteps(Long leaveId, Integer currentStepId) {
        LeaveApproval currentApproval = approvalRepository.findByLeaveIdAndStepIdAndApprovalStatus(leaveId, currentStepId, "APPROVED")
                .orElse(null);
        
        if (currentApproval == null || currentApproval.getWorkflowId() == null) {
            return List.of();
        }
        
        List<LeaveWorkflowStep> allSteps = stepRepository
                .findByWorkflowWorkflowIdOrderByStepOrder(currentApproval.getWorkflowId());
        
        LeaveWorkflowStep currentStep = allSteps.stream()
                .filter(s -> s.getStepId().equals(currentStepId))
                .findFirst()
                .orElse(null);
        
        if (currentStep == null) {
            return List.of();
        }
        
        // Return steps after current step
        return allSteps.stream()
                .filter(s -> s.getStepOrder() > currentStep.getStepOrder())
                .collect(Collectors.toList());
    }
    
    private LeaveApprovalDTO convertToDTO(LeaveApproval approval) {
        String approverName = null;
        if (approval.getApproverId() != null) {
            approverName = userRepository.findById(approval.getApproverId())
                    .map(Users::getName)
                    .orElse(null);
        }
        
        return new LeaveApprovalDTO(
                approval.getApprovalId(),
                approval.getLeaveId(),
                approval.getWorkflowId(),
                approval.getStepId(),
                approval.getApproverId(),
                approverName,
                approval.getApprovalStatus(),
                approval.getComment(),
                approval.getApprovedDate(),
                approval.getCreatedAt()
        );
    }
}

