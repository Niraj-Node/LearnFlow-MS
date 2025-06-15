package com.lms.courseservice.kafka;

import cloudinary.events.CloudinaryEvent.CourseEdited;
import com.google.protobuf.ByteString;
import user.events.UserEvent.CourseCreated;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    public void sendCourseCreatedEvent(String courseId, String creatorId) {
        CourseCreated event = CourseCreated.newBuilder()
                .setCourseId(courseId)
                .setCreatorId(creatorId)
                .build();

        kafkaTemplate.send("course-created-topic", event.toByteArray());
        log.info("Published CourseCreated event for userId={}", creatorId);
    }

    public void sendCourseEditedEvent(String courseId, String oldThumbnailUrl, byte[] newImageBytes) {
        CourseEdited event = CourseEdited.newBuilder()
                .setCourseId(courseId)
                .setOldThumbnailUrl(oldThumbnailUrl == null ? "" : oldThumbnailUrl)
                .setNewImageData(ByteString.copyFrom(newImageBytes))
                .build();

        kafkaTemplate.send("course-edited-topic", event.toByteArray());
        log.info("Published CourseEdited event for courseId={}", courseId);
    }
}
