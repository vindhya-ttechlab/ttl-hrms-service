package com.ttl.userportal.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProjectDTO
{
    private Long id;
    private String name;
    private String status;
    private Integer progress;
    private String startDate;
    private String endDate;
    private Integer teamSize;
}
