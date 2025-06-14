package com.lms.paymentservice.service;

public interface StripeWebhookService {
    void handleStripeWebhookEvent(String payload, String sigHeader);
}
