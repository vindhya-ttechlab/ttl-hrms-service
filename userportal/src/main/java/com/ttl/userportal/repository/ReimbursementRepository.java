package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Reimbursement;
import com.ttl.userportal.enums.ReimbursementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, Long> {

    List<Reimbursement> findByUserIdAndIsActiveTrueOrderByAppliedDateDesc(Integer userId);

    List<Reimbursement> findByUserIdAndStatusAndIsActiveTrueOrderByAppliedDateDesc(Integer userId, ReimbursementStatus status);

    @Query("SELECT COALESCE(SUM(r.approvedAmount), 0) FROM Reimbursement r " + "WHERE r.userId = :userId AND YEAR(r.approvedDate) = :year " + "AND r.status = :status AND r.isActive = true")
    BigDecimal sumApprovedAmountByUserAndYear(@Param("userId") Integer userId, @Param("year") int year, @Param("status") ReimbursementStatus status);

    List<Reimbursement> findByApproverIdAndStatus(Integer approverId, ReimbursementStatus status);

    List<Reimbursement> findByStatus(ReimbursementStatus status);
}

