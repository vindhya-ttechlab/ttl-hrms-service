package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    
    Optional<Employee> findByEmpCode(String empCode);
    
    Optional<Employee> findByEmail(String email);
    
    Optional<Employee> findByEmployeeIdAndStatus(Integer employeeId, Employee.Status status);

    Employee findByUserId(Long userId);
}

