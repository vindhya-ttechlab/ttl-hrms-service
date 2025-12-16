package com.ttl.userportal.repository;

import com.ttl.userportal.entity.AppraisalTemplateQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalTemplateQuestionRepository extends JpaRepository<AppraisalTemplateQuestion, Long>{
    List<AppraisalTemplateQuestion> findAllByTemplateIdAndRecordStatus(Long templateId, Boolean status);
    Optional<AppraisalTemplateQuestion> findByTemplateIdAndQuestionIdAndRecordStatus(Long templateId,
                                                                                     Long questionId, Boolean status);
    Optional<AppraisalTemplateQuestion> findByTemplateIdAndQuestionId(Long templateId, Long questionId);

    List<AppraisalTemplateQuestion> findAllByQuestionIdAndRecordStatus(
            Long questionId,
            Boolean recordStatus
    );
}
