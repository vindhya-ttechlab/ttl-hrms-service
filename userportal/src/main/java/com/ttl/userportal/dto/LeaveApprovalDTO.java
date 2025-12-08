package com.ttl.userportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApprovalDTO {
    private Long approvalId;
    private Long leaveId;
    private Integer workflowId;
    private Integer stepId;
    private Integer approverId;
    private String approverName;
    private String approvalStatus; // "PENDING", "APPROVED", "REJECTED"
    private String comment;
    private LocalDateTime approvedDate;
    private LocalDateTime createdAt;
}

