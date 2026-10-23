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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ComplaintService complaintService;

    public AdminController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return complaintService.adminDashboard();
    }

    @GetMapping("/complaints")
    public List<Complaint> complaints(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        return complaintService.all(search, status, category);
    }

    @GetMapping("/complaints/{id}")
    public Complaint complaint(@PathVariable Integer id) {
        return complaintService.get(id);
    }

    @PatchMapping("/complaints/{id}")
    public Complaint update(@PathVariable Integer id,
            @Valid @RequestBody Requests.StatusUpdate request, HttpSession session) {
        return complaintService.updateStatus(id, request.status(), request.remark(),
                user(session).name());
    }

    @GetMapping("/complaints/{id}/timeline")
    public List<TimelineEntry> timeline(@PathVariable Integer id) {
        return complaintService.timelineForAdmin(id);
    }

    @PostMapping("/complaints/{id}/timeline")
    public ResponseEntity<TimelineEntry> comment(@PathVariable Integer id,
            @Valid @RequestBody Requests.Comment request, HttpSession session) {
        TimelineEntry entry = complaintService.addAdminComment(id, request.message(),
                user(session).name());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
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
