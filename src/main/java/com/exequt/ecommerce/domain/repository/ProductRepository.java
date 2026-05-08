package com.exequt.ecommerce.domain.repository;

import com.exequt.ecommerce.domain.model.Product;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(String id);
    Product save(Product product);
}
