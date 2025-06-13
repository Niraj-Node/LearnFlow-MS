package com.lms.courseservice.controller;

import com.lms.courseservice.auth.UserContextHolder;
import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.request.EditCourseRequest;
import com.lms.courseservice.dto.request.SearchCourseRequest;
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

    @GetMapping("public/search")
    public ResponseEntity<?> searchCourses(@Valid SearchCourseRequest request) {
        List<CourseResponse> courses = courseService.searchCourses(request);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("No matching courses found");
        }
        return ResponseEntity.ok(Map.of("courses", courses));
    }

    @PatchMapping("creator/togglepublish/{courseId}")
    public ResponseEntity<?> togglePublishCourse(
            @PathVariable UUID courseId,
            @RequestParam boolean publish
    ) {
        UUID userId = UserContextHolder.getCurrentUserId();
        String statusMessage = courseService.togglePublishCourse(userId, courseId, publish);
        return ResponseEntity.ok(Map.of("message", "Course is " + statusMessage));
    }

    @GetMapping("creator/purchased")
    public ResponseEntity<?> getPurchasedCourses() {
        UUID userId = UserContextHolder.getCurrentUserId();
        List<CourseResponse> purchasedCourses = courseService.getPurchasedCourses(userId);
        return ResponseEntity.ok(Map.of("courses", purchasedCourses));
    }

}
