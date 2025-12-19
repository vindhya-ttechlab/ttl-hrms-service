package com.ttl.userportal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDTO {
    private Long id;
    private String templateCode;
    private String templateName;
    private String subject;
    private String body;
    private String placeholdersJson;
    private List<PlaceholderDefinition> placeholders; // Parsed placeholders for convenience
    private String description;
    private Boolean isHtml;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

