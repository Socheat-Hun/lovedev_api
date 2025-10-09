-- V1__Initial_Schema.sql
-- Initial database schema for LoveDev API

-- ============================================
-- USERS TABLE
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
                       primary_role VARCHAR(20) NOT NULL DEFAULT 'USER',
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

-- Add constraints to users table
ALTER TABLE users ADD CONSTRAINT chk_primary_role CHECK (primary_role IN ('USER', 'EMPLOYEE', 'MANAGER', 'ADMIN'));
ALTER TABLE users ADD CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BANNED'));

-- Create indexes for users table
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_primary_role ON users(primary_role);
CREATE INDEX idx_user_deleted ON users(deleted_at);
CREATE INDEX idx_user_email_verification ON users(email_verification_token);
CREATE INDEX idx_user_password_reset ON users(password_reset_token);

-- ============================================
-- REFRESH_TOKENS TABLE
-- ============================================
CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                token VARCHAR(500) NOT NULL UNIQUE,
                                user_id UUID NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                revoked_at TIMESTAMP
);

-- Add foreign key to refresh_tokens
ALTER TABLE refresh_tokens ADD CONSTRAINT fk_refresh_token_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Create indexes for refresh_tokens
CREATE UNIQUE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens(expires_at);

-- ============================================
-- AUDIT_LOGS TABLE
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
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key to audit_logs
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_log_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- Create indexes for audit_logs
CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_logs(created_at);

-- ============================================
-- USER_ROLES TABLE (Multi-role support)
-- ============================================
CREATE TABLE user_roles (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL,
                            role VARCHAR(20) NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add constraints to user_roles
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_roles ADD CONSTRAINT chk_user_role
    CHECK (role IN ('USER', 'EMPLOYEE', 'MANAGER', 'ADMIN'));
ALTER TABLE user_roles ADD CONSTRAINT uk_user_role
    UNIQUE (user_id, role);

-- Create indexes for user_roles
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);





-- V2__Insert_Admin_User.sql
-- Insert default admin user

-- Insert admin user
-- Email: admin@lovedev.com
-- Password: Admin@123
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    primary_role,
    status,
    email_verified
) VALUES (
             'admin@lovedev.com',
             '$2a$10$O6Aipmy8tD2H94nc7PPfV.2lJyxWWZvvSig3HdzuvJ51SXkgjZSWm',
             'Admin',
             'User',
             'ADMIN',
             'ACTIVE',
             true
         );

-- Add ADMIN role to user_roles table
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE email = 'admin@lovedev.com';