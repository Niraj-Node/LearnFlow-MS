package com.lms.progressservice.service.impl;

import com.lms.grpc.CheckUserCoursePurchaseRequest;
import com.lms.grpc.CheckUserCoursePurchaseResponse;
import com.lms.grpc.CourseServiceGrpc;
import com.lms.grpc.LectureExistenceAndCountRequest;
import com.lms.grpc.LectureExistenceAndCountResponse;
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
        CheckUserCoursePurchaseResponse purchaseResponse = paymentStub.checkUserCoursePurchase(
                CheckUserCoursePurchaseRequest.newBuilder()
                        .setUserId(userId.toString())
                        .setCourseId(courseId.toString())
                        .build()
        );
        if (!purchaseResponse.getHasPurchased()) {
            throw new ForbiddenException("Course not purchased by user");
        }

        // Check lecture exists and get total count
        LectureExistenceAndCountResponse courseResponse = courseStub.checkLectureExistenceAndGetCount(
                LectureExistenceAndCountRequest.newBuilder()
                        .setCourseId(courseId.toString())
                        .setLectureId(lectureId.toString())
                        .build()
        );

        if (!courseResponse.getLectureExists()) {
            throw new ResourceNotFoundException("Lecture not found in course");
        }

        int totalLectures = courseResponse.getTotalLectures();

        // Find or create CourseProgress
        CourseProgress courseProgress = courseProgressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseGet(() -> CourseProgress.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .completed(false)
                        .lectureProgress(new ArrayList<>())
                        .build());
        // Update lecture viewed status
        boolean alreadyExists = false;
        for (LectureProgress lp : courseProgress.getLectureProgress()) {
            if (lp.getLectureId().equals(lectureId)) {
                lp.setViewed(true);
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            courseProgress.getLectureProgress().add(new LectureProgress(lectureId, true));
        }

        // Check if course is completed
        long viewedCount = courseProgress.getLectureProgress().stream().filter(LectureProgress::getViewed).count();
        if (viewedCount == totalLectures) {
            courseProgress.setCompleted(true);
        }
        courseProgressRepository.save(courseProgress);
    }

    @Override
    public CourseProgressResponse getCourseProgress(UUID userId, UUID courseId) {
        return courseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .map(progress -> new CourseProgressResponse(progress.getLectureProgress(), progress.isCompleted()))
                .orElseGet(() -> new CourseProgressResponse(Collections.emptyList(), false));
    }
}
