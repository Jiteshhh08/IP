package com.college.complaints.web;

import com.college.complaints.domain.Complaint;
import com.college.complaints.service.ComplaintService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final ComplaintService complaintService;

    public StudentController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(HttpSession session) {
        return complaintService.studentDashboard(user(session).id());
    }

    @GetMapping("/complaints")
    public List<Complaint> complaints(HttpSession session) {
        return complaintService.forStudent(user(session).id());
    }

    @PostMapping("/complaints")
    public ResponseEntity<Complaint> create(@Valid @RequestBody Requests.Complaint request,
            HttpSession session) {
        Complaint complaint = complaintService.create(user(session).id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(complaint);
    }

    @GetMapping("/complaints/{id}")
    public Complaint complaint(@PathVariable Integer id, HttpSession session) {
        return complaintService.forStudentDetail(user(session).id(), id);
    }

    @GetMapping("/profile")
    public SessionUser profile(HttpSession session) {
        return user(session);
    }

    private SessionUser user(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please log in.");
        }
        return user;
    }
}
