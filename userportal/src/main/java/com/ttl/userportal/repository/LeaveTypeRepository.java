package com.ttl.userportal.repository;

import com.ttl.userportal.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Integer> {
    Optional<LeaveType> findByTypeName(String typeName);
    List<LeaveType> findByIsActiveTrueOrderByTypeName();
    boolean existsByTypeName(String typeName);
}

