package com.exequt.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Webhook payload received from the payment provider")
public record WebhookRequest(
        @JsonProperty("paymentId")
        @Schema(description = "ID of the payment being reported", example = "p-12345")
        String paymentId,

        @JsonProperty("event")
        @Schema(description = "Event type: CONFIRMED or FAILED", example = "CONFIRMED", allowableValues = {"CONFIRMED", "FAILED"})
        String event,

        @JsonProperty("idempotencyKey")
        @Schema(description = "Unique key for deduplication — same key arriving twice is a no-op", example = "idem-abc123")
        String idempotencyKey) {
}
