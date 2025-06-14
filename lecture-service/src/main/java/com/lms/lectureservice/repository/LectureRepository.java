package com.lms.lectureservice.repository;

import com.lms.lectureservice.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, UUID> {
    Optional<Lecture> findFirstByCourseId(UUID courseId);
    List<Lecture> findByCourseId(UUID courseId);
}
