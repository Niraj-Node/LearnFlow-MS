package com.lms.lectureservice.service.impl;

import com.lms.grpc.*;
import com.lms.lectureservice.dto.LectureRequest;
import com.lms.lectureservice.dto.LectureResponse;
import com.lms.lectureservice.exception.ForbiddenException;
import com.lms.lectureservice.exception.ResourceNotFoundException;
import com.lms.lectureservice.kafka.KafkaProducer;
import com.lms.lectureservice.mapper.LectureMapper;
import com.lms.lectureservice.model.Lecture;
import com.lms.lectureservice.repository.LectureRepository;
import com.lms.lectureservice.service.LectureService;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureServiceImpl implements LectureService {

    private final LectureRepository lectureRepository;
    private final CourseServiceGrpc.CourseServiceBlockingStub courseStub;
    private final KafkaProducer kafkaProducer;
    private final PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;

    @Override
    @Transactional
    public LectureResponse createLecture(UUID currentUserId, UUID courseId, LectureRequest request) {

        Optional<Lecture> existing = lectureRepository.findFirstByCourseId(courseId);
        UUID creatorId;

        if (existing.isPresent()) {
            creatorId = existing.get().getCreatorId();
        } else {
            try {
                GetCourseCreatorResponse response = courseStub.getCourseCreatorById(
                        GetCourseCreatorRequest.newBuilder().setCourseId(courseId.toString()).build()
                );
                creatorId = UUID.fromString(response.getCreatorId());
            } catch (StatusRuntimeException e) {
                throw new ResourceNotFoundException("Course not found");
            }
        }

        // Authorization check
        if (!creatorId.equals(currentUserId)) {
            throw new ForbiddenException("You are not allowed to add lecture to this course.");
        }

        // Save lecture
        Lecture lecture = new Lecture();
        lecture.setLectureTitle(request.getLectureTitle());
        lecture.setCourseId(courseId);
        lecture.setCreatorId(creatorId);
        lecture = lectureRepository.save(lecture);

        kafkaProducer.sendLectureCreatedEvent(courseId, lecture.getId());
        return LectureMapper.toResponseDto(lecture);
    }

    @Override
    @Transactional
    public LectureResponse editLecture(UUID currentUserId, UUID lectureId, LectureRequest request) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        if (!lecture.getCreatorId().equals(currentUserId)) {
            throw new ForbiddenException("You are not allowed to edit this lecture.");
        }

        // Partial update
        if (request.getLectureTitle() != null) lecture.setLectureTitle(request.getLectureTitle());
        if (request.getVideoUrl() != null) lecture.setVideoUrl(request.getVideoUrl());
        if (request.getPublicId() != null) lecture.setPublicId(request.getPublicId());
        if (request.getIsPreviewFree() != null) lecture.setIsPreviewFree(request.getIsPreviewFree());

        Lecture updatedLecture = lectureRepository.save(lecture);
        return LectureMapper.toResponseDto(updatedLecture);
    }

    @Override
    @Transactional
    public void deleteLecture(UUID currentUserId, UUID lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecture not found"));

        if (!lecture.getCreatorId().equals(currentUserId)) {
            throw new ForbiddenException("You are not allowed to delete this lecture.");
        }

        UUID courseId = lecture.getCourseId();
        lectureRepository.delete(lecture);
        // Publish Kafka event
        kafkaProducer.sendLectureDeletedEvent(courseId, lectureId);
    }

    @Override
    public List<LectureResponse> getLecturesByCourse(UUID currentUserId, UUID courseId) {

        boolean hasPurchasedTemp;

        try {
            CheckUserCoursePurchaseResponse response = paymentStub.checkUserCoursePurchase(
                    CheckUserCoursePurchaseRequest.newBuilder()
                            .setUserId(currentUserId.toString())
                            .setCourseId(courseId.toString())
                            .build()
            );
            hasPurchasedTemp = response.getHasPurchased();
        } catch (StatusRuntimeException e) {
            System.out.println("gRPC call to payment service failed: " + e.getMessage());
            hasPurchasedTemp = false;
        }

        final boolean hasPurchased = hasPurchasedTemp;
        List<Lecture> lectures = lectureRepository.findByCourseId(courseId);

        return lectures.stream().map(lecture -> {
            LectureResponse dto = new LectureResponse();
            dto.setId(lecture.getId());
            dto.setLectureTitle(lecture.getLectureTitle());
            dto.setCourseId(lecture.getCourseId());
            dto.setCreatorId(lecture.getCreatorId());
            dto.setIsPreviewFree(lecture.getIsPreviewFree());

            // Only allow full access if user has purchased or it's a preview
            if (hasPurchased || Boolean.TRUE.equals(lecture.getIsPreviewFree())) {
                dto.setVideoUrl(lecture.getVideoUrl());
                dto.setPublicId(lecture.getPublicId());
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
