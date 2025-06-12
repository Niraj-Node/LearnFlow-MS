package com.lms.cloudinaryservice.kafka;

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

    @KafkaListener(topics = "user-photo-updated-topic", groupId = "cloudinary-service")
    public void consumeUserPhotoUpdatedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            UserPhotoUpdated event = UserPhotoUpdated.parseFrom(record.value());

            userCloudinaryService.handleUserPhotoUpdate(
                    event.getUserId(),
                    event.getOldPhotoUrl(),
                    event.getImageData().toByteArray()
            );
            log.info("Consumed UserPhotoUpdated event for userId={}", event.getUserId());

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Error parsing event: " + e.getMessage());
        }
    }
}

