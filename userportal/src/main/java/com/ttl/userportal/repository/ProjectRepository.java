package com.ttl.userportal.repository;

import com.ttl.userportal.entity.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface  ProjectRepository extends JpaRepository<Projects,Long>
{
    List<Projects>findByEmployeeId(int empId);

}
