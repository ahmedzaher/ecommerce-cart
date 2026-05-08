package com.exequt.ecommerce.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItem {
    private String productId;
    private int quantity;
    private BigDecimal price;

    public CartItem() {
    }

    public CartItem(String productId, int quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
