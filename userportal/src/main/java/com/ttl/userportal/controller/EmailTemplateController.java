package com.ttl.userportal.controller;

import com.ttl.userportal.dto.EmailTemplateDTO;
import com.ttl.userportal.entity.EmailTemplate;
import com.ttl.userportal.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/email-templates")
@CrossOrigin(origins = "http://localhost:3000")
public class EmailTemplateController {

    @Autowired
    private EmailTemplateService emailTemplateService;

    /**
     * Get all active email templates
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllTemplates() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<EmailTemplate> templates = emailTemplateService.getAllActiveTemplates();
            List<EmailTemplateDTO> templateDTOs = templates.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            response.put("status", "SUCCESS");
            response.put("data", templateDTOs);
            response.put("message", "Templates retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get a specific email template by code
     */
    @GetMapping("/{templateCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getTemplateByCode(@PathVariable String templateCode) {
        Map<String, Object> response = new HashMap<>();
        try {
            return emailTemplateService.getTemplateByCode(templateCode)
                    .map(template -> {
                        response.put("status", "SUCCESS");
                        response.put("data", convertToDTO(template));
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("status", "ERROR");
                        response.put("error", "Template not found: " + templateCode);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update an email template (Admin only)
     */
    @PutMapping("/{templateCode}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable String templateCode,
            @RequestBody EmailTemplateDTO templateDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            return emailTemplateService.getTemplateByCode(templateCode)
                    .map(existingTemplate -> {
                        // Update fields
                        if (templateDTO.getTemplateName() != null) {
                            existingTemplate.setTemplateName(templateDTO.getTemplateName());
                        }
                        if (templateDTO.getSubject() != null) {
                            existingTemplate.setSubject(templateDTO.getSubject());
                        }
                        if (templateDTO.getBody() != null) {
                            existingTemplate.setBody(templateDTO.getBody());
                        }
                        if (templateDTO.getDescription() != null) {
                            existingTemplate.setDescription(templateDTO.getDescription());
                        }
                        if (templateDTO.getIsHtml() != null) {
                            existingTemplate.setIsHtml(templateDTO.getIsHtml());
                        }
                        if (templateDTO.getIsActive() != null) {
                            existingTemplate.setIsActive(templateDTO.getIsActive());
                        }
                        if (templateDTO.getPlaceholdersJson() != null) {
                            existingTemplate.setPlaceholdersJson(templateDTO.getPlaceholdersJson());
                        }
                        
                        EmailTemplate savedTemplate = emailTemplateService.saveTemplate(existingTemplate);
                        response.put("status", "SUCCESS");
                        response.put("data", convertToDTO(savedTemplate));
                        response.put("message", "Template updated successfully");
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        response.put("status", "ERROR");
                        response.put("error", "Template not found: " + templateCode);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private EmailTemplateDTO convertToDTO(EmailTemplate template) {
        EmailTemplateDTO dto = new EmailTemplateDTO();
        dto.setId(template.getId());
        dto.setTemplateCode(template.getTemplateCode());
        dto.setTemplateName(template.getTemplateName());
        dto.setSubject(template.getSubject());
        dto.setBody(template.getBody());
        dto.setPlaceholdersJson(template.getPlaceholdersJson());
        dto.setPlaceholders(emailTemplateService.getPlaceholderDefinitions(template));
        dto.setDescription(template.getDescription());
        dto.setIsHtml(template.getIsHtml());
        dto.setIsActive(template.getIsActive());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        return dto;
    }
}

