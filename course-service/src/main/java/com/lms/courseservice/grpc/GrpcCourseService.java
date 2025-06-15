package com.lms.courseservice.grpc;

import com.lms.courseservice.exception.ResourceNotFoundException;
import com.lms.courseservice.model.Course;
import com.lms.courseservice.repository.CourseRepository;
import com.lms.grpc.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcCourseService extends CourseServiceGrpc.CourseServiceImplBase {

    private final CourseRepository courseRepository;

    @Override
    public void getCourseDetailsById(GetCourseDetailsByIdRequest request,
                                     StreamObserver<GetCourseDetailsByIdResponse> responseObserver) {
        try {
            UUID courseId = UUID.fromString(request.getCourseId());

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

            GetCourseDetailsByIdResponse response = GetCourseDetailsByIdResponse.newBuilder()
                    .setId(course.getId().toString())
                    .setTitle(course.getCourseTitle())
                    .setThumbnail(course.getCourseThumbnail() != null ? course.getCourseThumbnail() : "")
                    .setPrice(course.getCoursePrice() != null ? course.getCoursePrice() : 0.0)
                    .setCreatorId(course.getCreatorId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getCourseCreatorById(GetCourseCreatorRequest request,
                                     StreamObserver<GetCourseCreatorResponse> responseObserver) {
        try {
            UUID courseId = UUID.fromString(request.getCourseId());

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

            GetCourseCreatorResponse response = GetCourseCreatorResponse.newBuilder()
                    .setCreatorId(course.getCreatorId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void checkLectureExistenceAndGetCount(LectureExistenceAndCountRequest request,
                                                 StreamObserver<LectureExistenceAndCountResponse> responseObserver) {
        try {
            UUID courseId = UUID.fromString(request.getCourseId());
            UUID lectureId = UUID.fromString(request.getLectureId());

            List<UUID> lectureIds = courseRepository.findLectureIdsByCourseId(courseId);
            boolean lectureExists = lectureIds.contains(lectureId);

            LectureExistenceAndCountResponse response = LectureExistenceAndCountResponse.newBuilder()
                    .setLectureExists(lectureExists)
                    .setTotalLectures(lectureIds.size())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to check lecture existence: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
