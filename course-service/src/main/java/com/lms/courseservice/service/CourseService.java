package com.lms.courseservice.service;

import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.request.EditCourseRequest;
import com.lms.courseservice.dto.request.SearchCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;
import com.lms.courseservice.model.Course;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse editCourse(String courseId, EditCourseRequest request);
    List<CourseResponse> getPublishedCourses();
    List<CourseResponse> getCoursesByCreator(UUID creatorId);
    List<CourseResponse> searchCourses(SearchCourseRequest request);
    String togglePublishCourse(UUID userId, UUID courseId, boolean publish);
    }
