package com.lms.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class CheckoutSessionCreationResponse {
    private boolean success;
    private String url;
}
