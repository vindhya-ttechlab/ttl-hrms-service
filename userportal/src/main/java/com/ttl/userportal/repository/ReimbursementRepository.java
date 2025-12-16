package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Reimbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {
    
    // Find all reimbursements for a user
    List<Reimbursement> findByUserIdAndIsActiveTrueOrderByAppliedDateDesc(Integer userId);
    
    // Find by status for a user
    List<Reimbursement> findByUserIdAndStatusAndIsActiveTrueOrderByAppliedDateDesc(
            Integer userId, Reimbursement.ReimbursementStatus status);
    
    // Find pending approvals for a manager (approver)
    List<Reimbursement> findByApproverIdAndStatusAndIsActiveTrueOrderByAppliedDateAsc(
            Integer approverId, Reimbursement.ReimbursementStatus status);
    
    // Find all pending approvals for a manager
    @Query("SELECT r FROM Reimbursement r WHERE r.approverId = :approverId " +
           "AND r.status = 'PENDING' AND r.isActive = true ORDER BY r.appliedDate ASC")
    List<Reimbursement> findPendingApprovalsByApproverId(@Param("approverId") Integer approverId);
    
    // Find reimbursements by date range for a user
    @Query("SELECT r FROM Reimbursement r WHERE r.userId = :userId " +
           "AND r.expenseDate BETWEEN :startDate AND :endDate " +
           "AND r.isActive = true ORDER BY r.expenseDate DESC")
    List<Reimbursement> findByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    // Find reimbursements by year for a user (for summary)
    @Query("SELECT r FROM Reimbursement r WHERE r.userId = :userId " +
           "AND YEAR(r.expenseDate) = :year AND r.isActive = true")
    List<Reimbursement> findByUserIdAndYear(@Param("userId") Integer userId, @Param("year") int year);
    
    // Find reimbursements by category for a user
    List<Reimbursement> findByUserIdAndCategoryIdAndIsActiveTrueOrderByAppliedDateDesc(
            Integer userId, Integer categoryId);
    
    // Count pending reimbursements for approver
    @Query("SELECT COUNT(r) FROM Reimbursement r WHERE r.approverId = :approverId " +
           "AND r.status = 'PENDING' AND r.isActive = true")
    Long countPendingByApproverId(@Param("approverId") Integer approverId);
    
    // Sum of approved amounts for a user in a year
    @Query("SELECT COALESCE(SUM(r.approvedAmount), 0) FROM Reimbursement r " +
           "WHERE r.userId = :userId AND YEAR(r.approvedDate) = :year " +
           "AND r.status = 'APPROVED' AND r.isActive = true")
    java.math.BigDecimal sumApprovedAmountByUserAndYear(
            @Param("userId") Integer userId, @Param("year") int year);
    
    // Find all reimbursements that need approval (for HR/Admin)
    @Query("SELECT r FROM Reimbursement r WHERE r.status = 'PENDING' " +
           "AND r.isActive = true ORDER BY r.appliedDate ASC")
    List<Reimbursement> findAllPendingReimbursements();

    List<Reimbursement> findByApproverIdAndStatus(Integer approverId, String status);

    List<Reimbursement> findByStatus(String status);
}

