package com.lms.progressservice.service.impl;

import com.lms.grpc.CheckUserCoursePurchaseRequest;
import com.lms.grpc.CheckUserCoursePurchaseResponse;
import com.lms.grpc.CourseServiceGrpc;
import com.lms.grpc.PaymentServiceGrpc;
import com.lms.progressservice.dto.CourseProgressResponse;
import com.lms.progressservice.exception.ForbiddenException;
import com.lms.progressservice.exception.ResourceNotFoundException;
import com.lms.progressservice.model.CourseProgress;
import com.lms.progressservice.model.LectureProgress;
import com.lms.progressservice.repository.CourseProgressRepository;
import com.lms.progressservice.service.ICourseProgressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseProgressServiceImpl implements ICourseProgressService {

    private final CourseProgressRepository courseProgressRepository;
    @GrpcClient("paymentService")
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentStub;
    @GrpcClient("courseService")
    private CourseServiceGrpc.CourseServiceBlockingStub courseStub;

    @Override
    @Transactional
    public void updateLectureProgress(UUID courseId, UUID lectureId, UUID userId) {
        // Check if course is purchased by current user
        if (hasUserPurchasedCourse(userId, courseId)) {
            throw new ForbiddenException("Course not purchased by user");
        }

        CourseProgress courseProgress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course progress not found"));

        // Update lecture viewed status
        Optional<LectureProgress> lectureProgressOpt = courseProgress.getLectureProgress().stream()
                .filter(lp -> lp.getLectureId().equals(lectureId))
                .findFirst();
        if (lectureProgressOpt.isEmpty()) {
            throw new ResourceNotFoundException("Lecture not found in course progress");
        }
        lectureProgressOpt.get().setViewed(true);

        // Check if course is completed
        boolean allViewed = courseProgress.getLectureProgress().stream()
                .allMatch(lp -> Boolean.TRUE.equals(lp.getViewed()));
        courseProgress.setCompleted(allViewed);

        courseProgressRepository.save(courseProgress);
    }

    @Override
    public CourseProgressResponse getCourseProgress(UUID userId, UUID courseId) {
        // Check if course is purchased by current user
        if (hasUserPurchasedCourse(userId, courseId)) {
            throw new ForbiddenException("Course not purchased by user");
        }

        CourseProgress progress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course progress not found"));
        return new CourseProgressResponse(progress.getLectureProgress(), progress.isCompleted());
    }

    @Override
    @Transactional
    public void markCourseAsCompleted(UUID userId, UUID courseId) {
        if (!hasUserPurchasedCourse(userId, courseId)) {
            throw new ForbiddenException("Course not purchased by user");
        }

        CourseProgress courseProgress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course progress not found"));

        for (LectureProgress lp : courseProgress.getLectureProgress()) {
            lp.setViewed(true);
        }
        courseProgress.setCompleted(true);
        courseProgressRepository.save(courseProgress);
    }

    @Override
    @Transactional
    public void markCourseAsIncomplete(UUID userId, UUID courseId) {
        if (!hasUserPurchasedCourse(userId, courseId)) {
            throw new ForbiddenException("Course not purchased by user");
        }

        CourseProgress courseProgress = courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course progress not found"));

        for (LectureProgress lp : courseProgress.getLectureProgress()) {
            lp.setViewed(false);
        }
        courseProgress.setCompleted(false);
        courseProgressRepository.save(courseProgress);
    }

    // Helper Methods

    private boolean hasUserPurchasedCourse(UUID userId, UUID courseId) {
        CheckUserCoursePurchaseResponse response = paymentStub.checkUserCoursePurchase(
                CheckUserCoursePurchaseRequest.newBuilder()
                        .setUserId(userId.toString())
                        .setCourseId(courseId.toString())
                        .build()
        );
        return !response.getHasPurchased();
    }
}
