package com.college.complaints.repository;

import com.college.complaints.domain.ComplaintUpdate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintUpdateRepository extends JpaRepository<ComplaintUpdate, Integer> {

    List<ComplaintUpdate> findByComplaint_ComplaintIdOrderByCreatedAtAscUpdateIdAsc(
            Integer complaintId);

    boolean existsByComplaint_ComplaintIdAndType(Integer complaintId, String type);
}
