package com.lms.lectureservice.kafka;

import course.events.CourseEvent.LectureCreated;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendLectureCreatedEvent(UUID courseId, UUID lectureId) {
        LectureCreated event = LectureCreated.newBuilder()
                .setCourseId(courseId.toString())
                .setLectureId(lectureId.toString())
                .build();

        kafkaTemplate.send("lecture-created-topic", event.toByteArray());
        log.info("Published LectureCreated event for courseId={} lectureId={}", courseId, lectureId);
    }
}

