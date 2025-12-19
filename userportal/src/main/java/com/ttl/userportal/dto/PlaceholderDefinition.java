package com.ttl.userportal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a placeholder definition stored in email_templates.placeholders_json
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceholderDefinition {
    
    /**
     * The placeholder key used in the template (e.g., "employee_name" for {{employee_name}})
     */
    private String key;
    
    /**
     * Human-readable label for the placeholder
     */
    private String label;
    
    /**
     * The field name in the source data object to map from
     */
    private String sourceField;
    
    /**
     * Default value if the source field is null or empty
     */
    private String defaultValue;
    
    /**
     * Whether this placeholder is required
     */
    private Boolean required;
}

