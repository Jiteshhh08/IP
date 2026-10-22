package com.college.complaints.repository;

import com.college.complaints.domain.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByEmailIgnoreCase(String email);
}
