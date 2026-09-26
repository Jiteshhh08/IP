-- =============================================
-- seed.sql - sample data (safe to re-run: INSERT IGNORE)
--
--   mysql -u root -p < database/seed.sql
--
-- Passwords are PBKDF2 hashes (NOT plaintext):
--   admin  / admin123
--   STU001..STU004 / pass123
-- Hashes generated with: java -cp out util.PasswordUtil <password>
-- Re-run that command if you want new hashes for changed passwords.
-- =============================================

USE student_complaint_system;

-- Admin account
INSERT IGNORE INTO admin (username, password_hash) VALUES
('admin', 'pbkdf2:65536:WYroCQAvyFnn1zIr8IGZZA==:TemRVzCrjbRP4AcSa+gHRAwz4UP2OCtS4tz+AzdfFEE=');

-- Students
INSERT IGNORE INTO students (student_id, name, email, password_hash) VALUES
('STU001', 'Rahul', 'rahul@college.edu', 'pbkdf2:65536:N6SpgyhaAvSgBDPoIefzYQ==:qnRIwE6cA42fFU7W54oBZUsmgTUPbwmfYFpvPiOPCZ0='),
('STU002', 'Priya Sharma', 'priya@college.edu', 'pbkdf2:65536:TFkrY+jwnml9ZFQgwOenTQ==:WmCx1ias2LOorugeqOquaYWenzNvnZY8vEe0HXfaiFc='),
('STU003', 'Amit Kumar', 'amit@college.edu', 'pbkdf2:65536:N6SpgyhaAvSgBDPoIefzYQ==:qnRIwE6cA42fFU7W54oBZUsmgTUPbwmfYFpvPiOPCZ0='),
('STU004', 'Sneha Patel', 'sneha@college.edu', 'pbkdf2:65536:TFkrY+jwnml9ZFQgwOenTQ==:WmCx1ias2LOorugeqOquaYWenzNvnZY8vEe0HXfaiFc=');

-- Sample complaints (all 4 statuses for demo)
INSERT IGNORE INTO complaints
(complaint_id, student_id, title, category, description, status, admin_remark)
VALUES
(1, 'STU001', 'Fan not working', 'Infrastructure', 'Fan in classroom 203 is not working.', 'Pending', NULL),
(2, 'STU002', 'Library timing', 'Library', 'Please extend library timing during exams.', 'Resolved', 'Timing has been updated to 9 PM.'),
(3, 'STU001', 'Book not available', 'Library', 'Data Structures book is not available in library.', 'In Progress', 'New books have been ordered.'),
(4, 'STU003', 'Mess food quality', 'Canteen', 'Canteen food quality is very poor since last week.', 'Pending', NULL),
(5, 'STU002', 'WiFi not working in hostel', 'Hostel', 'Hostel Block B WiFi is down for 3 days.', 'Rejected', 'WiFi is managed by outside vendor. Complaint closed.'),
(6, 'STU004', 'Fees receipt not received', 'Academic', 'Paid semester fees but did not get receipt.', 'Pending', NULL);
