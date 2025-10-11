-- ============================================
-- LoveDev API - Database Schema (Simplified)
-- Version: 1.0.0
-- No complex functions to avoid Flyway issues
-- ============================================

-- ============================================
-- 1. ROLES TABLE
-- ============================================
CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description TEXT,
                       is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_roles_name ON roles(name);

-- ============================================
-- 2. PERMISSIONS TABLE
-- ============================================
CREATE TABLE permissions (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             name VARCHAR(100) NOT NULL UNIQUE,
                             description TEXT,
                             resource VARCHAR(50) NOT NULL,
                             action VARCHAR(50) NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             UNIQUE(resource, action)
);

CREATE INDEX idx_permissions_name ON permissions(name);
CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_action ON permissions(action);

-- ============================================
-- 3. ROLE_PERMISSIONS JUNCTION TABLE
-- ============================================
CREATE TABLE role_permissions (
                                  role_id UUID NOT NULL,
                                  permission_id UUID NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  PRIMARY KEY (role_id, permission_id),
                                  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- ============================================
-- 4. USERS TABLE
-- ============================================
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       phone_number VARCHAR(20),
                       address VARCHAR(255),
                       date_of_birth DATE,
                       profile_picture_url VARCHAR(500),
                       bio TEXT,
                       status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                       email_verification_token VARCHAR(255),
                       email_verification_expires_at TIMESTAMP,
                       password_reset_token VARCHAR(255),
                       password_reset_expires_at TIMESTAMP,
                       last_login_at TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP
);

ALTER TABLE users ADD CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED'));

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_deleted ON users(deleted_at);
CREATE INDEX idx_user_email_verification ON users(email_verification_token);
CREATE INDEX idx_user_password_reset ON users(password_reset_token);

-- ============================================
-- 5. USER_ROLES JUNCTION TABLE
-- ============================================
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- ============================================
-- 6. REFRESH_TOKENS TABLE
-- ============================================
CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                token VARCHAR(500) NOT NULL UNIQUE,
                                user_id UUID NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                revoked_at TIMESTAMP,
                                CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens(expires_at);

-- ============================================
-- 7. AUDIT_LOGS TABLE
-- ============================================
CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID,
                            action VARCHAR(50) NOT NULL,
                            entity_type VARCHAR(50),
                            entity_id VARCHAR(255),
                            old_value JSONB,
                            new_value JSONB,
                            ip_address VARCHAR(45),
                            user_agent VARCHAR(500),
                            description TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_audit_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- ============================================
-- 8. SEED DEFAULT ROLES
-- ============================================
INSERT INTO roles (name, description, is_system_role) VALUES
                                                          ('ROLE_USER', 'Basic user with standard permissions', true),
                                                          ('ROLE_EMPLOYEE', 'Employee with workspace and team permissions', true),
                                                          ('ROLE_MANAGER', 'Manager with team management and reporting permissions', true),
                                                          ('ROLE_ADMIN', 'Administrator with full system access', true);

-- ============================================
-- 9. SEED PERMISSIONS
-- ============================================

-- Authentication & Session
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('auth:login', 'Login to the system', 'auth', 'login'),
                                                                  ('auth:logout', 'Logout from the system', 'auth', 'logout'),
                                                                  ('auth:refresh', 'Refresh authentication token', 'auth', 'refresh');

-- User Profile Management
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('profile:read', 'View own profile', 'profile', 'read'),
                                                                  ('profile:update', 'Update own profile', 'profile', 'update'),
                                                                  ('profile:delete', 'Delete own account', 'profile', 'delete'),
                                                                  ('profile:avatar:upload', 'Upload profile picture', 'profile', 'avatar:upload'),
                                                                  ('profile:avatar:delete', 'Delete profile picture', 'profile', 'avatar:delete'),
                                                                  ('profile:password:change', 'Change own password', 'profile', 'password:change');

-- User Management (Admin)
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('user:list', 'List all users', 'user', 'list'),
                                                                  ('user:read', 'View any user profile', 'user', 'read'),
                                                                  ('user:create', 'Create new user', 'user', 'create'),
                                                                  ('user:update', 'Update any user', 'user', 'update'),
                                                                  ('user:delete', 'Delete any user', 'user', 'delete'),
                                                                  ('user:restore', 'Restore deleted user', 'user', 'restore'),
                                                                  ('user:search', 'Search users', 'user', 'search'),
                                                                  ('user:export', 'Export user data', 'user', 'export');

-- Role Management
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('role:list', 'List all roles', 'role', 'list'),
                                                                  ('role:read', 'View role details', 'role', 'read'),
                                                                  ('role:create', 'Create new role', 'role', 'create'),
                                                                  ('role:update', 'Update role', 'role', 'update'),
                                                                  ('role:delete', 'Delete role', 'role', 'delete'),
                                                                  ('role:assign', 'Assign role to user', 'role', 'assign'),
                                                                  ('role:revoke', 'Revoke role from user', 'role', 'revoke');

