package com.lms.courseservice.kafka;

import lecture.events.LectureEvent.LectureCreated;
import lecture.events.LectureEvent.LectureDeleted;
import cloudinary.events.CloudinaryEvent.CourseThumbnailUploaded;
import com.lms.courseservice.exception.ResourceNotFoundException;
import com.lms.courseservice.model.Course;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.courseservice.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import payment.events.CoursePurchaseEvent.CoursePurchaseCompleted;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final CourseRepository courseRepository;
    private final CourseService courseService;

    @KafkaListener(topics = "course-thumbnail-uploaded-topic", groupId = "course-service")
    public void consumeCourseThumbnailUploadedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            CourseThumbnailUploaded event = CourseThumbnailUploaded.parseFrom(record.value());

            Course course = courseRepository.findById(UUID.fromString(event.getCourseId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

            course.setCourseThumbnail(event.getNewThumbnailUrl());
            courseRepository.save(course);

            log.info("Updated course thumbnail for courseId={}", event.getCourseId());
        } catch (Exception e) {
            log.error("Failed to handle CourseThumbnailUploaded event", e);
        }
    }

    @KafkaListener(topics = "course-purchase-completed-topic", groupId = "course-service")
    public void handleCoursePurchaseCompleted(ConsumerRecord<String, byte[]> record) {
        try {
            CoursePurchaseCompleted event =
                    CoursePurchaseCompleted.parseFrom(record.value());

            UUID courseId = UUID.fromString(event.getCourseId());
            UUID userId = UUID.fromString(event.getUserId());

            courseService.enrollStudent(courseId, userId);
            log.info("Course [{}] enrolled student [{}]", courseId, userId);

        } catch (Exception e) {
            log.error("Failed to process CoursePurchaseCompleted event in course-service", e);
        }
    }

    @KafkaListener(topics = "lecture-created-topic", groupId = "course-service")
    public void handleLectureCreated(ConsumerRecord<String, byte[]> record) {
        try {
            LectureCreated event = LectureCreated.parseFrom(record.value());
            UUID courseId = UUID.fromString(event.getCourseId());
            UUID lectureId = UUID.fromString(event.getLectureId());
            courseService.addLectureToCourse(courseId, lectureId);
            log.info("Lecture [{}] added to course [{}]", lectureId, courseId);
        } catch (Exception e) {
            log.error("Failed to process LectureCreated event", e);
        }
    }

    @KafkaListener(topics = "lecture-deleted-topic", groupId = "course-service")
    public void handleLectureDeleted(ConsumerRecord<String, byte[]> record) {
        try {
            LectureDeleted event = LectureDeleted.parseFrom(record.value());

            UUID courseId = UUID.fromString(event.getCourseId());
            UUID lectureId = UUID.fromString(event.getLectureId());

            courseService.removeLectureFromCourse(courseId, lectureId);
            log.info("Lecture [{}] removed from course [{}]", lectureId, courseId);

        } catch (Exception e) {
            log.error("Failed to process LectureDeleted event", e);
        }
    }
}
