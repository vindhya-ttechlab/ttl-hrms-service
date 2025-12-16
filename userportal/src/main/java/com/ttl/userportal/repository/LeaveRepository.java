package com.ttl.userportal.repository;

import com.ttl.userportal.entity.LeaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveEntity,Long>
{
    @Query("SELECT l FROM LeaveEntity l WHERE l.fromDate > :today AND l.employeeId = :employeeId")
    List<LeaveEntity> listOfUpcomingLeaves(@Param("today") LocalDate today, @Param("employeeId") Integer employeeId);

    @Query("SELECT l FROM LeaveEntity l WHERE l.fromDate <= :today AND l.employeeId = :employeeId")
    List<LeaveEntity> listOfPastLeaves(@Param("today") LocalDate today, @Param("employeeId") Integer employeeId);

    LeaveEntity findByIdAndIsActive(Long id, boolean status);

    @Query("SELECT l FROM LeaveEntity l WHERE YEAR(l.fromDate) = :year AND l.approver = :approverId")
    List<LeaveEntity> findByApprover(@Param("year") int year, @Param("approverId") Integer approverId);

}