-- Permission Management
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('permission:list', 'List all permissions', 'permission', 'list'),
                                                                  ('permission:read', 'View permission details', 'permission', 'read'),
                                                                  ('permission:create', 'Create new permission', 'permission', 'create'),
                                                                  ('permission:update', 'Update permission', 'permission', 'update'),
                                                                  ('permission:delete', 'Delete permission', 'permission', 'delete'),
                                                                  ('permission:assign', 'Assign permission to role', 'permission', 'assign'),
                                                                  ('permission:revoke', 'Revoke permission from role', 'permission', 'revoke');

-- Audit Logs
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('audit:read:own', 'View own audit logs', 'audit', 'read:own'),
                                                                  ('audit:read:all', 'View all audit logs', 'audit', 'read:all'),
                                                                  ('audit:export', 'Export audit logs', 'audit', 'export');

-- File Management
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('file:upload', 'Upload files', 'file', 'upload'),
                                                                  ('file:download', 'Download files', 'file', 'download'),
                                                                  ('file:delete:own', 'Delete own files', 'file', 'delete:own'),
                                                                  ('file:delete:all', 'Delete any files', 'file', 'delete:all'),
                                                                  ('file:list:own', 'List own files', 'file', 'list:own'),
                                                                  ('file:list:all', 'List all files', 'file', 'list:all');

-- Dashboard & Analytics
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('dashboard:view', 'View dashboard', 'dashboard', 'view'),
                                                                  ('analytics:view', 'View analytics', 'analytics', 'view'),
                                                                  ('analytics:export', 'Export analytics data', 'analytics', 'export');

-- Reports
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('report:view', 'View reports', 'report', 'view'),
                                                                  ('report:create', 'Create reports', 'report', 'create'),
                                                                  ('report:update', 'Update reports', 'report', 'update'),
                                                                  ('report:delete', 'Delete reports', 'report', 'delete'),
                                                                  ('report:export', 'Export reports', 'report', 'export'),
                                                                  ('report:schedule', 'Schedule report generation', 'report', 'schedule');

-- Settings
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('settings:view', 'View system settings', 'settings', 'view'),
                                                                  ('settings:update', 'Update system settings', 'settings', 'update');

-- Notifications
INSERT INTO permissions (name, description, resource, action) VALUES
                                                                  ('notification:read:own', 'Read own notifications', 'notification', 'read:own'),
                                                                  ('notification:send', 'Send notifications', 'notification', 'send'),
                                                                  ('notification:broadcast', 'Broadcast notifications', 'notification', 'broadcast');

-- ============================================
-- 10. ASSIGN PERMISSIONS TO ROLES
-- ============================================

-- ROLE_USER: Basic user permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_USER' AND p.name IN (
                                          'auth:login', 'auth:logout', 'auth:refresh',
                                          'profile:read', 'profile:update', 'profile:avatar:upload',
                                          'profile:avatar:delete', 'profile:password:change',
                                          'audit:read:own',
                                          'file:upload', 'file:download', 'file:delete:own', 'file:list:own',
                                          'dashboard:view',
                                          'notification:read:own'
    );

-- ROLE_EMPLOYEE: User permissions + employee features
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_EMPLOYEE' AND p.name IN (
                                              'auth:login', 'auth:logout', 'auth:refresh',
                                              'profile:read', 'profile:update', 'profile:avatar:upload',
                                              'profile:avatar:delete', 'profile:password:change',
                                              'audit:read:own',
                                              'file:upload', 'file:download', 'file:delete:own', 'file:list:own',
                                              'dashboard:view',
                                              'notification:read:own',
                                              'user:search',
                                              'analytics:view',
                                              'report:view', 'report:create', 'report:export'
    );

-- ROLE_MANAGER: Employee permissions + management features
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_MANAGER' AND p.name IN (
                                             'auth:login', 'auth:logout', 'auth:refresh',
                                             'profile:read', 'profile:update', 'profile:avatar:upload',
                                             'profile:avatar:delete', 'profile:password:change',
                                             'audit:read:own', 'audit:read:all',
                                             'file:upload', 'file:download', 'file:delete:own',
                                             'file:list:own', 'file:list:all',
                                             'dashboard:view',
                                             'notification:read:own', 'notification:send',
                                             'user:search', 'user:list', 'user:read', 'user:update',
                                             'analytics:view', 'analytics:export',
                                             'report:view', 'report:create', 'report:update',
                                             'report:delete', 'report:export', 'report:schedule',
                                             'role:list', 'role:read'
    );

-- ROLE_ADMIN: Full system access (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN';

-- ============================================
-- 11. INSERT DEFAULT ADMIN USER
-- ============================================
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    status,
    email_verified
) VALUES (
             'admin@lovedev.com',
             '$2a$10$O6Aipmy8tD2H94nc7PPfV.2lJyxWWZvvSig3HdzuvJ51SXkgjZSWm',
             'Admin',
             'User',
             'ACTIVE',
             true
         );

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@lovedev.com'
  AND r.name = 'ROLE_ADMIN';

-- ============================================
-- MIGRATION COMPLETE
-- ============================================