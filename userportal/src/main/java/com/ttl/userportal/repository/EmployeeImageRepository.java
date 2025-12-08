package com.ttl.userportal.repository;

import com.ttl.userportal.entity.EmployeeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeImageRepository extends JpaRepository<EmployeeImage, Long> {
    
    /**
     * Find the primary image for an employee
     */
    @Query("SELECT ei FROM EmployeeImage ei WHERE ei.employeeId = :employeeId")
    Optional<EmployeeImage> findPrimaryImageByEmployeeId(@Param("employeeId") Long employeeId);
    
    /**
     * Find all images for an employee
     */
    List<EmployeeImage> findByEmployeeIdOrderByUploadedAtDesc(Long employeeId);
    
    /**
     * Find all primary images for multiple employees
     */
    @Query("SELECT ei FROM EmployeeImage ei WHERE ei.employeeId IN :employeeIds AND ei.isPrimary = true")
    List<EmployeeImage> findPrimaryImagesByEmployeeIds(@Param("employeeIds") List<Long> employeeIds);
    
    /**
     * Check if employee has any images
     */
    boolean existsByEmployeeId(Long employeeId);
    
    /**
     * Count images for an employee
     */
    long countByEmployeeId(Long employeeId);
    
    /**
     * Delete all images for an employee
     */
    void deleteByEmployeeId(Long employeeId);
}
