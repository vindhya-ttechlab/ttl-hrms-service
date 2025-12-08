package com.ttl.userportal.service;

import com.ttl.userportal.dto.EmployeeImageDTO;
import com.ttl.userportal.entity.EmployeeImage;
import com.ttl.userportal.repository.EmployeeImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeImageService {
    
    @Autowired
    private EmployeeImageRepository employeeImageRepository;
    
    @Value("${app.upload.dir:uploads/employees}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Upload and save employee image
     */
//    public EmployeeImageDTO uploadEmployeeImage(Long employeeId, MultipartFile file) throws IOException {
//        // Validate file
//        if (file.isEmpty()) {
//            throw new IllegalArgumentException("File is empty");
//        }
//
//        // Validate file type
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new IllegalArgumentException("File must be an image");
//        }
//
//        // Create upload directory if it doesn't exist
//        Path uploadPath = Paths.get(uploadDir, employeeId.toString());
//        Files.createDirectories(uploadPath);
//
//        // Generate unique filename
//        String originalFilename = file.getOriginalFilename();
//        String fileExtension = originalFilename != null ?
//            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
//        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
//
//        // Save file
//        Path filePath = uploadPath.resolve(uniqueFilename);
//        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//        // Set previous primary images to non-primary
//        List<EmployeeImage> existingImages = employeeImageRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
//        for (EmployeeImage existingImage : existingImages) {
//            if (existingImage.getIsPrimary()) {
//                existingImage.setIsPrimary(false);
//                employeeImageRepository.save(existingImage);
//            }
//        }
//
//        // Create image record
//        String imageUrl = baseUrl + "/api/images/employee/" + employeeId + "/" + uniqueFilename;
////        EmployeeImage employeeImage = new EmployeeImage(
////            employeeId,
////            originalFilename,
////            filePath.toString(),
////            imageUrl,
////            contentType,
////            file.getSize()
////        );
//
////        EmployeeImage savedImage = employeeImageRepository.save(employeeImage);
////        return convertToDTO(savedImage);
//    }
//
    /**
     * Get primary image for employee
     */
    public Optional<EmployeeImageDTO> getPrimaryImageByEmployeeId(Long employeeId) {
        Optional<EmployeeImage> image = employeeImageRepository.findPrimaryImageByEmployeeId(employeeId);
        return image.map(this::convertToDTO);
    }
    
    /**
     * Get all images for employee
     */
    public List<EmployeeImageDTO> getAllImagesByEmployeeId(Long employeeId) {
        List<EmployeeImage> images = employeeImageRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
        return images.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Get primary images for multiple employees
     */
    public List<EmployeeImageDTO> getPrimaryImagesByEmployeeIds(List<Long> employeeIds) {
        List<EmployeeImage> images = employeeImageRepository.findPrimaryImagesByEmployeeIds(employeeIds);
        return images.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * Delete employee image
     */
    public void deleteEmployeeImage(Long imageId) throws IOException {
        Optional<EmployeeImage> imageOpt = employeeImageRepository.findById(imageId);
        if (imageOpt.isPresent()) {
            EmployeeImage image = imageOpt.get();
            
            // Delete file from filesystem
            Path filePath = Paths.get(image.getImagePath());
            Files.deleteIfExists(filePath);
            
            // Delete from database
            employeeImageRepository.deleteById(imageId);
        }
    }
    
    /**
     * Delete all images for employee
     */
    public void deleteAllEmployeeImages(Long employeeId) throws IOException {
        List<EmployeeImage> images = employeeImageRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
        
        for (EmployeeImage image : images) {
            // Delete file from filesystem
            Path filePath = Paths.get(image.getImagePath());
            Files.deleteIfExists(filePath);
        }
        
        // Delete from database
        employeeImageRepository.deleteByEmployeeId(employeeId);
    }
    
    /**
     * Convert Entity to DTO
     */
    private EmployeeImageDTO convertToDTO(EmployeeImage image) {
        return new EmployeeImageDTO(
            image.getImageId(),
            image.getEmployeeId(),
            image.getImageName(),
            image.getImageUrl(),
            image.getImageType(),
            image.getImageSize(),
            image.getIsPrimary(),
            image.getUploadedAt()
        );
    }
}
