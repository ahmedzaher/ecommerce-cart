package com.exequt.ecommerce.domain.repository;

import com.exequt.ecommerce.domain.model.Order;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    Optional<Order> findByIdWithLock(String id);
    Optional<Order> findByCartId(String cartId);
}
