package com.exequt.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Single line item in a cart or order")
public record CartItemDto(
        @Schema(description = "Product identifier", example = "prod-001")
        String productId,

        @Schema(description = "Quantity ordered", example = "2")
        int quantity,

        @Schema(description = "Unit price", example = "99.99")
        BigDecimal price,

        @Schema(description = "Quantity × price", example = "199.98")
        BigDecimal subtotal) {
}
