package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.domain.model.Order;
import com.exequt.ecommerce.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity existing = jpaRepository.findById(order.getId()).orElse(null);
        if (existing != null) {
            entity.setVersion(existing.getVersion());
        }
        entity = jpaRepository.save(entity);
        return orderMapper.toDomain(entity);
    }

    @Override
    public Optional<Order> findById(String id) {
        return jpaRepository.findById(id).map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdWithLock(String id) {
        return jpaRepository.findByIdWithLock(id).map(orderMapper::toDomain);
    }

    @Override
    public Optional<Order> findByCartId(String cartId) {
        return jpaRepository.findByCartId(cartId).map(orderMapper::toDomain);
    }
}
