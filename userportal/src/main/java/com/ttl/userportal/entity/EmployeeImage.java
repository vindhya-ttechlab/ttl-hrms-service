package com.ttl.userportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employee_images")
public class EmployeeImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "image_name", nullable = false)
    private String imageName;
    
    @Column(name = "image_path", nullable = false)
    private String imagePath;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @Column(name = "image_type", nullable = false)
    private String imageType;
    
    @Column(name = "image_size", nullable = false)
    private Long imageSize;
    
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = true;
    
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
