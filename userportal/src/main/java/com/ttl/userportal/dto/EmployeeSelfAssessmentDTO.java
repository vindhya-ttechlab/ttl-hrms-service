package com.ttl.userportal.dto;

import lombok.Data;

import java.util.List;

@Data
public class EmployeeSelfAssessmentDTO {
    private Long templateId;
    private String templateName;
    private List<EmployeeAnswerDTO> questionAnswers;
}
