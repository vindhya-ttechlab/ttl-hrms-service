-- =====================================================
-- Menu Items, Roles, Designations, Departments Schema
-- =====================================================

-- Create Menu Items Table
CREATE TABLE IF NOT EXISTS menu_items (
    menu_id INT AUTO_INCREMENT PRIMARY KEY,
    label VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    path VARCHAR(200) NOT NULL UNIQUE,
    parent_id INT DEFAULT NULL,
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES menu_items(menu_id) ON DELETE CASCADE
);

-- Create Roles Table
CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Designations Table
CREATE TABLE IF NOT EXISTS designations (
    designation_id INT AUTO_INCREMENT PRIMARY KEY,
    designation_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    level INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Departments Table
CREATE TABLE IF NOT EXISTS departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    head_id INT DEFAULT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_dept_head FOREIGN KEY (head_id) REFERENCES Users(id) ON DELETE SET NULL
);

-- Create Menu Role Map Table (Many-to-Many)
CREATE TABLE IF NOT EXISTS menu_role_map (
    map_id INT AUTO_INCREMENT PRIMARY KEY,
    menu_id INT NOT NULL,
    role_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_menu_role_menu FOREIGN KEY (menu_id) REFERENCES menu_items(menu_id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_role_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    UNIQUE KEY unique_menu_role (menu_id, role_id)
);

-- Create User Role Map Table (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_role_map (
    map_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_role (user_id, role_id)
);

-- =====================================================
-- Insert Initial Menu Items (Based on Sidebar.jsx)
-- =====================================================

-- Parent Menu Items
INSERT INTO menu_items (label, icon, path, parent_id, display_order, is_active) VALUES
('Home', 'Home', '/home', NULL, 1, TRUE),
('Attendance', 'CalendarMonth', '/attendance', NULL, 2, TRUE),
('Approvals', 'Approval', '/approvals', NULL, 3, TRUE),
('Payroll & Finance', 'AccountBalance', '/finance', NULL, 4, TRUE),
('User Management', 'People', '/user-management', NULL, 5, TRUE),
('Apprisals', 'StarRate', '/apprisals', NULL, 6, TRUE);

-- Child Menu Items for Payroll & Finance
INSERT INTO menu_items (label, icon, path, parent_id, display_order, is_active) VALUES
('Payroll Management', 'ReceiptLong', '/finance/payroll', 
 (SELECT menu_id FROM menu_items WHERE path = '/finance'), 1, TRUE),
('Reimbursements', 'AttachMoney', '/finance/reimbursements', 
 (SELECT menu_id FROM menu_items WHERE path = '/finance'), 2, TRUE),
('Payslips', 'Description', '/finance/payslips', 
 (SELECT menu_id FROM menu_items WHERE path = '/finance'), 3, TRUE);

-- Child Menu Items for User Management
INSERT INTO menu_items (label, icon, path, parent_id, display_order, is_active) VALUES
('Employee Profiles', 'Badge', '/user-management/employees', 
 (SELECT menu_id FROM menu_items WHERE path = '/user-management'), 1, TRUE),
('Onboarding / Offboarding', 'Login', '/user-management/onboarding', 
 (SELECT menu_id FROM menu_items WHERE path = '/user-management'), 2, TRUE),
('Role & Permissions', 'Security', '/user-management/roles', 
 (SELECT menu_id FROM menu_items WHERE path = '/user-management'), 3, TRUE);

-- =====================================================
-- Insert Initial Roles
-- =====================================================

INSERT INTO roles (role_name, description, is_active) VALUES
('Employee', 'Regular employee with basic access', TRUE),
('HR', 'Human Resources with employee management access', TRUE),
('Manager', 'Manager with team and approval access', TRUE),
('Admin', 'Administrator with full system access', TRUE);

-- =====================================================
-- Insert Initial Designations
-- =====================================================

INSERT INTO designations (designation_name, description, level, is_active) VALUES
('Software Engineer', 'Entry level software developer', 1, TRUE),
('Senior Software Engineer', 'Experienced software developer', 2, TRUE),
('Tech Lead', 'Technical leadership role', 3, TRUE),
('Engineering Manager', 'Manages engineering team', 4, TRUE),
('HR Executive', 'Human resources executive', 2, TRUE),
('HR Manager', 'Manages HR operations', 3, TRUE),
('Finance Executive', 'Finance operations executive', 2, TRUE),
('Finance Manager', 'Manages finance operations', 3, TRUE),
('Project Manager', 'Manages projects and teams', 3, TRUE),
('Director', 'Senior leadership role', 5, TRUE);

-- =====================================================
-- Insert Initial Departments
-- =====================================================

INSERT INTO departments (department_name, description, is_active) VALUES
('Engineering', 'Software development and engineering', TRUE),
('Human Resources', 'HR operations and employee management', TRUE),
('Finance', 'Finance and accounting operations', TRUE),
('Operations', 'Business operations and administration', TRUE),
('Sales', 'Sales and business development', TRUE),
('Marketing', 'Marketing and communications', TRUE);

-- =====================================================
-- Insert Menu Role Mapping (Default Access Control)
-- =====================================================

-- Employee Role - Basic Access
INSERT INTO menu_role_map (menu_id, role_id) VALUES
((SELECT menu_id FROM menu_items WHERE path = '/home'), (SELECT role_id FROM roles WHERE role_name = 'Employee')),
((SELECT menu_id FROM menu_items WHERE path = '/attendance'), (SELECT role_id FROM roles WHERE role_name = 'Employee')),
((SELECT menu_id FROM menu_items WHERE path = '/finance/payslips'), (SELECT role_id FROM roles WHERE role_name = 'Employee')),
((SELECT menu_id FROM menu_items WHERE path = '/apprisals'), (SELECT role_id FROM roles WHERE role_name = 'Employee'));

-- HR Role - HR and Employee Management Access
INSERT INTO menu_role_map (menu_id, role_id) VALUES
((SELECT menu_id FROM menu_items WHERE path = '/home'), (SELECT role_id FROM roles WHERE role_name = 'HR')),
((SELECT menu_id FROM menu_items WHERE path = '/attendance'), (SELECT role_id FROM roles WHERE role_name = 'HR')),
((SELECT menu_id FROM menu_items WHERE path = '/approvals'), (SELECT role_id FROM roles WHERE role_name = 'HR')),
((SELECT menu_id FROM menu_items WHERE path = '/user-management'), (SELECT role_id FROM roles WHERE role_name = 'HR')),
((SELECT menu_id FROM menu_items WHERE path = '/user-management/employees'), (SELECT role_id FROM roles WHERE role_name = 'HR')),
((SELECT menu_id FROM menu_items WHERE path = '/user-management/onboarding'), (SELECT role_id FROM roles WHERE role_name = 'HR')),
((SELECT menu_id FROM menu_items WHERE path = '/user-management/roles'), (SELECT role_id FROM roles WHERE role_name = 'HR')),
((SELECT menu_id FROM menu_items WHERE path = '/finance/payslips'), (SELECT role_id FROM roles WHERE role_name = 'HR'));

-- Manager Role - Team Management and Approvals
INSERT INTO menu_role_map (menu_id, role_id) VALUES
((SELECT menu_id FROM menu_items WHERE path = '/home'), (SELECT role_id FROM roles WHERE role_name = 'Manager')),
((SELECT menu_id FROM menu_items WHERE path = '/attendance'), (SELECT role_id FROM roles WHERE role_name = 'Manager')),
((SELECT menu_id FROM menu_items WHERE path = '/approvals'), (SELECT role_id FROM roles WHERE role_name = 'Manager')),
((SELECT menu_id FROM menu_items WHERE path = '/user-management/employees'), (SELECT role_id FROM roles WHERE role_name = 'Manager')),
((SELECT menu_id FROM menu_items WHERE path = '/apprisals'), (SELECT role_id FROM roles WHERE role_name = 'Manager'));

-- Admin Role - Full Access to All Menus
INSERT INTO menu_role_map (menu_id, role_id) 
SELECT menu_id, (SELECT role_id FROM roles WHERE role_name = 'Admin')
FROM menu_items
WHERE is_active = TRUE;

