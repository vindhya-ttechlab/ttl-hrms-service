package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    
    Optional<Department> findByDepartmentName(String departmentName);
    
    List<Department> findByIsActiveTrue();
    
    boolean existsByDepartmentName(String departmentName);
    
    List<Department> findByHeadId(Integer headId);
}

