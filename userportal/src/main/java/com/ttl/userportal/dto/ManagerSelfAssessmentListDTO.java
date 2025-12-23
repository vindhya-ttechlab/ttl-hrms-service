package com.ttl.userportal.dto;

import lombok.Data;

@Data
public class ManagerSelfAssessmentListDTO {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String employeeEmailId;
    private Long templateId;
    private String templateName;

    private Long reviewId;
}
