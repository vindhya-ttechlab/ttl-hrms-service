package com.ttl.userportal.mapper;

import com.ttl.userportal.dto.CreateUserRequest;
import com.ttl.userportal.entity.Employee;
import com.ttl.userportal.entity.Users;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        imports = { LocalDateTime.class }
)
public interface UserMapper {

    /* ---------------- CreateUserRequest → Users ---------------- */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "profileImage", ignore = true),
            @Mapping(target = "password", source = "encodedPassword"),
            @Mapping(target = "status", constant = "Active"),
            @Mapping(target = "isFirstLogin", constant = "true"),
            @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
            @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    })
    Users mapToUser(CreateUserRequest request, String encodedPassword);


    /* ---------------- Users → Employee (for user creation) ---------------- */
    @Mappings({
            @Mapping(target = "employeeId", ignore = true),
            @Mapping(target = "userId", expression = "java(user.getId() != null ? user.getId().longValue() : null)"),
            @Mapping(target = "profileImage", ignore = true),
            @Mapping(target = "manager", ignore = true),     // IMPORTANT
            @Mapping(target = "managerId", source = "manager"),
            @Mapping(target = "status", constant = "Active"),
            @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
            @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    })
    Employee mapToEmployee(Users user);


    /* ---------------- Users → Employee (alternative method) ---------------- */
    @Mappings({
            @Mapping(target = "employeeId", ignore = true),
            @Mapping(target = "userId", expression = "java(user.getId() != null ? user.getId().longValue() : null)"),
            @Mapping(target = "profileImage", ignore = true),
            @Mapping(target = "manager", ignore = true),
            @Mapping(target = "managerId", source = "manager"),
            @Mapping(
                    target = "status",
                    expression = "java(user.getStatus() != null ? " +
                            "Employee.Status.valueOf(user.getStatus().name()) : Employee.Status.Active)"
            ),
            @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())"),
            @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    })
    Employee mapUserToEmployee(Users user);
}
