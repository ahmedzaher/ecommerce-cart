package com.exequt.ecommerce.domain.repository;

import com.exequt.ecommerce.domain.model.Cart;
import java.util.Optional;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findById(String id);
}
