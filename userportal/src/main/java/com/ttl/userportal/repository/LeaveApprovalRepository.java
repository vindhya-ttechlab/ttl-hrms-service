package com.ttl.userportal.repository;

import com.ttl.userportal.entity.LeaveApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Long> {
    List<LeaveApproval> findByLeaveIdOrderByCreatedAtAsc(Long leaveId);
    List<LeaveApproval> findByLeaveIdAndApprovalStatus(Long leaveId, String status);
    Optional<LeaveApproval> findByLeaveIdAndStepIdAndApprovalStatus(Long leaveId, Integer stepId, String status);
    List<LeaveApproval> findByApproverIdAndApprovalStatus(Integer approverId, String status);
}

