package com.exequt.ecommerce.application.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String orderId) {
        super("An active payment already exists for order " + orderId);
    }
}
