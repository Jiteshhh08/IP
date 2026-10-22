package com.college.complaints.service;

import com.college.complaints.domain.Complaint;
import com.college.complaints.domain.Student;
import com.college.complaints.repository.ComplaintRepository;
import com.college.complaints.repository.StudentRepository;
import com.college.complaints.web.ApiException;
import com.college.complaints.web.Requests;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComplaintService {

    private static final List<String> STATUSES =
            List.of("Pending", "In Progress", "Resolved", "Rejected");
    private static final Set<String> CATEGORIES =
            Set.of("Academic", "Infrastructure", "Hostel", "Library", "Canteen", "Other");
    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "Pending", Set.of("In Progress", "Rejected"),
            "In Progress", Set.of("Resolved", "Rejected"),
            "Resolved", Set.of(),
            "Rejected", Set.of());

    private final ComplaintRepository complaints;
    private final StudentRepository students;

    public ComplaintService(ComplaintRepository complaints, StudentRepository students) {
        this.complaints = complaints;
        this.students = students;
    }

    @Transactional
    public Complaint create(String studentId, Requests.Complaint request) {
        String category = canonicalCategory(request.category());
        if (category == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid category.");
        }
        Student student = students.findById(studentId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Student not found."));
        Complaint complaint = new Complaint();
        complaint.setStudent(student);
        complaint.setTitle(request.title().trim());
        complaint.setCategory(category);
        complaint.setDescription(request.description().trim());
        complaint.setStatus("Pending");
        return complaints.save(complaint);
    }

    @Transactional(readOnly = true)
    public List<Complaint> forStudent(String studentId) {
        return complaints.findByStudent_StudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public Complaint forStudentDetail(String studentId, Integer complaintId) {
        Complaint complaint = get(complaintId);
        if (!complaint.getStudent().getStudentId().equals(studentId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This complaint does not belong to you.");
        }
        return complaint;
    }

    @Transactional(readOnly = true)
    public List<Complaint> all(String search, String status, String category) {
        return complaints.findAllByOrderByCreatedAtDesc().stream()
                .filter(complaint -> matches(complaint, search, status, category))
                .toList();
    }

    @Transactional(readOnly = true)
    public Complaint get(Integer complaintId) {
        return complaints.findById(complaintId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found."));
    }

    @Transactional
    public Complaint updateStatus(Integer complaintId, String requestedStatus, String remark) {
        Complaint complaint = get(complaintId);
        String status = canonicalStatus(requestedStatus);
        if (status == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status.");
        }
        if (!status.equals(complaint.getStatus())
                && !TRANSITIONS.getOrDefault(complaint.getStatus(), Set.of()).contains(status)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot change status from " + complaint.getStatus() + " to " + status + ".");
        }
        complaint.setStatus(status);
        if (remark != null) {
            complaint.setAdminRemark(remark.trim());
        }
        return complaints.save(complaint);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> studentDashboard(String studentId) {
        List<Complaint> owns = forStudent(studentId);
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("stats", stats(owns));
        dashboard.put("recent", owns.stream().limit(5).toList());
        return dashboard;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> adminDashboard() {
        List<Complaint> all = complaints.findAllByOrderByCreatedAtDesc();
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("stats", stats(all));
        dashboard.put("recent", all.stream().limit(5).toList());
        return dashboard;
    }

    private boolean matches(Complaint complaint, String search, String status, String category) {
        if (status != null && !status.isBlank()
                && !status.trim().equalsIgnoreCase(complaint.getStatus())) {
            return false;
        }
        if (category != null && !category.isBlank()
                && !category.trim().equalsIgnoreCase(complaint.getCategory())) {
            return false;
        }
        if (search != null && !search.isBlank()) {
            String query = search.trim().toLowerCase();
            return String.valueOf(complaint.getComplaintId()).contains(query)
                    || complaint.getStudent().getStudentId().toLowerCase().contains(query)
                    || complaint.getStudent().getName().toLowerCase().contains(query)
                    || complaint.getTitle().toLowerCase().contains(query);
        }
        return true;
    }

    private Map<String, Long> stats(List<Complaint> list) {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", (long) list.size());
        stats.put("pending", count(list, "Pending"));
        stats.put("inProgress", count(list, "In Progress"));
        stats.put("resolved", count(list, "Resolved"));
        stats.put("rejected", count(list, "Rejected"));
        return stats;
    }

    private long count(List<Complaint> list, String status) {
        return list.stream().filter(complaint -> status.equals(complaint.getStatus())).count();
    }

    private String canonicalStatus(String value) {
        if (value == null) {
            return null;
        }
        for (String status : STATUSES) {
            if (status.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }

    private String canonicalCategory(String value) {
        if (value == null) {
            return null;
        }
        for (String category : CATEGORIES) {
            if (category.equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        return null;
    }
}
