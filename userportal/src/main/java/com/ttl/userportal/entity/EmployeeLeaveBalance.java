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
@Table(name = "employee_leave_balance", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type_id"}))
public class EmployeeLeaveBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "leave_type_id", nullable = false)
    private Integer leaveTypeId;
    
    @Column(name = "allocated_days", nullable = false)
    private Integer allocatedDays;
    
    @Column(name = "used_days", nullable = false)
    private Integer usedDays = 0;
    
    @Column(name = "balance_days", nullable = false)
    private Integer balanceDays;
    
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balanceDays == null && allocatedDays != null) {
            // Initialize balance with allocated days (full balance available)
            balanceDays = allocatedDays;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Don't auto-calculate balance here - it's managed by the service layer
        // to account for pending leaves
    }
}

