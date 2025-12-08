package com.ttl.userportal.service;

import com.ttl.userportal.dto.LeaveWorkflowDTO;
import com.ttl.userportal.dto.LeaveWorkflowStepDTO;
import com.ttl.userportal.entity.LeaveWorkflow;
import com.ttl.userportal.entity.LeaveWorkflowStep;
import com.ttl.userportal.repository.LeaveWorkflowRepository;
import com.ttl.userportal.repository.LeaveWorkflowStepRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveWorkflowService {
    
    @Autowired
    private LeaveWorkflowRepository workflowRepository;
    
    @Autowired
    private LeaveWorkflowStepRepository stepRepository;
    
    public List<LeaveWorkflowDTO> getAllWorkflows() {
        return workflowRepository.findByIsActiveTrueOrderByWorkflowName().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public LeaveWorkflowDTO getWorkflowById(Integer id) {
        LeaveWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));
        return convertToDTO(workflow);
    }
    
    public LeaveWorkflowDTO getDefaultWorkflow() {
        LeaveWorkflow workflow = workflowRepository.findByIsDefaultTrueAndIsActiveTrue()
                .orElseThrow(() -> new RuntimeException("No default workflow found"));
        return convertToDTO(workflow);
    }
    
    @Transactional
    public LeaveWorkflowDTO createWorkflow(LeaveWorkflowDTO dto, Integer createdBy) {
        if (workflowRepository.existsByWorkflowName(dto.getWorkflowName())) {
            throw new RuntimeException("Workflow with name '" + dto.getWorkflowName() + "' already exists");
        }
        
        // If this is set as default, unset other defaults
        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            workflowRepository.findByIsDefaultTrueAndIsActiveTrue()
                    .ifPresent(w -> {
                        w.setIsDefault(false);
                        workflowRepository.save(w);
                    });
        }
        
        LeaveWorkflow workflow = new LeaveWorkflow();
        workflow.setWorkflowName(dto.getWorkflowName());
        workflow.setDescription(dto.getDescription());
        workflow.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        workflow.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        workflow.setCreatedBy(createdBy);
        workflow.setCreatedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        LeaveWorkflow saved = workflowRepository.save(workflow);
        
        // Save steps
        if (dto.getSteps() != null && !dto.getSteps().isEmpty()) {
            for (LeaveWorkflowStepDTO stepDTO : dto.getSteps()) {
                LeaveWorkflowStep step = new LeaveWorkflowStep();
                step.setWorkflow(saved);
                step.setStepOrder(stepDTO.getStepOrder());
                step.setApproverRoleId(stepDTO.getApproverRoleId());
                step.setApproverUserId(stepDTO.getApproverUserId());
                step.setApproverType(stepDTO.getApproverType());
                step.setIsRequired(stepDTO.getIsRequired() != null ? stepDTO.getIsRequired() : true);
                step.setCanReject(stepDTO.getCanReject() != null ? stepDTO.getCanReject() : true);
                step.setCreatedAt(LocalDateTime.now());
                step.setUpdatedAt(LocalDateTime.now());
                stepRepository.save(step);
            }
        }
        
        return convertToDTO(saved);
    }
    
    @Transactional
    public LeaveWorkflowDTO updateWorkflow(Integer id, LeaveWorkflowDTO dto) {
        LeaveWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));
        
        // Check if workflow name is being changed
        if (!workflow.getWorkflowName().equals(dto.getWorkflowName()) && 
            workflowRepository.existsByWorkflowName(dto.getWorkflowName())) {
            throw new RuntimeException("Workflow with name '" + dto.getWorkflowName() + "' already exists");
        }
        
        // If this is set as default, unset other defaults
        if (dto.getIsDefault() != null && dto.getIsDefault() && !workflow.getIsDefault()) {
            workflowRepository.findByIsDefaultTrueAndIsActiveTrue()
                    .ifPresent(w -> {
                        if (!w.getWorkflowId().equals(id)) {
                            w.setIsDefault(false);
                            workflowRepository.save(w);
                        }
                    });
        }
        
        workflow.setWorkflowName(dto.getWorkflowName());
        workflow.setDescription(dto.getDescription());
        workflow.setIsActive(dto.getIsActive());
        workflow.setIsDefault(dto.getIsDefault());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        // Delete existing steps and create new ones
        stepRepository.deleteByWorkflowWorkflowId(id);
        
        if (dto.getSteps() != null && !dto.getSteps().isEmpty()) {
            for (LeaveWorkflowStepDTO stepDTO : dto.getSteps()) {
                LeaveWorkflowStep step = new LeaveWorkflowStep();
                step.setWorkflow(workflow);
                step.setStepOrder(stepDTO.getStepOrder());
                step.setApproverRoleId(stepDTO.getApproverRoleId());
                step.setApproverUserId(stepDTO.getApproverUserId());
                step.setApproverType(stepDTO.getApproverType());
                step.setIsRequired(stepDTO.getIsRequired() != null ? stepDTO.getIsRequired() : true);
                step.setCanReject(stepDTO.getCanReject() != null ? stepDTO.getCanReject() : true);
                step.setCreatedAt(LocalDateTime.now());
                step.setUpdatedAt(LocalDateTime.now());
                stepRepository.save(step);
            }
        }
        
        LeaveWorkflow updated = workflowRepository.save(workflow);
        return convertToDTO(updated);
    }
    
    public void deleteWorkflow(Integer id) {
        LeaveWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found with ID: " + id));
        
        // Soft delete
        workflow.setIsActive(false);
        workflow.setUpdatedAt(LocalDateTime.now());
        workflowRepository.save(workflow);
    }
    
    private LeaveWorkflowDTO convertToDTO(LeaveWorkflow workflow) {
        List<LeaveWorkflowStepDTO> steps = stepRepository
                .findByWorkflowWorkflowIdOrderByStepOrder(workflow.getWorkflowId())
                .stream()
                .map(this::convertStepToDTO)
                .collect(Collectors.toList());
        
        return new LeaveWorkflowDTO(
                workflow.getWorkflowId(),
                workflow.getWorkflowName(),
                workflow.getDescription(),
                workflow.getIsActive(),
                workflow.getIsDefault(),
                workflow.getCreatedBy(),
                steps
        );
    }
    
    private LeaveWorkflowStepDTO convertStepToDTO(LeaveWorkflowStep step) {
        return new LeaveWorkflowStepDTO(
                step.getStepId(),
                step.getWorkflow().getWorkflowId(),
                step.getStepOrder(),
                step.getApproverRoleId(),
                step.getApproverUserId(),
                step.getApproverType(),
                step.getIsRequired(),
                step.getCanReject()
        );
    }
}

