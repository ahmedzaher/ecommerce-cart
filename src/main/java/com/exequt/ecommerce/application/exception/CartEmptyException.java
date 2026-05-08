package com.exequt.ecommerce.application.exception;

public class CartEmptyException extends RuntimeException {
    public CartEmptyException(String cartId) {
        super("Cart " + cartId + " is empty");
    }
}
