package com.lms.paymentservice.controller;

import com.lms.paymentservice.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook")
public class StripeWebhookController {

    private final StripeWebhookService webhookService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody byte[] payloadBytes,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        webhookService.handleStripeWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
