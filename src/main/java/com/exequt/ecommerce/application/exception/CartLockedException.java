package com.exequt.ecommerce.application.exception;

public class CartLockedException extends RuntimeException {
    public CartLockedException(String cartId) {
        super("Cart " + cartId + " is already locked");
    }
}
