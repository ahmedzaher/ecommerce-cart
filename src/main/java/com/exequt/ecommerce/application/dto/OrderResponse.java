package com.exequt.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order details including state, items, and amounts")
public record OrderResponse(
        @Schema(description = "Unique order identifier", example = "e5f6g7h8")
        String id,

        @Schema(description = "ID of the source cart", example = "a1b2c3d4")
        String cartId,

        @Schema(description = "Line items from the original cart")
        List<CartItemDto> items,

        @Schema(description = "Current order state", example = "CREATED",
                allowableValues = {"CREATED", "PENDING_PAYMENT", "PAYMENT_FAILED", "PAID", "CANCELLED"})
        String state,

        @Schema(description = "Total order amount", example = "199.98")
        BigDecimal totalAmount,

        @Schema(description = "When the order was created")
        LocalDateTime createdAt,

        @Schema(description = "When the order was last updated")
        LocalDateTime updatedAt) {
}
