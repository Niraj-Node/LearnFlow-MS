package com.lms.userservice.kafka;

import cloudinary.events.CloudinaryEvent.UserPhotoUpdated;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public void produceUserPhotoUpdatedEvent(String userId, String oldPhotoUrl, byte[] imageBytes) {
        UserPhotoUpdated photoUpdated = UserPhotoUpdated.newBuilder()
                .setUserId(userId)
                .setOldPhotoUrl(oldPhotoUrl)
                .setImageData(ByteString.copyFrom(imageBytes))
                .build();

        try {
            kafkaTemplate.send("user-photo-updated-topic", photoUpdated.toByteArray());
            log.info("Produced UserPhotoUpdated event for userId={}", userId);
        } catch (Exception e) {
            log.error("Error sending UserPhotoUpdated event: {}", e.getMessage());
        }
    }
}
