package com.lms.courseservice.kafka;

import cloudinary.events.CloudinaryEvent.CourseThumbnailUploaded;
import com.lms.courseservice.exception.ResourceNotFoundException;
import com.lms.courseservice.model.Course;
import com.lms.courseservice.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final CourseRepository courseRepository;

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

}
