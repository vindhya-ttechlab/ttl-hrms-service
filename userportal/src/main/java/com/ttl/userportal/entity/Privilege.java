package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Privilege Entity
 * Represents a privilege (VIEW, READ, WRITE, EDIT) for a specific resource (LEAVE_TYPE, LEAVE, etc.)
 * 
 * Example: VIEW privilege on LEAVE_TYPE resource
 * This entity maps to the 'permissions' table in the database
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Privilege")
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer privilegeId; // Maps to permission_id column

    @Column(name = "permission_name", nullable = false, length = 50)
    private String privilegeName; // VIEW, READ, WRITE, EDIT - maps to permission_name column

    @Column(name = "resource", nullable = false, length = 100)
    private String resource; // LEAVE_TYPE, LEAVE, EMPLOYEE, etc.

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}

