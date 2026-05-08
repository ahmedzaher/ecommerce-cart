package com.exequt.ecommerce.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class Payment {
    private String id;
    private String orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String idempotencyKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
    }

    public static Payment create(String orderId, BigDecimal amount, String idempotencyKey) {
        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;
        payment.idempotencyKey = idempotencyKey;
        payment.createdAt = LocalDateTime.now();
        payment.updatedAt = payment.createdAt;
        return payment;
    }

    public void confirm() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm payment with status: " + this.status);
        }
        this.status = PaymentStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Cannot fail payment with status: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }
}
