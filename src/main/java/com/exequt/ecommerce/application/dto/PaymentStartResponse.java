package com.exequt.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Response returned after initiating a payment")
public record PaymentStartResponse(
        @Schema(description = "Unique payment identifier", example = "p-12345")
        String paymentId,

        @Schema(description = "ID of the order being paid", example = "e5f6g7h8")
        String orderId,

        @Schema(description = "Payment amount", example = "199.98")
        BigDecimal amount,

        @Schema(description = "Payment status", example = "PENDING")
        String status,

        @Schema(description = "Client-provided idempotency key echoed back for correlation", example = "pay-retry-abc-123")
        String idempotencyKey) {
}
