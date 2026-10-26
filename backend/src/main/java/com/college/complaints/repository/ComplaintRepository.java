package com.college.complaints.repository;

import com.college.complaints.domain.Complaint;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Integer> {

    List<Complaint> findByStudent_StudentIdOrderByCreatedAtDesc(String studentId);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    long countByStudent_StudentId(String studentId);
}
