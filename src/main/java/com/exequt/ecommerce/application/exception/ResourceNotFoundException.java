package com.exequt.ecommerce.application.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found: " + id);
    }
}
