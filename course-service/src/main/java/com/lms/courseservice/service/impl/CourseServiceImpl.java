package com.lms.courseservice.service.impl;

import com.lms.courseservice.exception.BadRequestException;
import com.lms.courseservice.exception.ForbiddenException;
import com.lms.courseservice.util.CourseUtil;
import com.lms.courseservice.dto.request.SearchCourseRequest;
import com.lms.courseservice.grpc.GrpcUserClient;
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

import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final KafkaProducer kafkaProducer;
    private final CourseRepository courseRepository;
    private final GrpcUserClient grpcUserClient;

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
            throw new ForbiddenException("You are not allowed to edit this course");
        }

        if (request.getCoursePrice() < 50) {
            throw new BadRequestException("Course price must be at least ₹50 to comply with Stripe minimums.");
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

    @Override
    public List<CourseResponse> getPublishedCourses() {
        List<Course> courses = courseRepository.findByIsPublishedTrue();
        return CourseUtil.enrichWithCreatorInfo(courses, grpcUserClient);
    }

    @Override
    public List<CourseResponse> getCoursesByCreator(UUID creatorId) {
        List<Course> courses = courseRepository.findByCreatorId(creatorId);
        return courses.stream()
                .map(CourseMapper::toSummaryResponse)
                .toList();
    }

    @Override
    public List<CourseResponse> searchCourses(SearchCourseRequest request) {
        List<Course> resultCourses = CourseUtil.searchCoursesWithFilter(request, courseRepository);
        return CourseUtil.enrichWithCreatorInfo(resultCourses, grpcUserClient);
    }

    public String togglePublishCourse(UUID userId, UUID courseId, boolean publish) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getCreatorId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this course");
        }

        if (publish && (course.getLectureIds() == null || course.getLectureIds().isEmpty())) {
            throw new BadRequestException("Cannot publish a course with no lectures");
        }

        course.setIsPublished(publish);
        courseRepository.save(course);

        return publish ? "Published" : "Unpublished";
    }

    @Override
    public List<CourseResponse> getPurchasedCourses(UUID userId) {
        List<Course> purchased = courseRepository.findByEnrolledStudentIdsContaining(userId);
        if (purchased.isEmpty()) {
            throw new ResourceNotFoundException("No purchased courses found");
        }
        return CourseUtil.enrichWithCreatorInfo(purchased, grpcUserClient);
    }

    @Override
    @Transactional
    public void enrollStudent(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getEnrolledStudentIds().contains(userId)) {
            course.getEnrolledStudentIds().add(userId);
            courseRepository.save(course);
        }
    }

    @Override
    @Transactional
    public void addLectureToCourse(UUID courseId, UUID lectureId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.getLectureIds().add(lectureId);
        courseRepository.save(course);
    }

}
