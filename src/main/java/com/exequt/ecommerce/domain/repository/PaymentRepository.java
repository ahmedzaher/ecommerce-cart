package com.exequt.ecommerce.domain.repository;

import com.exequt.ecommerce.domain.model.Payment;
import com.exequt.ecommerce.domain.model.PaymentStatus;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(String id);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status);
}
