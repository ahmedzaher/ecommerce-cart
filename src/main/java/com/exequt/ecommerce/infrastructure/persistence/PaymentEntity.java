package com.exequt.ecommerce.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_order_status", columnNames = {"order_id", "status"})
})
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {

    @Id
    private String id;

    @Column(name = "order_id")
    private String orderId;

    private BigDecimal amount;

    private String status;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
