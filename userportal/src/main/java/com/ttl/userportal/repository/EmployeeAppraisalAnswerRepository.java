package com.ttl.userportal.repository;

import com.ttl.userportal.entity.AppraisalTemplateQuestion;
import com.ttl.userportal.entity.EmployeeAppraisalAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAppraisalAnswerRepository extends JpaRepository<EmployeeAppraisalAnswer, Long> {
    Optional<EmployeeAppraisalAnswer> findByTemplateIdAndQuestionIdAndUserId(
            Long templateId,
            Long questionId,
            Long userId
    );

    List<EmployeeAppraisalAnswer> findAllByTemplateIdAndUserIdAndRecordStatus(
            Long templateId,
            Long userId,
            Boolean recordStatus
    );

    List<EmployeeAppraisalAnswer> findAllByQuestionIdAndRecordStatus(
            Long questionId,
            Boolean recordStatus
    );
}
