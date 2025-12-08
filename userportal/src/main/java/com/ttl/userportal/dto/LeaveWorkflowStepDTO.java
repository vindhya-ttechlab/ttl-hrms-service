package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveWorkflowStepDTO {
    private Integer stepId;
    private Integer workflowId;
    private Integer stepOrder;
    private Integer approverRoleId;
    private Integer approverUserId;
    private String approverType; // "ROLE", "USER", "MANAGER", "HR"
    private Boolean isRequired;
    private Boolean canReject;
}

