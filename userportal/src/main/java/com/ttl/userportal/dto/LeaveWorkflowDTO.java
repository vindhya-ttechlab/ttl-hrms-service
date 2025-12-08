package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveWorkflowDTO {
    private Integer workflowId;
    private String workflowName;
    private String description;
    private Boolean isActive;
    private Boolean isDefault;
    private Integer createdBy;
    private List<LeaveWorkflowStepDTO> steps;
}

