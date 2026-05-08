package com.exequt.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Cart details including items, total amount, and lock status")
public record CartResponse(
        @Schema(description = "Unique cart identifier", example = "a1b2c3d4")
        String id,

        @Schema(description = "Items currently in the cart")
        List<CartItemDto> items,

        @Schema(description = "Sum of all item subtotals", example = "199.98")
        BigDecimal totalAmount,

        @Schema(description = "Whether the cart has been locked for checkout")
        boolean locked,

        @Schema(description = "When the cart was created")
        LocalDateTime createdAt) {
}
