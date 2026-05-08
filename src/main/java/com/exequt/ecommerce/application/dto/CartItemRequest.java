package com.exequt.ecommerce.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body for adding an item to a cart")
public record CartItemRequest(
        @NotBlank
        @Schema(description = "Product identifier", example = "prod-001")
        String productId,

        @Positive
        @Schema(description = "Quantity to add", example = "2")
        int quantity) {
}
