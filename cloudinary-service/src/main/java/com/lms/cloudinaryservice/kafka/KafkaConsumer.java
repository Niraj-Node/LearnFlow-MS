package com.lms.cloudinaryservice.kafka;

import course.events.CourseEvent.CourseEdited;
import com.lms.cloudinaryservice.service.CourseCloudinaryService;
import user.events.UserEvent.UserPhotoUpdated;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lms.cloudinaryservice.service.UserCloudinaryService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final UserCloudinaryService userCloudinaryService;
    private final CourseCloudinaryService courseCloudinaryService;
    private final KafkaProducer kafkaProducer;

    @KafkaListener(topics = "user-photo-updated-topic", groupId = "cloudinary-service")
    public void consumeUserPhotoUpdatedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            UserPhotoUpdated event = UserPhotoUpdated.parseFrom(record.value());

            String newUrl = userCloudinaryService.handleUserPhotoUpdate(
                    event.getUserId(),
                    event.getOldPhotoUrl(),
                    event.getImageData().toByteArray()
            );

            kafkaProducer.produceUserPhotoUploadCompletedEvent(event.getUserId(), newUrl);
            log.info("Handled UserPhotoUpdated and uploaded Photo for courseId={}", event.getUserId());

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Error parsing event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "course-edited-topic", groupId = "cloudinary-service")
    public void consumeCourseEditedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            CourseEdited event = CourseEdited.parseFrom(record.value());

            String newUrl = courseCloudinaryService.handleCourseThumbnailUpdate(
                    event.getOldThumbnailUrl(),
                    event.getNewImageData().toByteArray()
            );

            kafkaProducer.produceCourseThumbnailUploadedEvent(event.getCourseId(), newUrl);
            log.info("Handled CourseEdited and uploaded thumbnail for courseId={}", event.getCourseId());

        } catch (Exception e) {
            log.error("Failed to handle CourseEdited event", e);
        }
    }
}

