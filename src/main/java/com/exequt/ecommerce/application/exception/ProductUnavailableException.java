package com.exequt.ecommerce.application.exception;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException(String productId) {
        super("Product is not available: " + productId);
    }
}
