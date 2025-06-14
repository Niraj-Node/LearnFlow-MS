package com.lms.paymentservice.service;

import com.lms.paymentservice.dto.CheckoutSessionCreationResponse;

import java.util.UUID;

public interface CoursePaymentService {
    CheckoutSessionCreationResponse createCheckoutSession(UUID courseId);
}

