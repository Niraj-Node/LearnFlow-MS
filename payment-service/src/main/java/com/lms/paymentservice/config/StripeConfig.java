package com.lms.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Bean
    public RequestOptions requestOptions() {

        Stripe.apiKey = stripeSecretKey;

        return RequestOptions.builder()
                .setApiKey(stripeSecretKey)
                .build();
    }
}
