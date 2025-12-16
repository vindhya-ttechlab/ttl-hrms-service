package com.ttl.userportal.repository;

import com.ttl.userportal.entity.ReimbursementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReimbursementCategoryRepository extends JpaRepository<ReimbursementCategory, Integer> {
    
    // Find all active categories
    List<ReimbursementCategory> findByIsActiveTrueOrderByCategoryNameAsc();
    
    // Find by name
    Optional<ReimbursementCategory> findByCategoryName(String categoryName);
    
    // Check if category exists by name
    boolean existsByCategoryName(String categoryName);
}

