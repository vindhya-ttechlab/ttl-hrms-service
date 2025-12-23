package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "manager_appraisal_review",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"manager_id", "employee_id", "template_id"}
                )
        }
        )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerAppraisalReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "employee_id")
    private Long employeeId; // not employee table yet for now users table

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "review_text")
    private String reviewText;

    @Column(name = "record_status")
    private Boolean recordStatus;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
