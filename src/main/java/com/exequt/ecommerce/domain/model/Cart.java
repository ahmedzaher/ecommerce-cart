package com.exequt.ecommerce.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Cart {
    private String id;
    private List<CartItem> items;
    private boolean locked;
    private LocalDateTime createdAt;

    public Cart() {
        this.id = UUID.randomUUID().toString();
        this.items = new ArrayList<>();
        this.locked = false;
        this.createdAt = LocalDateTime.now();
    }

    public static Cart create() {
        return new Cart();
    }

    public void addItem(CartItem item) {
        if (locked) {
            throw new IllegalStateException("Cannot add items to a locked cart");
        }
        items.add(item);
    }

    public void lock() {
        this.locked = true;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
