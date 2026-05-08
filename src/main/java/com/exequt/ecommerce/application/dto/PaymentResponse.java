package com.exequt.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Payment details including status and idempotency key")
public record PaymentResponse(
        @Schema(description = "Unique payment identifier", example = "p-12345")
        String id,

        @Schema(description = "ID of the order associated with this payment", example = "e5f6g7h8")
        String orderId,

        @Schema(description = "Payment amount", example = "199.98")
        BigDecimal amount,

        @Schema(description = "Current payment status", example = "CONFIRMED",
                allowableValues = {"PENDING", "CONFIRMED", "FAILED"})
        String status,

        @Schema(description = "Idempotency key used for this payment attempt", example = "abc-def-123")
        String idempotencyKey) {
}
