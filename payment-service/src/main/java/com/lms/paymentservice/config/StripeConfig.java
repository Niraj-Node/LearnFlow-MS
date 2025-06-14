package com.lms.paymentservice.config;

import com.stripe.net.RequestOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Bean
    public RequestOptions requestOptions() {
        return RequestOptions.builder()
                .build();
    }
}
