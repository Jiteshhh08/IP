package com.college.complaints.config;

import com.college.complaints.domain.Admin;
import com.college.complaints.domain.Complaint;
import com.college.complaints.domain.Student;
import com.college.complaints.repository.AdminRepository;
import com.college.complaints.repository.ComplaintRepository;
import com.college.complaints.repository.StudentRepository;
import com.college.complaints.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final StudentRepository students;
    private final AdminRepository admins;
    private final ComplaintRepository complaints;

    @Value("${app.seed-demo:true}")
    private boolean seedDemo;

    public DemoDataSeeder(StudentRepository students, AdminRepository admins,
            ComplaintRepository complaints) {
        this.students = students;
        this.admins = admins;
        this.complaints = complaints;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDemo) {
            return;
        }
        seedAdmin();
        seedStudents();
        seedComplaints();
    }

    private void seedAdmin() {
        if (admins.count() > 0) {
            return;
        }
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPasswordHash(PasswordUtil.hash("admin123"));
        admins.save(admin);
        log.warn("Seeded demo admin 'admin' / 'admin123'. Change it before deploying.");
    }

    private void seedStudents() {
        if (students.count() > 0) {
            return;
        }
        saveStudent("STU001", "Rahul", "rahul@college.edu");
        saveStudent("STU002", "Priya Sharma", "priya@college.edu");
        saveStudent("STU003", "Amit Kumar", "amit@college.edu");
        saveStudent("STU004", "Sneha Patel", "sneha@college.edu");
        log.info("Seeded demo students STU001-STU004 / 'pass123'.");
    }

    private void saveStudent(String id, String name, String email) {
        Student student = new Student();
        student.setStudentId(id);
        student.setName(name);
        student.setEmail(email);
        student.setPasswordHash(PasswordUtil.hash("pass123"));
        students.save(student);
    }

    private void seedComplaints() {
        if (complaints.count() > 0) {
            return;
        }
        saveComplaint("STU001", "Fan not working", "Infrastructure",
                "Fan in classroom 203 is not working.", "Pending", null);
        saveComplaint("STU002", "Library timing", "Library",
                "Please extend library timing during exams.", "Resolved",
                "Timing has been updated to 9 PM.");
        saveComplaint("STU001", "Book not available", "Library",
                "Data Structures book is not available in library.", "In Progress",
                "New books have been ordered.");
        saveComplaint("STU003", "Mess food quality", "Canteen",
                "Canteen food quality is very poor since last week.", "Pending", null);
        saveComplaint("STU002", "WiFi not working in hostel", "Hostel",
                "Hostel Block B WiFi is down for 3 days.", "Rejected",
                "WiFi is managed by an outside vendor.");
        saveComplaint("STU004", "Fees receipt not received", "Academic",
                "Paid semester fees but did not get receipt.", "Pending", null);
        log.info("Seeded 6 demo complaints.");
    }

    private void saveComplaint(String studentId, String title, String category, String description,
            String status, String adminRemark) {
        Complaint complaint = new Complaint();
        complaint.setStudent(students.findById(studentId).orElseThrow());
        complaint.setTitle(title);
        complaint.setCategory(category);
        complaint.setDescription(description);
        complaint.setStatus(status);
        complaint.setAdminRemark(adminRemark);
        complaints.save(complaint);
    }
}
