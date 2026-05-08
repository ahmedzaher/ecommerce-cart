package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.domain.model.Product;
import com.exequt.ecommerce.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.findById("prod-001").isPresent()) {
            return;
        }

        productRepository.save(Product.create("prod-001", "Wireless Mouse", new BigDecimal("29.99"), true));
        productRepository.save(Product.create("prod-002", "Mechanical Keyboard", new BigDecimal("89.99"), true));
        productRepository.save(Product.create("prod-003", "USB-C Cable", new BigDecimal("9.99"), true));
        productRepository.save(Product.create("prod-004", "Monitor Stand", new BigDecimal("49.99"), false));
    }
}
