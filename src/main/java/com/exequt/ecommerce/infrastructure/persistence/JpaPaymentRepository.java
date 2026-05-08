package com.exequt.ecommerce.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
    boolean existsByOrderIdAndStatus(String orderId, String status);
}
