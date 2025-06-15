package com.lms.progressservice.controller;

import com.lms.progressservice.auth.UserContextHolder;
import com.lms.progressservice.dto.CourseProgressResponse;
import com.lms.progressservice.service.ICourseProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ICourseProgressService courseProgressService;

    @PostMapping("/lecture/view/{courseId}/{lectureId}")
    public ResponseEntity<String> updateLectureProgress(
            @PathVariable UUID courseId,
            @PathVariable UUID lectureId
    ) {
        UUID userId = UserContextHolder.getCurrentUserId();
        courseProgressService.updateLectureProgress(courseId, lectureId, userId);
        return ResponseEntity.ok("Lecture progress updated successfully.");
    }

    // at the time of a call use getCourseById & getCourseLectures to fetch other relevant information
    @GetMapping("course/{courseId}")
    public ResponseEntity<CourseProgressResponse> getCourseProgress(@PathVariable UUID courseId) {
        UUID userId = UserContextHolder.getCurrentUserId();
        CourseProgressResponse response = courseProgressService.getCourseProgress(userId, courseId);
        return ResponseEntity.ok(response);
    }
}

