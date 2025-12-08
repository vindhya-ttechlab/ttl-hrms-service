package com.ttl.userportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ttl.userportal.entity.Users;

import java.util.List;

public interface UserRepository extends JpaRepository <Users, Integer> {
    Users findByEmail(String email);

    Users findByIdAndStatus(Integer id, Users.Status status);

    Users findById(Long id);
    List<Users> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrEmpCodeContainingIgnoreCase(
            String name, String email, String empCode);
}
 