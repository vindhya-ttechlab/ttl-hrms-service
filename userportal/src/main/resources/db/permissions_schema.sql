-- =====================================================
-- Permissions and Role-Permission Mapping Schema
-- =====================================================

-- Create Permissions Table
CREATE TABLE IF NOT EXISTS permissions (
    permission_id INT AUTO_INCREMENT PRIMARY KEY,
    permission_name VARCHAR(50) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_permission_resource (permission_name, resource)
);

-- Create Role Permission Map Table (Many-to-Many)
CREATE TABLE IF NOT EXISTS role_permission_map (
    map_id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_role_permission (role_id, permission_id),
    CONSTRAINT fk_rpm_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_rpm_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE
);

-- Insert default permissions for LEAVE_TYPE resource
INSERT INTO permissions (permission_name, resource, description, is_active) VALUES
('VIEW', 'LEAVE_TYPE', 'View leave types', TRUE),
('READ', 'LEAVE_TYPE', 'Read leave type details', TRUE),
('WRITE', 'LEAVE_TYPE', 'Create, update, and delete leave types', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insert default permissions for LEAVE resource
INSERT INTO permissions (permission_name, resource, description, is_active) VALUES
('VIEW', 'LEAVE', 'View leave requests', TRUE),
('READ', 'LEAVE', 'Read leave request details', TRUE),
('WRITE', 'LEAVE', 'Create, update, and delete leave requests', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insert default permissions for EMPLOYEE resource
INSERT INTO permissions (permission_name, resource, description, is_active) VALUES
('VIEW', 'EMPLOYEE', 'View employee information', TRUE),
('READ', 'EMPLOYEE', 'Read employee details', TRUE),
('WRITE', 'EMPLOYEE', 'Create, update, and delete employee information', TRUE)
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Example: Assign all permissions to Admin role (assuming role_id = 1 is Admin)
-- You may need to adjust role_id based on your actual role setup
INSERT INTO role_permission_map (role_id, permission_id, is_active)
SELECT 1, permission_id, TRUE
FROM permissions
WHERE resource IN ('LEAVE_TYPE', 'LEAVE', 'EMPLOYEE')
ON DUPLICATE KEY UPDATE is_active = TRUE;

-- Example: Assign VIEW and READ permissions to Employee role (assuming role_id = 2 is Employee)
INSERT INTO role_permission_map (role_id, permission_id, is_active)
SELECT 2, permission_id, TRUE
FROM permissions
WHERE resource IN ('LEAVE_TYPE', 'LEAVE', 'EMPLOYEE')
AND permission_name IN ('VIEW', 'READ')
ON DUPLICATE KEY UPDATE is_active = TRUE;

-- Example: Assign VIEW and READ permissions to Manager role (assuming role_id = 3 is Manager)
INSERT INTO role_permission_map (role_id, permission_id, is_active)
SELECT 3, permission_id, TRUE
FROM permissions
WHERE resource IN ('LEAVE_TYPE', 'LEAVE', 'EMPLOYEE')
AND permission_name IN ('VIEW', 'READ', 'WRITE')
ON DUPLICATE KEY UPDATE is_active = TRUE;

