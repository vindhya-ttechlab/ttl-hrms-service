package com.ttl.userportal.repository;

import com.ttl.userportal.entity.ManagerAppraisalReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerAppraisalReviewRepository extends JpaRepository<ManagerAppraisalReview, Long> {
    ManagerAppraisalReview findByTemplateIdAndRecordStatus(Long templateId, Boolean recordStatus);
    ManagerAppraisalReview findByTemplateIdAndEmployeeIdAndRecordStatus(Long templateId, Long EmployeeId, Boolean recordStatus);

    Optional<ManagerAppraisalReview> findByIdAndManagerIdAndRecordStatus(
            Long id,
            Long managerId,
            Boolean recordStatus
    );
    ManagerAppraisalReview findByTemplateIdAndEmployeeIdAndManagerIdAndRecordStatus(
            Long templateId,
            Long employeeId,
            Long managerId,
            Boolean recordStatus
    );
}
