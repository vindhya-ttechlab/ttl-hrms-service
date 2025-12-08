package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Integer> {
    
    Optional<Designation> findByDesignationName(String designationName);
    
    List<Designation> findByIsActiveTrueOrderByLevel();
    
    boolean existsByDesignationName(String designationName);
}

