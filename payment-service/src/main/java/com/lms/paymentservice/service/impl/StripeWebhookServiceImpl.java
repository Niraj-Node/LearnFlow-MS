package com.lms.paymentservice.service.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lms.paymentservice.enums.Status;
import com.lms.paymentservice.exception.PaymentException;
import com.lms.paymentservice.kafka.KafkaProducer;
import com.lms.paymentservice.model.CoursePurchase;
import com.lms.paymentservice.repository.CoursePurchaseRepository;
import com.lms.paymentservice.service.StripeWebhookService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookServiceImpl implements StripeWebhookService {

    private final CoursePurchaseRepository coursePurchaseRepository;
    private final KafkaProducer kafkaProducer;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public void handleStripeWebhookEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                var deserializer = event.getDataObjectDeserializer();

                Session session;
                if (deserializer.getObject().isPresent()) {
                    Object stripeObject = deserializer.getObject().get();
                    log.info("Deserialized object class: {}", stripeObject.getClass().getName());

                    if (!(stripeObject instanceof Session)) {
                        throw new PaymentException("Unexpected object type in event data");
                    }
                    session = (Session) stripeObject;
                } else {
                    log.warn("Session object not present in webhook. Attempting to retrieve manually.");
                    JsonObject rawPayload = JsonParser.parseString(payload).getAsJsonObject();
                    JsonObject dataObject = rawPayload.getAsJsonObject("data").getAsJsonObject("object");
                    String sessionId = dataObject.get("id").getAsString();
                    session = Session.retrieve(sessionId);
                }

                handleCheckoutSessionCompleted(session);
            }

        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature", e);
            throw new PaymentException("Invalid Stripe signature", e);
        } catch (Exception e) {
            log.error("Webhook handling failed", e);
            throw new PaymentException("Webhook handling failed", e);
        }
    }

    private void handleCheckoutSessionCompleted(Session session) {
        String sessionId = session.getId();

        Optional<CoursePurchase> optionalPurchase = coursePurchaseRepository.findByPaymentId(sessionId);
        if (optionalPurchase.isEmpty()) {
            throw new PaymentException("Course purchase not found for session ID: " + sessionId);
        }

        CoursePurchase purchase = optionalPurchase.get();
        purchase.setStatus(Status.SUCCESS);
        coursePurchaseRepository.save(purchase);

        kafkaProducer.sendCoursePurchaseCompletedEvent(
                purchase.getCourseId().toString(),
                purchase.getUserId().toString()
        );
    }
}