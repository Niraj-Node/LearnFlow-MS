package com.lms.progressservice.repository;

import com.lms.progressservice.model.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseProgressRepository extends JpaRepository<CourseProgress, UUID> {
    Optional<CourseProgress> findByUserIdAndCourseId(UUID userId, UUID courseId);
    List<CourseProgress> findAllByCourseId(UUID courseId);
    @Query("SELECT DISTINCT cp.courseId FROM CourseProgress cp")
    List<UUID> findDistinctCourseIds();
    Optional<CourseProgress> findFirstByCourseId(UUID courseId);
}
