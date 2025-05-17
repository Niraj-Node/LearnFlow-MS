package com.lms.cloudinaryservice.kafka;

import cloudinary.events.UserEventOuterClass.UserEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.lms.cloudinaryservice.model.UserEventType;
import com.lms.cloudinaryservice.service.UserCloudinaryService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final UserCloudinaryService userCloudinaryService;

    @KafkaListener(topics = "user", groupId = "cloudinary-service")
    public void consumeUserEvent(ConsumerRecord<String, byte[]> record) {
        try {
            UserEvent event = UserEvent.parseFrom(record.value());

            if (event.getEventType().equals(UserEventType.USER_PHOTO_UPDATED.name())) {
                userCloudinaryService.handleUserPhotoUpdate(event.getUserId(), event.getOldPhotoUrl(), event.getNewPhotoBase64());
            }

        } catch (InvalidProtocolBufferException e) {
            System.err.println("Error parsing event: " + e.getMessage());
        }
    }
}

