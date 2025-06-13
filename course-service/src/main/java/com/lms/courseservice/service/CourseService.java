package com.lms.courseservice.service;

import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.request.EditCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;

public interface CourseService {
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse editCourse(String courseId, EditCourseRequest request);
}
