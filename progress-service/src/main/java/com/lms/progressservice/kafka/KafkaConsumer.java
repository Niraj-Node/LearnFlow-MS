package com.lms.progressservice.kafka;

import com.lms.grpc.GetAllUserIdsRequest;
import com.lms.grpc.GetAllUserIdsResponse;
import com.lms.grpc.UserServiceGrpc;
import com.lms.progressservice.model.CourseProgress;
import com.lms.progressservice.model.LectureProgress;
import com.lms.progressservice.repository.CourseProgressRepository;
import course.events.LectureEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import user.events.CourseEvent.CourseCreated;

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
}
