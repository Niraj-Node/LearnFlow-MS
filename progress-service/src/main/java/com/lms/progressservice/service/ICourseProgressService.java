package com.lms.progressservice.service;

import com.lms.progressservice.dto.CourseProgressResponse;

import java.util.UUID;

public interface ICourseProgressService {
    void updateLectureProgress(UUID courseId, UUID lectureId, UUID userId);
    CourseProgressResponse getCourseProgress(UUID userId, UUID courseId);
    void markCourseAsCompleted(UUID userId, UUID courseId);
    void markCourseAsIncomplete(UUID userId, UUID courseId);
}
