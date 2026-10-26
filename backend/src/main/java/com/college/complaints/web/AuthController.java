package com.college.complaints.web;

import com.college.complaints.domain.Admin;
import com.college.complaints.domain.Student;
import com.college.complaints.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Student register(@Valid @RequestBody Requests.Register request, HttpSession session) {
        Student student = authService.register(request);
        session.setAttribute("user",
                new SessionUser(student.getStudentId(), student.getName(), student.getEmail(), "STUDENT"));
        return student;
    }

    @PostMapping("/login")
    public Student login(@Valid @RequestBody Requests.Login request, HttpSession session) {
        Student student = authService.login(request.identifier(), request.password());
        session.setAttribute("user",
                new SessionUser(student.getStudentId(), student.getName(), student.getEmail(), "STUDENT"));
        return student;
    }

    @PostMapping("/admin/login")
    public Map<String, Object> adminLogin(@Valid @RequestBody Requests.AdminLogin request,
            HttpSession session) {
        Admin admin = authService.adminLogin(request.username(), request.password());
        SessionUser user = new SessionUser(admin.getUsername(), "Administrator", "", "ADMIN");
        session.setAttribute("user", user);
        return Map.of("id", user.id(), "name", user.name(), "role", user.role());
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in."));
        }
        return ResponseEntity.ok(user);
    }
}
