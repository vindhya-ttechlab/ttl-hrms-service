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
@Table(name = "leave_types")
public class LeaveType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_type_id")
    private Integer leaveTypeId;
    
    @Column(name = "type_name", nullable = false, unique = true, length = 100)
    private String typeName;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "number_of_days", nullable = false)
    private Integer numberOfDays;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_carry_forward_allowed")
    private Boolean isCarryForwardAllowed = false;
    
    @Column(name = "max_carry_forward_days")
    private Integer maxCarryForwardDays;
    
    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = true;
    
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}

