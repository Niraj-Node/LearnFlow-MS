package com.lms.courseservice.controller;

import com.lms.courseservice.auth.UserContextHolder;
import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.request.EditCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;
import com.lms.courseservice.exception.ResourceNotFoundException;
import com.lms.courseservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("creator")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("creator/{courseId}")
    public ResponseEntity<CourseResponse> editCourse(
            @PathVariable String courseId,
            @Valid @ModelAttribute EditCourseRequest request) {
        CourseResponse response = courseService.editCourse(courseId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("public/published")
    public ResponseEntity<?> getPublishedCourses() {
        List<CourseResponse> courses = courseService.getPublishedCourses();
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("No published courses found");
        }
        return ResponseEntity.ok(Map.of("courses", courses));
    }

    @GetMapping("creator")
    public ResponseEntity<?> getCreatorCourses() {
        UUID userId = UserContextHolder.getCurrentUserId();
        List<CourseResponse> courses = courseService.getCoursesByCreator(userId);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("No courses found for the current creator");
        }
        return ResponseEntity.ok(Map.of("courses", courses));
    }
}
