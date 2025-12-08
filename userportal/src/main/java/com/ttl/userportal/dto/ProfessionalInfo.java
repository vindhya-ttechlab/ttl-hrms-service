package com.ttl.userportal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Data
@Getter
@Setter
public class ProfessionalInfo
{
    private String position;
    private String department;
    private String joinDate;   // Could be LocalDate
    private String experience;
    private ManagerDTO manager;
    private String team;
    private List<String> skills;
    private String education;
    private List<String> languages;
    private List<ProjectDTO> currentProjects;
    private List<String> achievements;

}
