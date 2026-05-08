package com.exequt.ecommerce.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCartRepository extends JpaRepository<CartEntity, String> {
}
