package com.exequt.ecommerce.application.exception;

public class IdempotencyKeyMismatchException extends RuntimeException {
    public IdempotencyKeyMismatchException(String requestKey, String storedKey) {
        super("Idempotency key mismatch: request key '" + requestKey
                + "' does not match stored key '" + storedKey + "'");
    }
}
