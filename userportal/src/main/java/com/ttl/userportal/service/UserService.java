package com.ttl.userportal.service;

import com.ttl.userportal.dto.*;
import com.ttl.userportal.entity.BankDetails;
import com.ttl.userportal.entity.Employee;
import com.ttl.userportal.entity.Projects;
import com.ttl.userportal.entity.Users;
import com.ttl.userportal.entity.UserRoleMap;
import com.ttl.userportal.entity.Role;
import com.ttl.userportal.mapper.UserMapper;
import com.ttl.userportal.repository.BankDetailsRepository;
import com.ttl.userportal.repository.DocumentRepository;
import com.ttl.userportal.repository.EmployeeRepository;
import com.ttl.userportal.repository.ProjectRepository;
import com.ttl.userportal.repository.UserRepository;
import com.ttl.userportal.repository.UserRoleMapRepository;
import com.ttl.userportal.repository.RoleRepository;
import com.ttl.userportal.util.JwtUtil;
import com.ttl.userportal.util.PasswordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Value("${app.user.default.role.id:1}")
    private Integer defaultRoleId;

    @Value("${app.user.default.role.name:Employee}")
    private String defaultRoleName;

    @Value("${app.email.template.new-user-welcome:NEW_USER_WELCOME}")
    private String newUserWelcomeTemplate;

    @Autowired
    private LeaveTypeService leaveTypeService;

    @Autowired
    private AccountActivationService accountActivationService;



    @Transactional
    public Users createUser(CreateUserRequest createUserRequest) {
        log.info("Creating user: {}", createUserRequest.getEmail());
        
        Users existingUser = userRepository.findByEmail(createUserRequest.getEmail());
        if (existingUser != null) {
            throw new RuntimeException("User with email " + createUserRequest.getEmail() + " already exists");
        }
        Employee existingEmployee = employeeRepository.findByEmail(createUserRequest.getEmail()).orElse(null);
        if (existingEmployee != null) {
            throw new RuntimeException("Employee with email " + createUserRequest.getEmail() + " already exists");
        }
        
        // Generate a temporary random password (user will set their own via activation link)
        String tempPassword = PasswordGenerator.generatePassword(16);
        String encodedPassword = passwordEncoder.encode(tempPassword);
        
        Users user = userMapper.mapToUser(createUserRequest, encodedPassword);
        
        Users savedUser = userRepository.save(user);
        log.info("User saved with ID: {}", savedUser.getId());
        
        Employee employee = userMapper.mapToEmployee(savedUser);
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee saved with ID: {}", savedEmployee.getEmployeeId());
        
        Integer existingRoleId = createUserRequest.getRoleId();
        if (existingRoleId == null) {
            assignDefaultRoleFromProperties(savedUser.getId());
        } else {
            assignRoleToUser(savedUser.getId(), existingRoleId);
        }

        leaveTypeService.createBalance(savedEmployee.getEmployeeId());
        
        // Generate activation token and send welcome email with activation link
        try {
            String activationUrl = accountActivationService.generateActivationToken(savedUser);
            accountActivationService.sendActivationEmail(savedUser, activationUrl);
            log.info("Activation email sent to user: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send activation email to user: {}. Error: {}", savedUser.getEmail(), e.getMessage());
            // Don't fail the user creation if email fails
        }
        
        return savedUser;
    }

    private void assignDefaultRoleFromProperties(Integer userId) {
        try {
            log.info("Assigning default role (ID: {}, Name: {}) to user ID: {}", defaultRoleId, defaultRoleName, userId);

            // Get default role from application properties
            Role defaultRole = roleRepository.findById(defaultRoleId)
                    .orElseThrow(() -> new RuntimeException(
                        String.format("Default role with ID %d (%s) not found. Please ensure roles are initialized in the database.",
                                defaultRoleId, defaultRoleName)));

            if (!defaultRoleName.equals(defaultRole.getRoleName())) {
                log.warn("Default role name mismatch: Expected '{}', but found '{}'. Using role ID {} anyway.",
                        defaultRoleName, defaultRole.getRoleName(), defaultRoleId);
            }

            // Check if user already has this role
            if (!userRoleMapRepository.existsByUserIdAndRoleId(userId, defaultRole.getRoleId())) {
                UserRoleMap userRoleMap = new UserRoleMap();
                userRoleMap.setUserId(userId);
                userRoleMap.setRoleId(defaultRole.getRoleId());
                userRoleMap.setIsActive(true);
                userRoleMap.setCreatedAt(LocalDateTime.now());
                userRoleMap.setUpdatedAt(LocalDateTime.now());

                userRoleMapRepository.save(userRoleMap);
                log.info("Assigned default role '{}' (ID: {}) to user with ID: {}",
                        defaultRole.getRoleName(), defaultRole.getRoleId(), userId);
            } else {
                log.info("User with ID {} already has role '{}' (ID: {})",
                        userId, defaultRole.getRoleName(), defaultRole.getRoleId());
            }
        } catch (Exception e) {
            log.error("Error assigning default role to user with ID: {}", userId, e);
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
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role with ID " + roleId + " not found");
        }
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

    private void mappUserRole(Users users,Integer roleId)
    {
        UserRoleMap newRoleUserMap=new UserRoleMap();
        newRoleUserMap.setRoleId(roleId);
        newRoleUserMap.setUserId(users.getId());

    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
