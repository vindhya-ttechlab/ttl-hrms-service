package com.ttl.userportal.service;

import com.ttl.userportal.dto.*;
import com.ttl.userportal.entity.BankDetails;
import com.ttl.userportal.entity.Projects;
import com.ttl.userportal.entity.Users;
import com.ttl.userportal.entity.UserRoleMap;
import com.ttl.userportal.entity.Role;
import com.ttl.userportal.repository.BankDetailsRepository;
import com.ttl.userportal.repository.DocumentRepository;
import com.ttl.userportal.repository.ProjectRepository;
import com.ttl.userportal.repository.UserRepository;
import com.ttl.userportal.repository.UserRoleMapRepository;
import com.ttl.userportal.repository.RoleRepository;
import com.ttl.userportal.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserService 
{
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeImageService employeeImageService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BankDetailsRepository bankDetailsRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRoleMapRepository userRoleMapRepository;

    @Autowired
    private RoleRepository roleRepository;



    public Users createUser(CreateUserRequest createUserRequest) {
        log.info("Creating user: {}", createUserRequest.getEmail());
        
        // Check if user already exists
        Users existingUser = userRepository.findByEmail(createUserRequest.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("User with email " + createUserRequest.getEmail() + " already exists");
        }
        
        // Create new user
        Users user = new Users();
        user.setEmpCode(createUserRequest.getEmpCode());
        user.setName(createUserRequest.getName());
        user.setEmail(createUserRequest.getEmail());
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setPhone(createUserRequest.getPhone());
        user.setLocation(createUserRequest.getLocation());
        user.setDateOfBirth(createUserRequest.getDateOfBirth());
        user.setEmergencyContact(createUserRequest.getEmergencyContact());
        user.setEmergencyPhone(createUserRequest.getEmergencyPhone());
        user.setAddress(createUserRequest.getAddress());
        user.setPosition(createUserRequest.getPosition());
        user.setDepartment(createUserRequest.getDepartment());
        user.setJoinDate(createUserRequest.getJoinDate());
        user.setExperience(createUserRequest.getExperience());
        user.setEducation(createUserRequest.getEducation());
        user.setTeam(createUserRequest.getTeam());
        user.setManager(createUserRequest.getManager());
        user.setSkills(createUserRequest.getSkills());
        user.setLanguages(createUserRequest.getLanguages());
        user.setAchievement(createUserRequest.getAchievement());
        user.setStatus(Users.Status.Active);
        user.setIsFirstLogin(true); // New users must change password on first login
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save user first to get the generated ID
        Users savedUser = userRepository.save(user);
        
        // Assign default role (role_id = 1, which should be Employee)
        assignDefaultRole(savedUser.getId());
        
        return savedUser;
    }

    /**
     * Assign default role (Employee - role_id = 1) to a user
     * @param userId The user ID
     */
    private void assignDefaultRole(Integer userId) {
        try {
            // Get default role (Employee with role_id = 1)
            Role defaultRole = roleRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Default role (Employee) not found. Please ensure roles are initialized in the database."));
            
            // Check if user already has this role
            if (!userRoleMapRepository.existsByUserIdAndRoleId(userId, defaultRole.getRoleId())) {
                UserRoleMap userRoleMap = new UserRoleMap();
                userRoleMap.setUserId(userId);
                userRoleMap.setRoleId(defaultRole.getRoleId());
                userRoleMap.setIsActive(true);
                userRoleMap.setCreatedAt(LocalDateTime.now());
                userRoleMap.setUpdatedAt(LocalDateTime.now());
                
                userRoleMapRepository.save(userRoleMap);
                log.info("Assigned default role '{}' to user with ID: {}", defaultRole.getRoleName(), userId);
            } else {
                log.info("User with ID {} already has role '{}'", userId, defaultRole.getRoleName());
            }
        } catch (Exception e) {
            log.error("Error assigning default role to user with ID: {}", userId, e);
            // Don't throw exception - user creation should succeed even if role assignment fails
            // Role can be assigned later manually
        }
    }


    public LoginResponse loginUser(LoginRequest loginRequest)
    {
        log.info("Login User method...");
        Users users=userRepository.findByEmail(loginRequest.getUserName());
        LoginResponse loginResponse=new LoginResponse();
        
        if (users == null) {
            log.error("User not found with email: {}", loginRequest.getUserName());
            throw new RuntimeException("User not found");
        }
        
        // Validate password
        if (!passwordEncoder.matches(loginRequest.getPassword(), users.getPassword())) {
            log.error("Invalid password for user: {}", loginRequest.getUserName());
            throw new RuntimeException("Invalid credentials");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(users.getEmail(), users.getId());
        
        // Get user role IDs
        List<Integer> userRoleIds = userRoleMapRepository.findActiveRoleIdsByUserId(users.getId());
        
        // Get user role names
        List<String> userRoles = getUserRoles(users.getId());
        
        // Get primary role ID (first role, or default to 1 for Employee)
        Integer primaryRoleId = userRoleIds.isEmpty() ? 1 : userRoleIds.get(0);
        
        loginResponse.setEmail(users.getEmail());
        loginResponse.setName(users.getName());
        loginResponse.setPhoneNumber(users.getPhone());
        loginResponse.setToken(token);
        loginResponse.setUserId(users.getId());
        loginResponse.setRoles(userRoles);
        loginResponse.setRoleIds(userRoleIds);
        loginResponse.setPrimaryRoleId(primaryRoleId);
        loginResponse.setIsFirstLogin(users.getIsFirstLogin() != null ? users.getIsFirstLogin() : true);

        return loginResponse;
    }

    /**
     * Get all active role names for a user
     * @param userId The user ID
     * @return List of role names
     */
    public List<String> getUserRoles(Integer userId) {
        List<Integer> roleIds = userRoleMapRepository.findActiveRoleIdsByUserId(userId);
        return roleRepository.findAllById(roleIds)
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    /**
     * Assign a role to a user (many-to-many relationship)
     * @param userId The user ID
     * @param roleId The role ID
     */
    public void assignRoleToUser(Integer userId, Integer roleId) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }
        
        // Check if role exists
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role with ID " + roleId + " not found");
        }
        
        // Check if mapping already exists
        if (!userRoleMapRepository.existsByUserIdAndRoleId(userId, roleId)) {
            UserRoleMap userRoleMap = new UserRoleMap();
            userRoleMap.setUserId(userId);
            userRoleMap.setRoleId(roleId);
            userRoleMap.setIsActive(true);
            userRoleMap.setCreatedAt(LocalDateTime.now());
            userRoleMap.setUpdatedAt(LocalDateTime.now());
            
            userRoleMapRepository.save(userRoleMap);
            log.info("Assigned role ID {} to user ID {}", roleId, userId);
        } else {
            log.info("User ID {} already has role ID {}", userId, roleId);
        }
    }

    /**
     * Remove a role from a user
     * @param userId The user ID
     * @param roleId The role ID
     */
    public void removeRoleFromUser(Integer userId, Integer roleId) {
        userRoleMapRepository.deleteByUserIdAndRoleId(userId, roleId);
        log.info("Removed role ID {} from user ID {}", roleId, userId);
    }

    public EmployeeDTO userDetails(UserDetails userDetails) {
        String userName = userDetails.getEmail();
        log.info("User Details method for user: {}", userName);
        Users users = userRepository.findByEmail(userName);

        if (users == null) {
            log.error("User not found with email: {}", userName);
            throw new RuntimeException("User not found with email: " + userName);
        }

        EmployeeDTO employeeDetails = new EmployeeDTO();

        // Personal Info mapping
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setId(String.valueOf(users.getId()));
        personalInfo.setName(users.getName());
        personalInfo.setEmployeeId(users.getEmpCode());
        personalInfo.setEmail(users.getEmail());
        personalInfo.setPhone(users.getPhone());
        personalInfo.setLocation(users.getLocation());
        personalInfo.setDateOfBirth(users.getDateOfBirth() != null ? users.getDateOfBirth().toString() : null);
        personalInfo.setEmergencyContact(users.getEmergencyContact());
        personalInfo.setEmergencyPhone(users.getEmergencyPhone());
        personalInfo.setAddress(users.getAddress());
        // Set profile image - try new image service first, fallback to old field
        String profileImageUrl = null;
        try {
            // Try to get image from new employee_images table
            Optional<EmployeeImageDTO> imageData = employeeImageService.getPrimaryImageByEmployeeId(users.getId().longValue());
            if (imageData.isPresent()) {
                profileImageUrl = imageData.get().getImageUrl();
                log.info("Found image from employee_images table for user: {}", users.getEmail());
            } else {
                // Fallback to old profile_image field
                profileImageUrl = users.getProfileImage();
                log.info("Using fallback profile_image field for user: {}", users.getEmail());
            }
        } catch (Exception e) {
            // If there's any error, fallback to old field
            profileImageUrl = users.getProfileImage();
            log.warn("Error fetching image from employee_images table, using fallback for user: {}, error: {}",
                    users.getEmail(), e.getMessage());
        }
        personalInfo.setProfileImage(profileImageUrl);
        personalInfo.setStatus(String.valueOf(users.getStatus()));

        employeeDetails.setPersonalInfo(personalInfo);

        // Professional Info mapping
        ProfessionalInfo professionalInfo = new ProfessionalInfo();
        professionalInfo.setPosition(users.getPosition());
        professionalInfo.setDepartment(users.getDepartment());
        professionalInfo.setJoinDate(users.getJoinDate() != null ? users.getJoinDate().toString() : null);
        professionalInfo.setExperience(users.getExperience());
        professionalInfo.setTeam(users.getTeam());
        professionalInfo.setSkills(splitCsvToList(users.getSkills()));
        professionalInfo.setEducation(users.getEducation());
        professionalInfo.setLanguages(splitCsvToList(users.getLanguages()));
        professionalInfo.setAchievements(splitCsvToList(users.getAchievement()));

        if (users.getManager() != null) {
            Users managerData = userRepository.findByIdAndStatus(users.getManager(), Users.Status.Active);
            if (managerData != null) {
                ManagerDTO manager = new ManagerDTO();
                manager.setName(managerData.getName());
                manager.setPosition(managerData.getPosition());
                manager.setEmail(managerData.getEmail());
                manager.setPhone(managerData.getPhone());
                manager.setId(managerData.getId());

            String managerImg = null;
            try {
                // Try to get image from new employee_images table
                Optional<EmployeeImageDTO> imageData = employeeImageService.getPrimaryImageByEmployeeId(managerData.getId().longValue());
                if (imageData.isPresent()) {
                    managerImg = imageData.get().getImageUrl();
                    log.info("Found image from employee_images table for manager: {}", managerData.getEmail());
                } else {
                    // Fallback to manager's old profile_image field
                    managerImg = managerData.getProfileImage();
                    log.info("Using fallback profile_image field for manager: {}", managerData.getEmail());
                }
            } catch (Exception e) {
                // If there's any error, fallback to manager's old field
                managerImg = managerData.getProfileImage();
                log.warn("Error fetching image from employee_images table for manager: {}, error: {}",
                        managerData.getEmail(), e.getMessage());
            }

            manager.setProfileImage(managerImg);
            professionalInfo.setManager(manager);
        } else {
            log.warn("Manager with ID {} not found or not active for user {}", users.getManager(), users.getEmail());
        }
    }

        // Projects mapping

        List<Projects>projects=projectRepository.findByEmployeeId(users.getId());
        List<ProjectDTO>listOfProjectDTO=new ArrayList<>();

        if(projects!=null)
        {
            for(Projects p: projects)
            {
                ProjectDTO project=new ProjectDTO();
                project.setName(p.getName());
                project.setId(p.getId());
                project.setStatus(p.getStatus());
                project.setProgress(p.getProgress());
                project.setStartDate(String.valueOf(p.getStartDate()));
                project.setEndDate(String.valueOf(p.getEndDate()));
                project.setTeamSize(p.getTeamSize());
                listOfProjectDTO.add(project);
            }



            professionalInfo.setCurrentProjects(listOfProjectDTO);

        }

        employeeDetails.setProfessionalInfo(professionalInfo);

        return employeeDetails;
    }


    // Get all employees
    public List<EmployeeDetails> getAllEmployees() {
        List<Users> allUsers = userRepository.findAll();
        return mapToEmployeeDetails(allUsers);
    }

    // Search employees by name, email, or empCode
    public List<EmployeeDetails> searchEmployees(String search) {
        List<Users> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrEmpCodeContainingIgnoreCase(search, search, search);
        return mapToEmployeeDetails(users);
    }

    // Helper method to map Users -> EmployeeDetails
    private List<EmployeeDetails> mapToEmployeeDetails(List<Users> users) {
        List<EmployeeDetails> employeeDetailsList = new ArrayList<>();

        for (Users user : users) {
            EmployeeDetails emp = new EmployeeDetails();
            emp.setId(user.getId());
            emp.setName(user.getName());
            emp.setEmail(user.getEmail());
            emp.setPhone(user.getPhone());
            emp.setDesignation(user.getPosition());
            emp.setDepartment(user.getDepartment());
            emp.setJoiningDate(user.getJoinDate());
            emp.setIsActive(user.getStatus() == Users.Status.Active);
            emp.setCreatedDate(user.getCreatedAt().toLocalDate());
            emp.setEmployeeCode(user.getEmpCode());
            emp.setCreatedBy("Admin"); // or fetch if you store createdBy

            // Bank Details
            BankDetails bank = bankDetailsRepository.findByUserId(user.getId());
            if (bank != null) {
                emp.setBankName(bank.getBankName());
                emp.setAccountNo(bank.getAccountNumber());
                emp.setIfscCode(bank.getIfscCode());
                emp.setBranchName(bank.getBranchName());
            }

            // Documents
            List<DocumentDTO> docs = documentRepository.findByUserId(user.getId())
                    .stream()
                    .map(d -> new DocumentDTO(
                            d.getId(),
                            d.getDocumentName(),
                            d.getDocumentType(),
                            d.getDocumentUrl(),
                            d.getVerified(),
                            d.getUser().getId()
                    ))
                    .collect(Collectors.toList());
            emp.setDocuments(docs);

            employeeDetailsList.add(emp);
        }

        return employeeDetailsList;
    }

    private List<String> splitCsvToList(String csv) {
        if (csv == null || csv.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Change user password (for first-time login or regular password change)
     * @param userId The user ID
     * @param changePasswordRequest The change password request
     * @return true if password changed successfully
     */
    public boolean changePassword(Integer userId, ChangePasswordRequest changePasswordRequest) {
        log.info("Changing password for user ID: {}", userId);
        
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate current password if it's not first login
        if (user.getIsFirstLogin() == null || !user.getIsFirstLogin()) {
            if (changePasswordRequest.getCurrentPassword() == null || 
                !passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
        }
        
        // Validate new password and confirm password match
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }
        
        // Validate password strength (minimum 8 characters)
        if (changePasswordRequest.getNewPassword().length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user.setIsFirstLogin(false); // Mark that user has changed password
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);
        
        return true;
    }
}
