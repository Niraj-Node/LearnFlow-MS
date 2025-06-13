package com.lms.courseservice.repository;

import com.lms.courseservice.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
    List<Course> findByIsPublishedTrue();
    List<Course> findByCreatorId(UUID creatorId);
}
