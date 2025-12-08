package com.ttl.userportal.repository;

import com.ttl.userportal.entity.BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankDetailsRepository extends JpaRepository<BankDetails, Integer> {
    BankDetails findByUserId(Integer userId);
}
