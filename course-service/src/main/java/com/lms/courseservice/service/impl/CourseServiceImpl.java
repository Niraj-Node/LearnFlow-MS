package com.lms.courseservice.service.impl;

import com.lms.courseservice.auth.UserContextHolder;
import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;
import com.lms.courseservice.mapper.CourseMapper;
import com.lms.courseservice.model.Course;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.service.CourseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = new Course();
        course.setCourseTitle(request.getCourseTitle());
        course.setCategory(request.getCategory());
        course.setCreatorId(UserContextHolder.getCurrentUserId());

        Course saved = courseRepository.save(course);
        return CourseMapper.toCreateCourseResponse(saved);
    }
}
