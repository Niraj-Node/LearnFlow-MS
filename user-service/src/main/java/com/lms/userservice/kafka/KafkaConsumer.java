package com.lms.userservice.kafka;

import com.lms.userservice.enums.Role;
import course.events.CourseEvent.CourseCreated;
import cloudinary.events.CloudinaryEvent.UserPhotoUploadCompleted;
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

    @KafkaListener(topics = "user-photo-upload-completed-topic", groupId = "user-service")
    public void consumeUserPhotoUploadCompletedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            UserPhotoUploadCompleted event = UserPhotoUploadCompleted.parseFrom(record.value());

            UUID userId = UUID.fromString(event.getUserId());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            user.setPhotoUrl(event.getNewPhotoUrl());
            userRepository.save(user);

            log.info("Updated user [{}] with new photo URL.", userId);

        } catch (Exception e) {
            log.error("Error handling UserPhotoUploadCompleted event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "course-created-topic", groupId = "user-service")
    public void consumeCourseCreatedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            CourseCreated event = CourseCreated.parseFrom(record.value());

            UUID creatorId = UUID.fromString(event.getCreatorId());
            User user = userRepository.findById(creatorId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (user.getRole() == Role.STUDENT) {
                user.setRole(Role.INSTRUCTOR);
                userRepository.save(user);
                log.info("User [{}] promoted to INSTRUCTOR after creating course", creatorId);
            }

        } catch (Exception e) {
            log.error("Error processing CourseCreated event: {}", e.getMessage(), e);
        }
    }
}