package com.lms.courseservice.repository;

import com.lms.courseservice.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
    List<Course> findByIsPublishedTrue();
    List<Course> findByCreatorId(UUID creatorId);
    List<Course> findByEnrolledStudentIdsContaining(UUID studentId);
    @Query("SELECT c.lectureIds FROM Course c WHERE c.id = :courseId")
    List<UUID> findLectureIdsByCourseId(@Param("courseId") UUID courseId);

}
