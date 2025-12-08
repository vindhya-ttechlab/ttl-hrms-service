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
@Table(name = "leave_workflow_steps")
public class LeaveWorkflowStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Integer stepId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    private LeaveWorkflow workflow;
    
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
    
    @Column(name = "approver_role_id")
    private Integer approverRoleId;
    
    @Column(name = "approver_user_id")
    private Integer approverUserId;
    
    @Column(name = "approver_type", nullable = false, length = 50)
    private String approverType; // "ROLE", "USER", "MANAGER", "HR"
    
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;
    
    @Column(name = "can_reject", nullable = false)
    private Boolean canReject = true;
    
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}

