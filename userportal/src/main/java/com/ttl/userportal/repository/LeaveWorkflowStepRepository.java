package com.ttl.userportal.repository;

import com.ttl.userportal.entity.LeaveWorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveWorkflowStepRepository extends JpaRepository<LeaveWorkflowStep, Integer> {
    List<LeaveWorkflowStep> findByWorkflowWorkflowIdOrderByStepOrder(Integer workflowId);
    void deleteByWorkflowWorkflowId(Integer workflowId);
}

