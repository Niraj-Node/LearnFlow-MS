package com.lms.progressservice.kafka;

import com.lms.grpc.GetAllUserIdsRequest;
import com.lms.grpc.GetAllUserIdsResponse;
import com.lms.grpc.UserServiceGrpc;
import com.lms.progressservice.model.CourseProgress;
import com.lms.progressservice.model.LectureProgress;
import com.lms.progressservice.repository.CourseProgressRepository;
import course.events.LectureEvent;
import user.events.UserEvent.UserCreated;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import user.events.CourseEvent.CourseCreated;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final CourseProgressRepository courseProgressRepository;

    @GrpcClient("userService")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    @KafkaListener(topics = "course-created-topic", groupId = "progress-service-group")
    @Transactional
    public void handleCourseCreatedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            CourseCreated event = CourseCreated.parseFrom(record.value());
            UUID courseId = UUID.fromString(event.getCourseId());

            // Fetch all user IDs from user-service
            GetAllUserIdsResponse userIdsResponse = userStub.getAllUserIds(GetAllUserIdsRequest.newBuilder().build());
            List<CourseProgress> progressList = new ArrayList<>();

            for (String userIdStr : userIdsResponse.getUserIdsList()) {
                UUID userId = UUID.fromString(userIdStr);
                CourseProgress progress = CourseProgress.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .completed(false)
                        .lectureProgress(new ArrayList<>()) // empty initially
                        .build();
                progressList.add(progress);
            }

            courseProgressRepository.saveAll(progressList);
            log.info("Initialized CourseProgress for {} users for courseId={}", progressList.size(), courseId);
        } catch (Exception e) {
            log.error("Failed to handle CourseCreated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "lecture-created-topic", groupId = "progress-service-group")
    @Transactional
    public void handleLectureCreatedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            LectureEvent.LectureCreated event = LectureEvent.LectureCreated.parseFrom(record.value());
            UUID courseId = UUID.fromString(event.getCourseId());
            UUID lectureId = UUID.fromString(event.getLectureId());

            List<CourseProgress> progresses = courseProgressRepository.findAllByCourseId(courseId);

            for (CourseProgress progress : progresses) {
                    progress.getLectureProgress().add(new LectureProgress(lectureId, false));
                    progress.setCompleted(false); // new lecture means not completed
            }

            courseProgressRepository.saveAll(progresses);
            log.info("Appended new lectureId={} to {} CourseProgress records for courseId={}",
                    lectureId, progresses.size(), courseId);

        } catch (Exception e) {
            log.error("Failed to handle LectureCreated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-created-topic", groupId = "progress-service-group")
    @Transactional
    public void handleUserCreatedEvent(ConsumerRecord<String, byte[]> record) {
        try {
            UserCreated event = UserCreated.parseFrom(record.value());
            UUID userId = UUID.fromString(event.getUserId());

            // 1. Get all distinct courseIds from the DB
            List<UUID> distinctCourseIds = courseProgressRepository.findDistinctCourseIds();
            List<CourseProgress> progressList = new ArrayList<>();

            for (UUID courseId : distinctCourseIds) {

                // 2. Fetch any one CourseProgress for the course to extract its lectureProgress list
                Optional<CourseProgress> existing = courseProgressRepository
                        .findFirstByCourseId(courseId);
                if (existing.isEmpty()) continue;

                List<LectureProgress> lectureProgressCopy = existing.get().getLectureProgress().stream()
                        .map(lp -> new LectureProgress(lp.getLectureId(), false)) // copy with viewed=false
                        .toList();

                CourseProgress progress = CourseProgress.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .lectureProgress(lectureProgressCopy)
                        .completed(false)
                        .build();

                progressList.add(progress);
            }

            courseProgressRepository.saveAll(progressList);
            log.info("Initialized CourseProgress for new userId={} for {} existing courses",
                    userId, progressList.size());

        } catch (Exception e) {
            log.error("Failed to handle UserCreated event: {}", e.getMessage(), e);
        }
    }

}
