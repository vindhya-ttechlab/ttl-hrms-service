package com.ttl.userportal.entity;

import com.ttl.userportal.dto.AppraisalQuestionDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "appraisal_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalQuestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "question_text", unique = true)
    private String questionText;

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

    public boolean hasChanged(AppraisalQuestionDTO dto){
        return !Objects.equals(this.questionText, dto.getQuestionText());
    }
}
