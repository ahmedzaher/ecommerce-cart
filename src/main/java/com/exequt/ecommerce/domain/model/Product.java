package com.exequt.ecommerce.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class Product {
    private String id;
    private String name;
    private BigDecimal price;
    private boolean available;
    private Long version;

    public Product() {
    }

    public static Product create(String name, BigDecimal price, boolean available) {
        Product product = new Product();
        product.id = UUID.randomUUID().toString();
        product.name = name;
        product.price = price;
        product.available = available;
        return product;
    }

    public static Product create(String id, String name, BigDecimal price, boolean available) {
        Product product = new Product();
        product.id = id;
        product.name = name;
        product.price = price;
        product.available = available;
        return product;
    }
}
