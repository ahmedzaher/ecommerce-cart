package com.exequt.ecommerce.domain.model;

public enum OrderState {
    CREATED,
    PENDING_PAYMENT,
    PAYMENT_FAILED,
    PAID,
    CANCELLED;

    public boolean canTransitionTo(OrderState target) {
        return switch (this) {
            case CREATED, PAYMENT_FAILED -> target == PENDING_PAYMENT || target == CANCELLED;
            case PENDING_PAYMENT -> target == PAID || target == PAYMENT_FAILED;
            case PAID, CANCELLED -> false;
        };
    }
}
