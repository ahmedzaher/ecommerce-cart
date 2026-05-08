package com.exequt.ecommerce.infrastructure.persistence;

import com.exequt.ecommerce.domain.model.Payment;
import com.exequt.ecommerce.domain.model.PaymentStatus;
import com.exequt.ecommerce.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = paymentMapper.toEntity(payment);
        PaymentEntity existing = jpaRepository.findById(payment.getId()).orElse(null);
        if (existing != null) {
            entity.setVersion(existing.getVersion());
        }
        entity = jpaRepository.save(entity);
        return paymentMapper.toDomain(entity);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return jpaRepository.findById(id).map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return jpaRepository.findByIdempotencyKey(idempotencyKey).map(paymentMapper::toDomain);
    }

    @Override
    public boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status) {
        return jpaRepository.existsByOrderIdAndStatus(orderId, status.name());
    }
}
