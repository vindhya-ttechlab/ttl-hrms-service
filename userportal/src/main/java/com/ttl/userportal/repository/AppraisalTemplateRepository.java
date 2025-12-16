package com.ttl.userportal.repository;

import com.ttl.userportal.entity.AppraisalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalTemplateRepository extends JpaRepository<AppraisalTemplate, Long> {
    List<AppraisalTemplate> findAllByRecordStatus(boolean status);
    Optional<AppraisalTemplate> findByIdAndRecordStatus(Long id, boolean status);
}
