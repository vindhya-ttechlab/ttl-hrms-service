package com.ttl.userportal.repository;

import com.ttl.userportal.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    
    Optional<EmailTemplate> findByTemplateCodeAndIsActiveTrue(String templateCode);
    
    Optional<EmailTemplate> findByTemplateCode(String templateCode);
    
    List<EmailTemplate> findByIsActiveTrue();
    
    boolean existsByTemplateCode(String templateCode);
}

