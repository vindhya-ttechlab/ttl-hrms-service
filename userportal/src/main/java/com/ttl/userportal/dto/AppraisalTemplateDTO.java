package com.ttl.userportal.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppraisalTemplateDTO {
    private Long templateId;
    private String templateName;
    private List<AppraisalQuestionDTO> appraisalQuestions;
}
