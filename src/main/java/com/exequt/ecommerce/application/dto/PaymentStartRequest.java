package com.exequt.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for initiating a payment on an order")
public record PaymentStartRequest(
        @NotBlank
        @Schema(description = "Client-generated idempotency key. Must be unique per payment attempt. Sending the same key again returns the cached response (safe retry).", example = "pay-retry-abc-123")
        String idempotencyKey) {
}
