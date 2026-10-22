package com.college.complaints.service;

import com.college.complaints.domain.Admin;
import com.college.complaints.domain.Student;
import com.college.complaints.repository.AdminRepository;
import com.college.complaints.repository.StudentRepository;
import com.college.complaints.util.PasswordUtil;
import com.college.complaints.web.ApiException;
import com.college.complaints.web.Requests;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final StudentRepository students;
    private final AdminRepository admins;

    public AuthService(StudentRepository students, AdminRepository admins) {
        this.students = students;
        this.admins = admins;
    }

    @Transactional
    public Student register(Requests.Register request) {
        String studentId = request.studentId().trim().toUpperCase(Locale.ROOT);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (students.existsByStudentId(studentId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Student ID is already registered.");
        }
        if (students.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered.");
        }
        Student student = new Student();
        student.setStudentId(studentId);
        student.setName(request.name().trim());
        student.setEmail(email);
        student.setPasswordHash(PasswordUtil.hash(request.password()));
        return students.save(student);
    }

    @Transactional(readOnly = true)
    public Student login(String identifier, String password) {
        String value = identifier.trim();
        Student student = value.contains("@")
                ? students.findByEmailIgnoreCase(value).orElse(null)
                : students.findById(value.toUpperCase(Locale.ROOT)).orElse(null);
        if (student == null || !PasswordUtil.matches(password, student.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid ID/email or password.");
        }
        return student;
    }

    @Transactional(readOnly = true)
    public Admin adminLogin(String username, String password) {
        Admin admin = admins.findById(username.trim()).orElse(null);
        if (admin == null || !PasswordUtil.matches(password, admin.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }
        return admin;
    }
}
