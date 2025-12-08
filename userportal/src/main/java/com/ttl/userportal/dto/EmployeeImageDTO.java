package com.ttl.userportal.dto;

import java.time.LocalDateTime;

public class EmployeeImageDTO {
    
    private Long imageId;
    private Long employeeId;
    private String imageName;
    private String imageUrl;
    private String imageType;
    private Long imageSize;
    private Boolean isPrimary;
    private LocalDateTime uploadedAt;
    
    // Constructors
    public EmployeeImageDTO() {}
    
    public EmployeeImageDTO(Long imageId, Long employeeId, String imageName, 
                           String imageUrl, String imageType, Long imageSize, 
                           Boolean isPrimary, LocalDateTime uploadedAt) {
        this.imageId = imageId;
        this.employeeId = employeeId;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.imageSize = imageSize;
        this.isPrimary = isPrimary;
        this.uploadedAt = uploadedAt;
    }
    
    // Getters and Setters
    public Long getImageId() {
        return imageId;
    }
    
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    
    public Long getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImageType() {
        return imageType;
    }
    
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }
    
    public Long getImageSize() {
        return imageSize;
    }
    
    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }
    
    public Boolean getIsPrimary() {
        return isPrimary;
    }
    
    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }
    
    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
