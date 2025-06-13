package com.lms.courseservice.repository;

import com.lms.courseservice.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByIsPublishedTrue();
    List<Course> findByCreatorId(UUID creatorId);
}
