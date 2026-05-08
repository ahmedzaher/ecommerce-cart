package com.exequt.ecommerce.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<ProductEntity, String> {
}
