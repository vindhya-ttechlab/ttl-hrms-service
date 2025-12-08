package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@Getter
@Setter
@Entity
@Table(name = "projects")
public class Projects
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String status;

    private int progress;

    private LocalDate startDate;

    private LocalDate endDate;

    private int teamSize;

    private int employeeId;

}
