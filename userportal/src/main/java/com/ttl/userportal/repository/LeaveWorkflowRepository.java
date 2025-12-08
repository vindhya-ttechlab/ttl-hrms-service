package com.ttl.userportal.repository;

import com.ttl.userportal.entity.LeaveWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveWorkflowRepository extends JpaRepository<LeaveWorkflow, Integer> {
    Optional<LeaveWorkflow> findByWorkflowName(String workflowName);
    List<LeaveWorkflow> findByIsActiveTrueOrderByWorkflowName();
    Optional<LeaveWorkflow> findByIsDefaultTrueAndIsActiveTrue();
    boolean existsByWorkflowName(String workflowName);
}

