package com.ttl.userportal.repository;

import com.ttl.userportal.entity.EmailDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailDetailsRepository extends JpaRepository<EmailDetails, Long> {
}
