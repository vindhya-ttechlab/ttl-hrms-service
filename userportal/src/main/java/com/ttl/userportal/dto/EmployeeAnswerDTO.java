package com.ttl.userportal.dto;

import lombok.Data;

@Data
public class EmployeeAnswerDTO {
    private Long answerId;
    private Long questionId;
    private String questionText;
    private String answerText;
}