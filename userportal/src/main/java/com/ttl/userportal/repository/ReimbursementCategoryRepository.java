package com.ttl.userportal.repository;

import com.ttl.userportal.entity.ReimbursementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementCategoryRepository extends JpaRepository<ReimbursementCategory, Integer> {
    List<ReimbursementCategory> findByIsActiveTrueOrderByCategoryNameAsc();

    boolean existsByCategoryName(String categoryName);
}

