package com.lms.userservice.kafka;

import cloudinary.events.UserEventOuterClass.UserEvent;
import com.lms.userservice.exception.ResourceNotFoundException;
import com.lms.userservice.model.User;
import com.lms.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final UserRepository userRepository;

    @KafkaListener(topics = "user", groupId = "user-service")
    public void consumeUserPhotoUploadedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            UserEvent event = UserEvent.parseFrom(record.value());

            if (event.getEventType().equals("USER_PHOTO_UPLOAD_COMPLETED")) {
                UUID userId = UUID.fromString(event.getUserId());

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                user.setPhotoUrl(event.getPhotoUrl());
                userRepository.save(user);

                log.info("Updated user [{}] with new photo URL.", userId);
            }

        } catch (Exception e) {
            log.error("Error handling USER_PHOTO_UPLOAD_COMPLETED event: {}", e.getMessage());
        }
    }
}