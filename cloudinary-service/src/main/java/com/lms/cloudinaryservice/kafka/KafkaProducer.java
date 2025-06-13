package com.lms.cloudinaryservice.kafka;

import user.events.UserEvent.UserPhotoUploadCompleted;
import course.events.CourseEvent.CourseThumbnailUploaded;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produceUserPhotoUploadCompletedEvent(String userId, String newPhotoUrl) {
        UserPhotoUploadCompleted event = UserPhotoUploadCompleted.newBuilder()
                .setUserId(userId)
                .setNewPhotoUrl(newPhotoUrl)
                .build();

        try {
            kafkaTemplate.send("user-photo-upload-completed-topic", event.toByteArray());
            log.info("Produced UserPhotoUploadCompleted event for userId={}, url={}", userId, newPhotoUrl);
        } catch (Exception e) {
            log.error("Failed to produce UserPhotoUploadCompleted event: {}", e.getMessage());
        }
    }

    public void produceCourseThumbnailUploadedEvent(String courseId, String newUrl) {
        CourseThumbnailUploaded event = CourseThumbnailUploaded.newBuilder()
                .setCourseId(courseId)
                .setNewThumbnailUrl(newUrl)
                .build();

        kafkaTemplate.send("course-thumbnail-uploaded-topic", event.toByteArray());
        log.info("Produced CourseThumbnailUploaded event for courseId={}, url={}", courseId, newUrl);
    }

}
