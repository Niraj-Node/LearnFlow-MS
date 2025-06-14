package com.lms.paymentservice.controller;

import com.lms.paymentservice.dto.CheckoutSessionCreationResponse;
import com.lms.paymentservice.service.CoursePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final CoursePaymentService coursePaymentService;

    @PostMapping("/course/checkout/{courseId}")
    public ResponseEntity<CheckoutSessionCreationResponse> createCheckoutSession(@PathVariable UUID courseId) {
        CheckoutSessionCreationResponse response = coursePaymentService.createCheckoutSession(courseId);
        return ResponseEntity.ok(response);
    }
}

