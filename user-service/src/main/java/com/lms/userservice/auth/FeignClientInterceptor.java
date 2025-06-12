package com.lms.userservice.auth;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        UUID userId = UserContextHolder.getCurrentUserId();
        if (userId != null) {
            requestTemplate.header("X-User-Id", userId.toString());
        }
    }
}
