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
public class Order {
    private String id;
    private String cartId;
    private List<CartItem> items;
    private OrderState state;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order() {
    }

    public static Order createFromCart(Cart cart) {
        Order order = new Order();
        order.id = UUID.randomUUID().toString();
        order.cartId = cart.getId();
        order.items = new ArrayList<>(cart.getItems());
        order.state = OrderState.CREATED;
        order.totalAmount = cart.getTotalAmount();
        order.createdAt = LocalDateTime.now();
        order.updatedAt = order.createdAt;
        return order;
    }

    public void transitionTo(OrderState target) {
        if (!this.state.canTransitionTo(target)) {
            throw new InvalidStateTransitionException(this.state, target);
        }
        this.state = target;
        this.updatedAt = LocalDateTime.now();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
