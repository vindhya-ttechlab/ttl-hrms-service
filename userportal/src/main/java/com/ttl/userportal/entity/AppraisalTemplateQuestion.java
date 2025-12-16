package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_template_question")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppraisalTemplateQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "question_id")
    private Long questionId;

    @Column(name = "record_status")
    private Boolean recordStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
