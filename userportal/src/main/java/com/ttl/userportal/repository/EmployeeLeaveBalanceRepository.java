package com.ttl.userportal.repository;

import com.ttl.userportal.entity.EmployeeLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLeaveBalanceRepository extends JpaRepository<EmployeeLeaveBalance, Long> {
    
    Optional<EmployeeLeaveBalance> findByEmployeeIdAndLeaveTypeId(Integer employeeId, Integer leaveTypeId);
    
    List<EmployeeLeaveBalance> findByEmployeeId(Long employeeId);
    
    void deleteByEmployeeIdAndLeaveTypeId(Long employeeId, Integer leaveTypeId);

}

