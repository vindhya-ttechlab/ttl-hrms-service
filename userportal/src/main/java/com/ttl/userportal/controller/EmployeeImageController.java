package com.ttl.userportal.controller;

import com.ttl.userportal.dto.EmployeeImageDTO;
import com.ttl.userportal.service.EmployeeImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class EmployeeImageController {
    
    @Autowired
    private EmployeeImageService employeeImageService;
    
    /**
     * Upload employee image
     */
//    @PostMapping("/employee/{employeeId}/upload")
//    public ResponseEntity<?> uploadEmployeeImage(
//            @PathVariable Long employeeId,
//            @RequestParam("file") MultipartFile file) {
//        try {
//            EmployeeImageDTO imageDTO = employeeImageService.uploadEmployeeImage(employeeId, file);
//            return ResponseEntity.ok(imageDTO);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error uploading file: " + e.getMessage());
//        }
//    }
    
    /**
     * Get primary image for employee
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeePrimaryImage(@PathVariable Long employeeId) {
        Optional<EmployeeImageDTO> imageDTO = employeeImageService.getPrimaryImageByEmployeeId(employeeId);
        if (imageDTO.isPresent()) {
            return ResponseEntity.ok(imageDTO.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all images for employee
     */
    @GetMapping("/employee/{employeeId}/all")
    public ResponseEntity<List<EmployeeImageDTO>> getAllEmployeeImages(@PathVariable Long employeeId) {
        List<EmployeeImageDTO> images = employeeImageService.getAllImagesByEmployeeId(employeeId);
        return ResponseEntity.ok(images);
    }
    
    /**
     * Get primary images for multiple employees
     */
    @PostMapping("/employees/primary")
    public ResponseEntity<List<EmployeeImageDTO>> getPrimaryImagesForEmployees(
            @RequestBody List<Long> employeeIds) {
        List<EmployeeImageDTO> images = employeeImageService.getPrimaryImagesByEmployeeIds(employeeIds);
        return ResponseEntity.ok(images);
    }
    
    /**
     * Serve image file
     */
    @GetMapping("/employee/{employeeId}/{filename}")
    public ResponseEntity<Resource> serveImage(
            @PathVariable Long employeeId,
            @PathVariable String filename) {
        try {
            // Construct file path
            Path filePath = Paths.get("uploads/employees", employeeId.toString(), filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = "application/octet-stream";
                if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.toLowerCase().endsWith(".webp")) {
                    contentType = "image/webp";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete employee image
     */
    @DeleteMapping("/employee/{imageId}")
    public ResponseEntity<?> deleteEmployeeImage(@PathVariable Long imageId) {
        try {
            employeeImageService.deleteEmployeeImage(imageId);
            return ResponseEntity.ok().body("Image deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting image: " + e.getMessage());
        }
    }
    
    /**
     * Delete all images for employee
     */
    @DeleteMapping("/employee/{employeeId}/all")
    public ResponseEntity<?> deleteAllEmployeeImages(@PathVariable Long employeeId) {
        try {
            employeeImageService.deleteAllEmployeeImages(employeeId);
            return ResponseEntity.ok().body("All images deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting images: " + e.getMessage());
        }
    }
}
