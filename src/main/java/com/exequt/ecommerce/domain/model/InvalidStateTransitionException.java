package com.exequt.ecommerce.domain.model;

public class InvalidStateTransitionException extends RuntimeException {
    private final OrderState fromState;
    private final OrderState toState;

    public InvalidStateTransitionException(OrderState fromState, OrderState toState) {
        super("Invalid state transition: " + fromState + " -> " + toState);
        this.fromState = fromState;
        this.toState = toState;
    }

    public OrderState getFromState() {
        return fromState;
    }

    public OrderState getToState() {
        return toState;
    }
}
