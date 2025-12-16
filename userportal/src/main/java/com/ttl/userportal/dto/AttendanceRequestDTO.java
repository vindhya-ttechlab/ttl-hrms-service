package com.ttl.userportal.dto;

import lombok.Data;

import java.time.LocalDate;

@Data

public class AttendanceRequestDTO {
    private Long id;
    private Long userId;
    private LocalDate date;
    private String type;
    private String remarks;
    private LocalDate createdAt;
    private LocalDate updatedAt;

}
