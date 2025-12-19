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
    
    @Column(name = "type_code", nullable = false, unique = true, length = 20)
    private String typeCode; // EL, SL, ML, MNL, CL, LWP, PH
    
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
    
    // Advance notice required in days (e.g., 7 days for EL)
    @Column(name = "advance_notice_days")
    private Integer advanceNoticeDays;
    
    // For sick leave - medical certificate required after X consecutive days
    @Column(name = "medical_cert_required_after_days")
    private Integer medicalCertRequiredAfterDays;
    
    // Gender specific leave (FEMALE, MALE, ALL)
    @Column(name = "applicable_gender", length = 10)
    private String applicableGender = "ALL";
    
    // For menstrual leave - age restrictions
    @Column(name = "min_age")
    private Integer minAge;
    
    @Column(name = "max_age")
    private Integer maxAge;
    
    // For compensatory leave - days to use the leave
    @Column(name = "expiry_days")
    private Integer expiryDays;
    
    // Is this a paid leave?
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = true;
    
    // Can be applied for half day?
    @Column(name = "allow_half_day")
    private Boolean allowHalfDay = true;
    
    // Maximum leaves per month (for menstrual leave = 1)
    @Column(name = "max_per_month")
    private Integer maxPerMonth;
    
    // Minimum hours worked to earn compensatory leave
    @Column(name = "min_hours_for_comp")
    private Integer minHoursForComp;
    
    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}

