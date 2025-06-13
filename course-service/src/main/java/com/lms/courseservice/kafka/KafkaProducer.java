package com.lms.courseservice.kafka;

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

    public void sendCourseCreatedEvent(String creatorId) {
        CourseCreated event = CourseCreated.newBuilder()
                .setCreatorId(creatorId)
                .build();

        kafkaTemplate.send("course-created-topic", event.toByteArray());
        log.info("Published CourseCreated event for userId={}", creatorId);
    }
}
