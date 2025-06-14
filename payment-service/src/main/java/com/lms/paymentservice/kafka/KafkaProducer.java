package com.lms.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import payment.events.CoursePurchaseEvent.CoursePurchaseCompleted;

@Component
@RequiredArgsConstructor
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void sendCoursePurchaseCompletedEvent(String courseId, String userId) {
        CoursePurchaseCompleted event = CoursePurchaseCompleted.newBuilder()
                .setCourseId(courseId)
                .setUserId(userId)
                .build();

        kafkaTemplate.send("course-purchase-completed-topic", event.toByteArray());
        log.info("Published CoursePurchaseCompleted event for courseId={} userId={}", courseId, userId);
    }
}
