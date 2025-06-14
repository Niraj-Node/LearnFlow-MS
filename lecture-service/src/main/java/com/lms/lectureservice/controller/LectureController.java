package com.lms.lectureservice.controller;

import com.lms.lectureservice.auth.UserContextHolder;
import com.lms.lectureservice.dto.LectureRequest;
import com.lms.lectureservice.dto.LectureResponse;
import com.lms.lectureservice.service.LectureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @PostMapping("/{courseId}")
    public ResponseEntity<LectureResponse> createLecture(
            @PathVariable UUID courseId,
            @RequestBody @Valid LectureRequest request
    ) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        LectureResponse response = lectureService.createLecture(currentUserId, courseId, request);
        return ResponseEntity.status(201).body(response);
    }

    @PatchMapping("/{lectureId}")
    public ResponseEntity<LectureResponse> editLecture(
            @PathVariable UUID lectureId,
            @RequestBody LectureRequest request
    ) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        LectureResponse response = lectureService.editLecture(currentUserId, lectureId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{lectureId}")
    public ResponseEntity<String> deleteLecture(@PathVariable UUID lectureId) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        lectureService.deleteLecture(currentUserId, lectureId);
        return ResponseEntity.ok("Lecture removed successfully.");
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<List<LectureResponse>> getLecturesForCourse(@PathVariable UUID courseId) {
        UUID currentUserId = UserContextHolder.getCurrentUserId();
        List<LectureResponse> lectures = lectureService.getLecturesByCourse(currentUserId, courseId);
        return ResponseEntity.ok(lectures);
    }
}
