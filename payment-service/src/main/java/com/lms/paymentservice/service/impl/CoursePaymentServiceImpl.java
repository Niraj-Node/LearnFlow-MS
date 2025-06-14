package com.lms.paymentservice.service.impl;

import com.lms.grpc.GetCourseDetailsByIdRequest;
import com.lms.grpc.GetCourseDetailsByIdResponse;
import com.lms.grpc.CourseServiceGrpc;
import com.lms.paymentservice.auth.UserContextHolder;
import com.lms.paymentservice.dto.CheckoutSessionCreationResponse;
import com.lms.paymentservice.enums.Status;
import com.lms.paymentservice.model.CoursePurchase;
import com.lms.paymentservice.service.CoursePaymentService;
import com.lms.paymentservice.util.CoursePaymentUtil;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.lms.paymentservice.repository.CoursePurchaseRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoursePaymentServiceImpl implements CoursePaymentService {

    private final CoursePurchaseRepository coursePurchaseRepository;
    private final CoursePaymentUtil coursePaymentUtil;
    private final CourseServiceGrpc.CourseServiceBlockingStub courseStub;

    @Override
    public CheckoutSessionCreationResponse createCheckoutSession(UUID courseId) {
        UUID userId = UserContextHolder.getCurrentUserId();

        GetCourseDetailsByIdResponse course = courseStub.getCourseDetailsById(
                GetCourseDetailsByIdRequest.newBuilder().setCourseId(courseId.toString()).build()
        );

        coursePurchaseRepository.findByCourseIdAndUserId(courseId, userId).ifPresent(existingPurchase -> {
            if (existingPurchase.getStatus() == Status.SUCCESS) {
                throw new IllegalStateException("Course already purchased by the user");
            }
            coursePurchaseRepository.delete(existingPurchase); // Remove failed/incomplete
        });

        Session session = coursePaymentUtil.createCheckoutSession(courseId, userId, course);
        CoursePurchase purchase = new CoursePurchase();
        purchase.setCourseId(courseId);
        purchase.setUserId(userId);
        purchase.setAmount(course.getPrice());
        purchase.setPaymentId(session.getId());
        purchase.setStatus(Status.PENDING);

        coursePurchaseRepository.save(purchase);

        return new CheckoutSessionCreationResponse(true, session.getUrl());
    }
}
