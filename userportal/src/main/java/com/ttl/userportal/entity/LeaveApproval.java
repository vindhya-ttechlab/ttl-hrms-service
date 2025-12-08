package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_approvals")
public class LeaveApproval {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_id")
    private Long approvalId;
    
    @Column(name = "leave_id", nullable = false)
    private Long leaveId;
    
    @Column(name = "workflow_id")
    private Integer workflowId;
    
    @Column(name = "step_id")
    private Integer stepId;
    
    @Column(name = "approver_id", nullable = false)
    private Integer approverId;
    
    @Column(name = "approval_status", nullable = false, length = 50)
    private String approvalStatus; // "PENDING", "APPROVED", "REJECTED"
    
    @Column(name = "comment", length = 1000)
    private String comment;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}

