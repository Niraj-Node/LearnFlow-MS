package com.lms.courseservice.controller;

import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.request.EditCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;
import com.lms.courseservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseResponse> editCourse(
            @PathVariable String courseId,
            @Valid @ModelAttribute EditCourseRequest request) {
        CourseResponse response = courseService.editCourse(courseId, request);
        return ResponseEntity.ok(response);
    }
}
