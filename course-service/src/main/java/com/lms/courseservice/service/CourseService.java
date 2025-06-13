package com.lms.courseservice.service;

import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;

public interface CourseService {
    CourseResponse createCourse(CreateCourseRequest request);
}
