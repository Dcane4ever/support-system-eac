package com.cusservice.bsit.repository;

import com.cusservice.bsit.model.Classroom;
import com.cusservice.bsit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByTeacherOrderByCreatedAtDesc(User teacher);
    Optional<Classroom> findByClassCode(String classCode);
    List<Classroom> findByActiveTrue();
}
