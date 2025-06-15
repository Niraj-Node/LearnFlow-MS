package com.lms.progressservice.repository;

import com.lms.progressservice.model.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, UUID> {
    Optional<CourseProgress> findByUserIdAndCourseId(UUID userId, UUID courseId);
}
