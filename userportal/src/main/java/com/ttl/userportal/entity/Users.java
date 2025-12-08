
package com.ttl.userportal.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "Users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "emp_code", nullable = false, unique = true)
    private String empCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String location;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(length = 255)
    private String address;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('Active','Inactive') DEFAULT 'Active'")
    private Status status = Status.Active;

    @Column(length = 100)
    private String position;

    @Column(length = 100)
    private String department;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(length = 50)
    private String experience;

    @Column(length = 150)
    private String education;

    @Column(length = 100)
    private String team;

    @Column(name = "manager_id")
    private Integer manager;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Enum for status
    public enum Status {
        Active,
        Inactive
    }

    @Column(name="Password")
    private String password;

    @Column(name = "skills")
    private String skills;

    @Column(name = "languages")
    private String languages;

    @Column(name="achievement")
    private String achievement;

    @Column(name = "is_first_login", nullable = false)
    private Boolean isFirstLogin = true; // Default to true for new users

}
