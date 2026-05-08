package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.domain.model.Cart;
import com.exequt.ecommerce.domain.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final JpaCartRepository jpaRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    public Cart save(Cart cart) {
        CartEntity existing = jpaRepository.findById(cart.getId()).orElse(null);
        CartEntity entity;
        if (existing == null) {
            entity = cartMapper.toEntity(cart);
        } else {
            entity = syncExistingEntity(existing, cart);
        }
        entity = jpaRepository.save(entity);
        return cartMapper.toDomain(entity);
    }

    private CartEntity syncExistingEntity(CartEntity existing, Cart cart) {
        existing.setLocked(cart.isLocked());
        existing.setCreatedAt(cart.getCreatedAt());
        syncItems(existing, cart);
        return existing;
    }

    private void syncItems(CartEntity entity, Cart cart) {
        entity.getItems().clear();
        if (cart.getItems() == null) {
            return;
        }
        cart.getItems().stream()
                .map(cartItemMapper::toEntity)
                .forEach(entity::addItem);
    }

    @Override
    public Optional<Cart> findById(String id) {
        return jpaRepository.findById(id).map(cartMapper::toDomain);
    }
}
