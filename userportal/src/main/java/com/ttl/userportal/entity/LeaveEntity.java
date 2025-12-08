package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Leave_Table")
public class LeaveEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "Reason")
    private String reason;

    @Column(name = "type")
    private String type;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name="approver")
    private Integer approver;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "applied_date")
    private LocalDateTime appliedDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "leave_status")
    private String leaveStatus;

    @Column(name = "number_of_days")
    private Integer numberOfDays;

    @Column(name = "manager_comment")
    private String managerComment;
}
