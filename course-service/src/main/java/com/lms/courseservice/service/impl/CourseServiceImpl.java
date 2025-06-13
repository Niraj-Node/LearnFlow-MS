package com.lms.courseservice.service.impl;

import com.lms.courseservice.kafka.KafkaProducer;
import com.lms.courseservice.auth.UserContextHolder;
import com.lms.courseservice.dto.request.CreateCourseRequest;
import com.lms.courseservice.dto.request.EditCourseRequest;
import com.lms.courseservice.dto.response.CourseResponse;
import com.lms.courseservice.enums.CourseLevel;
import com.lms.courseservice.exception.ResourceNotFoundException;
import com.lms.courseservice.mapper.CourseMapper;
import com.lms.courseservice.model.Course;
import com.lms.courseservice.enums.Role;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.service.CourseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final KafkaProducer kafkaProducer;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        Course course = new Course();
        course.setCourseTitle(request.getCourseTitle());
        course.setCategory(request.getCategory());
        course.setCreatorId(UserContextHolder.getCurrentUserId());

        Course saved = courseRepository.save(course);

        // produce event if role is STUDENT (not already INSTRUCTOR/ADMIN)
        if (UserContextHolder.getCurrentUserRole() == Role.STUDENT) {
            kafkaProducer.sendCourseCreatedEvent(saved.getCreatorId().toString());
        }

        return CourseMapper.toCreateCourseResponse(saved);
    }

    @Override
    public CourseResponse editCourse(String courseId, EditCourseRequest request) {
        Course course = courseRepository.findById(UUID.fromString(courseId))
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        UUID userId = UserContextHolder.getCurrentUserId();

        if (!course.getCreatorId().equals(userId)) {
            throw new RuntimeException("You are not allowed to edit this course");
        }

        MultipartFile newThumbnail = request.getCourseThumbnail();
        if (newThumbnail != null && !newThumbnail.isEmpty()) {
            try {
                kafkaProducer.sendCourseEditedEvent(
                        courseId,
                        course.getCourseThumbnail(), // old URL
                        newThumbnail.getBytes()      // new image bytes
                );
            } catch (Exception e) {
                throw new RuntimeException("Failed to process photo", e);
            }
        }

        course.setCourseTitle(request.getCourseTitle());
        course.setSubTitle(request.getSubTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setCourseLevel(CourseLevel.valueOf(request.getCourseLevel()));
        course.setCoursePrice(request.getCoursePrice());

        courseRepository.save(course);

        return CourseMapper.toSummaryResponse(course);
    }
}
