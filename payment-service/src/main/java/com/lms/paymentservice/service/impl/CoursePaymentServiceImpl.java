package com.lms.paymentservice.service.impl;

import com.lms.paymentservice.auth.UserContextHolder;
import com.lms.paymentservice.dto.CheckoutSessionCreationResponse;
import com.lms.paymentservice.enums.Status;
import com.lms.paymentservice.exception.PaymentException;
import com.lms.paymentservice.model.CoursePurchase;
import com.lms.paymentservice.service.CoursePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import com.lms.paymentservice.repository.CoursePurchaseRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoursePaymentServiceImpl implements CoursePaymentService {

    private final CoursePurchaseRepository coursePurchaseRepository;
    private final Environment environment;

    @Override
    public CheckoutSessionCreationResponse createCheckoutSession(UUID courseId) {
        UUID userId = UserContextHolder.getCurrentUserId();

        // TODO: Fetch course details using courseId from the course service via gRPC
        String courseTitle = "Dummy Course";
        String courseThumbnail = "https://dummyimage.com/600x400";
        double coursePrice = 499.0; // also fetch creator id

        coursePurchaseRepository.findByCourseIdAndUserId(courseId, userId).ifPresent(existingPurchase -> {
            if (existingPurchase.getStatus() == Status.SUCCESS) {
                throw new IllegalStateException("Course already purchased by the user");
            } else {
                coursePurchaseRepository.delete(existingPurchase); // Delete incomplete purchase
            }
        });

        try {
            // Fetch Stripe key fetch for creator id from Table
            String stripeKey = environment.getRequiredProperty("stripe.secret.key");

            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(stripeKey)
                    .build();

            SessionCreateParams params = SessionCreateParams.builder()
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
                                                    .setUnitAmount((long) (coursePrice * 100))
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(courseTitle)
                                                                    .addImage(courseThumbnail)
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

            Session session = Session.create(params, requestOptions);

            CoursePurchase purchase = new CoursePurchase();
            purchase.setCourseId(courseId);
            purchase.setUserId(userId);
            purchase.setAmount(coursePrice);
            purchase.setPaymentId(session.getId());
            purchase.setStatus(Status.PENDING);

            coursePurchaseRepository.save(purchase);

            return new CheckoutSessionCreationResponse(true, session.getUrl());

        } catch (StripeException e) {
            throw new PaymentException("Stripe API error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while creating Stripe session", e);
        }
    }
}
