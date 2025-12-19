package com.ttl.userportal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttl.userportal.dto.PlaceholderDefinition;
import com.ttl.userportal.entity.EmailTemplate;
import com.ttl.userportal.repository.EmailTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing email templates stored in the database.
 * Template codes are stored in the email_templates table and referenced dynamically.
 */
@Slf4j
@Service
public class EmailTemplateService {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get an email template by its code
     * @param templateCode The template code
     * @return Optional containing the template if found
     */
    public Optional<EmailTemplate> getTemplateByCode(String templateCode) {
        return emailTemplateRepository.findByTemplateCodeAndIsActiveTrue(templateCode);
    }

    /**
     * Get all active email templates
     * @return List of active templates
     */
    public List<EmailTemplate> getAllActiveTemplates() {
        return emailTemplateRepository.findByIsActiveTrue();
    }

    /**
     * Parse placeholders JSON from template to list of PlaceholderDefinition
     * @param template The email template
     * @return List of placeholder definitions
     */
    public List<PlaceholderDefinition> getPlaceholderDefinitions(EmailTemplate template) {
        if (template == null || template.getPlaceholdersJson() == null || template.getPlaceholdersJson().isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(template.getPlaceholdersJson(), 
                    new TypeReference<List<PlaceholderDefinition>>() {});
        } catch (Exception e) {
            log.error("Failed to parse placeholders JSON for template: {}", template.getTemplateCode(), e);
            return List.of();
        }
    }

    /**
     * Build placeholder map by matching sourceField from definitions with provided data map.
     * Also includes direct key matching as fallback for any sourceData keys.
     * 
     * @param templateCode The template code
     * @param sourceData Map of source data (key = field name, value = field value)
     * @return Map of placeholder key to resolved value
     */
    public Map<String, String> buildPlaceholderMap(String templateCode, Map<String, Object> sourceData) {
        Map<String, String> placeholders = new HashMap<>();
        
        // First, add all sourceData entries directly as placeholders (direct key matching)
        // This ensures {{key}} works if sourceData has "key"
        for (Map.Entry<String, Object> entry : sourceData.entrySet()) {
            Object value = entry.getValue();
            placeholders.put(entry.getKey(), value != null ? String.valueOf(value) : "");
        }
        
        Optional<EmailTemplate> templateOpt = getTemplateByCode(templateCode);
        if (templateOpt.isEmpty()) {
            log.warn("Template not found: {}", templateCode);
            return placeholders;
        }

        // Then, apply placeholder definitions from placeholders_json if available
        // This maps sourceField -> key (e.g., "name" -> "employee_name")
        List<PlaceholderDefinition> definitions = getPlaceholderDefinitions(templateOpt.get());
        
        for (PlaceholderDefinition def : definitions) {
            String value = null;
            
            // Try to get value from sourceData using sourceField
            if (def.getSourceField() != null && sourceData.containsKey(def.getSourceField())) {
                Object rawValue = sourceData.get(def.getSourceField());
                value = rawValue != null ? String.valueOf(rawValue) : null;
            }
            
            // Use default value if value is null or empty
            if (value == null || value.isEmpty() || "null".equals(value)) {
                value = def.getDefaultValue() != null ? def.getDefaultValue() : "";
            }
            
            // Map sourceField value to the placeholder key
            placeholders.put(def.getKey(), value);
        }
        
        log.debug("Built placeholder map for template {}: {}", templateCode, placeholders.keySet());
        return placeholders;
    }

    /**
     * Process a template by replacing placeholders with actual values
     * Placeholders are in the format {{placeholder_key}}
     * 
     * @param template The email template string
     * @param placeholders Map of placeholder names to values
     * @return Processed template with placeholders replaced
     */
    public String processTemplate(String template, Map<String, String> placeholders) {
        if (template == null || placeholders == null) {
            return template;
        }
        
        String processed = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            processed = processed.replace(placeholder, value);
        }
        
        return processed;
    }

    /**
     * Get processed email subject using dynamic placeholder mapping
     * @param templateCode The template code
     * @param sourceData Map of source data to match with placeholder definitions
     * @return Processed subject
     */
    public String getProcessedSubject(String templateCode, Map<String, Object> sourceData) {
        Optional<EmailTemplate> templateOpt = getTemplateByCode(templateCode);
        if (templateOpt.isPresent()) {
            Map<String, String> placeholders = buildPlaceholderMap(templateCode, sourceData);
            return processTemplate(templateOpt.get().getSubject(), placeholders);
        }
        log.warn("Email template not found for code: {}. Using default subject.", templateCode);
        return "Notification from TTL HRMS";
    }

    /**
     * Get processed email body using dynamic placeholder mapping
     * @param templateCode The template code
     * @param sourceData Map of source data to match with placeholder definitions
     * @return Processed body
     */
    public String getProcessedBody(String templateCode, Map<String, Object> sourceData) {
        Optional<EmailTemplate> templateOpt = getTemplateByCode(templateCode);
        if (templateOpt.isPresent()) {
            Map<String, String> placeholders = buildPlaceholderMap(templateCode, sourceData);
            return processTemplate(templateOpt.get().getBody(), placeholders);
        }
        log.warn("Email template not found for code: {}. Using default body.", templateCode);
        return "This is a notification from TTL HRMS.";
    }

    /**
     * Check if template is HTML
     * @param templateCode The template code
     * @return true if HTML template, false otherwise
     */
    public boolean isHtmlTemplate(String templateCode) {
        Optional<EmailTemplate> templateOpt = getTemplateByCode(templateCode);
        return templateOpt.map(EmailTemplate::getIsHtml).orElse(false);
    }

    /**
     * Save or update an email template
     * @param template The template to save
     * @return Saved template
     */
    public EmailTemplate saveTemplate(EmailTemplate template) {
        return emailTemplateRepository.save(template);
    }
}
