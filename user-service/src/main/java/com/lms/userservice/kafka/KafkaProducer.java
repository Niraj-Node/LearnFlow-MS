package com.lms.userservice.kafka;

import cloudinary.events.UserEventOuterClass.UserEvent;
import com.lms.userservice.enums.UserEventType;
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

    public void produceUserEvent(String userId, String oldPhotoUrl, String newPhotoBase64, UserEventType eventType) {
        UserEvent event = UserEvent.newBuilder()
                .setUserId(userId)
                .setOldPhotoUrl(oldPhotoUrl)
                .setNewPhotoBase64(newPhotoBase64)
                .setEventType(eventType.name())
                .build();

        try {
            kafkaTemplate.send("user", event.toByteArray());
            log.info("Produced USER_PHOTO_UPDATED event for userId={}", userId);
        } catch (Exception e) {
            log.error("Error sending photo update event: {}", e.getMessage());
        }
    }
}
