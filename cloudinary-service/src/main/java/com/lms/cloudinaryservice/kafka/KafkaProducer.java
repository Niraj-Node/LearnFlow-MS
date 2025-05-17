package com.lms.cloudinaryservice.kafka;

import cloudinary.events.UserEventOuterClass.UserEvent;
import com.lms.cloudinaryservice.model.UserEventType;
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

    public void produceUserEvent(String userId, String photoUrl, UserEventType eventType) {
        UserEvent event = UserEvent.newBuilder()
                .setUserId(userId)
                .setEventType(eventType.name())
                .setPhotoUrl(photoUrl)
                .build();

        try {
            kafkaTemplate.send("user", event.toByteArray());
            log.info("Produced USER_PHOTO_UPLOAD_COMPLETED event for userId={}, url={}", userId, photoUrl);
        } catch (Exception e) {
            log.error("Failed to produce USER_PHOTO_UPLOAD_COMPLETED event: {}", e.getMessage());
        }
    }
}
