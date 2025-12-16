package com.ttl.userportal.repository;

import com.ttl.userportal.entity.AppraisalQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalQuestionRepository extends JpaRepository<AppraisalQuestions, Long> {
    List<AppraisalQuestions> findAllByRecordStatus(boolean b);

    Optional<AppraisalQuestions> findByIdAndRecordStatus(Long appraisalQuestionId, boolean b);

    Boolean existsByQuestionTextIgnoreCaseAndRecordStatusTrue(String questionText);

    @Query("""
        SELECT COUNT(q) > 0
        FROM AppraisalQuestions q
        WHERE LOWER(TRIM(q.questionText)) = LOWER(TRIM(:questionText))
          AND q.recordStatus = true
          AND q.id <> :id
        """
    )
    boolean existsDuplicateForUpdate(@Param("questionText") String questionText, @Param("id") Long id);

}
