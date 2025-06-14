package com.lms.paymentservice.grpc;

import com.lms.grpc.CheckUserCoursePurchaseRequest;
import com.lms.grpc.CheckUserCoursePurchaseResponse;
import com.lms.grpc.PaymentServiceGrpc;
import com.lms.paymentservice.enums.Status;
import com.lms.paymentservice.repository.CoursePurchaseRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class GrpcPaymentService extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final CoursePurchaseRepository coursePurchaseRepository;

    @Override
    public void checkUserCoursePurchase(CheckUserCoursePurchaseRequest request,
                                        StreamObserver<CheckUserCoursePurchaseResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            UUID courseId = UUID.fromString(request.getCourseId());

            boolean exists = coursePurchaseRepository.existsByUserIdAndCourseIdAndStatus(
                    userId, courseId, Status.SUCCESS
            );

            CheckUserCoursePurchaseResponse response = CheckUserCoursePurchaseResponse.newBuilder()
                    .setHasPurchased(exists)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to check purchase: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}
