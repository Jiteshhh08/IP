-- =============================================
-- Student Complaint Management System
-- schema.sql - tables only (no data)
--
--   mysql -u root -p < database/schema.sql
--   mysql -u root -p < database/seed.sql
--
-- The app ALSO auto-creates/seed on startup (config.DbInit),
-- so running these files is optional but recommended.
-- =============================================

CREATE DATABASE IF NOT EXISTS student_complaint_system;
USE student_complaint_system;

-- Students: real auth (ID + password hash). Email unique.
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Admins: username + password hash (never plaintext).
CREATE TABLE IF NOT EXISTS admin (
    username VARCHAR(50) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL
);

-- Complaints: one row per complaint, owned by one student.
CREATE TABLE IF NOT EXISTS complaints (
    complaint_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    title VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    admin_remark TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT chk_status CHECK (status IN ('Pending','In Progress','Resolved','Rejected')),
    INDEX idx_complaint_student (student_id),
    INDEX idx_complaint_status (status)
);
