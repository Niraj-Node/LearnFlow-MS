package com.lms.paymentservice.util;

import com.lms.grpc.GetCourseDetailsByIdResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CoursePaymentUtil {

    private final RequestOptions requestOptions;

    public Session createCheckoutSession(UUID courseId, UUID userId, GetCourseDetailsByIdResponse course) {
        try {
            if (course.getPrice() < 50) {
                throw new IllegalArgumentException("Course price must be at least ₹50 to comply with Stripe minimums.");
            }

            SessionCreateParams params = buildParams(courseId, userId, course);
            return Session.create(params, requestOptions);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe API error: " + e.getMessage(), e);
        }
    }

    private SessionCreateParams buildParams(UUID courseId, UUID userId, GetCourseDetailsByIdResponse course) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/course-progress/" + courseId)
                .setCancelUrl("http://localhost:5173/course-detail/" + courseId)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("inr")
                                                .setUnitAmount((long) (course.getPrice() * 100))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(course.getTitle())
                                                                .addImage(course.getThumbnail())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("courseId", courseId.toString())
                .putMetadata("userId", userId.toString())
                .setShippingAddressCollection(
                        SessionCreateParams.ShippingAddressCollection.builder()
                                .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.IN)
                                .build()
                )
                .build();
    }
}
