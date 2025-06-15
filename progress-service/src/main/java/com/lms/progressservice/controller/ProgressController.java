package com.lms.progressservice.controller;

import com.lms.progressservice.auth.UserContextHolder;
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
}

