package com.ttl.userportal.mapper;

import com.ttl.userportal.dto.CreateUserRequest;
import com.ttl.userportal.entity.Employee;
import com.ttl.userportal.entity.Users;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper class for converting between User and Employee entities
 * Uses mapping methods instead of get/set operations
 */
@Component
public class UserMapper {

    public Users mapToUser(CreateUserRequest request, String encodedPassword) {
        Users user = new Users();
        user.setEmpCode(request.getEmpCode());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setPhone(request.getPhone());
        user.setLocation(request.getLocation());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setEmergencyContact(request.getEmergencyContact());
        user.setEmergencyPhone(request.getEmergencyPhone());
        user.setAddress(request.getAddress());
        user.setPosition(request.getPosition());
        user.setDepartment(request.getDepartment());
        user.setJoinDate(request.getJoinDate());
        user.setExperience(request.getExperience());
        user.setEducation(request.getEducation());
        user.setTeam(request.getTeam());
        user.setManager(request.getManager());
        user.setSkills(request.getSkills());
        user.setLanguages(request.getLanguages());
        user.setAchievement(request.getAchievement());
        user.setStatus(Users.Status.Active);
        user.setIsFirstLogin(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Maps CreateUserRequest to Employee entity
     * @param request CreateUserRequest DTO
     * @return Employee entity
     */
    public Employee mapToEmployee(CreateUserRequest request) {
        Employee employee = new Employee();
        employee.setEmpCode(request.getEmpCode());
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setLocation(request.getLocation());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setEmergencyContact(request.getEmergencyContact());
        employee.setEmergencyPhone(request.getEmergencyPhone());
        employee.setAddress(request.getAddress());
        employee.setPosition(request.getPosition());
        employee.setDepartment(request.getDepartment());
        employee.setJoinDate(request.getJoinDate());
        employee.setExperience(request.getExperience());
        employee.setEducation(request.getEducation());
        employee.setTeam(request.getTeam());
        employee.setManagerId(request.getManager());
        employee.setStatus(Employee.Status.Active);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        return employee;
    }

    /**
     * Maps Users entity to Employee entity
     * @param user Users entity
     * @return Employee entity
     */
    public Employee mapUserToEmployee(Users user) {
        Employee employee = new Employee();
        employee.setEmpCode(user.getEmpCode());
        employee.setName(user.getName());
        employee.setEmail(user.getEmail());
        employee.setPhone(user.getPhone());
        employee.setLocation(user.getLocation());
        employee.setDateOfBirth(user.getDateOfBirth());
        employee.setEmergencyContact(user.getEmergencyContact());
        employee.setEmergencyPhone(user.getEmergencyPhone());
        employee.setAddress(user.getAddress());
        employee.setPosition(user.getPosition());
        employee.setDepartment(user.getDepartment());
        employee.setJoinDate(user.getJoinDate());
        employee.setExperience(user.getExperience());
        employee.setEducation(user.getEducation());
        employee.setTeam(user.getTeam());
        employee.setManagerId(user.getManager());
        employee.setProfileImage(user.getProfileImage());
        employee.setStatus(user.getStatus() != null ? 
            Employee.Status.valueOf(user.getStatus().name()) : Employee.Status.Active);
        employee.setCreatedAt(user.getCreatedAt());
        employee.setUpdatedAt(user.getUpdatedAt());
        return employee;
    }
}

