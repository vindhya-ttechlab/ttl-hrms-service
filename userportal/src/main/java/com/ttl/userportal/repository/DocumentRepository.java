package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document,Long>
{

    List<Document>findByUserId(Integer userId);

}
