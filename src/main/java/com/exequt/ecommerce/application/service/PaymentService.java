package com.exequt.ecommerce.application.service;

import com.exequt.ecommerce.application.dto.PaymentResponse;
import com.exequt.ecommerce.application.dto.PaymentStartRequest;
import com.exequt.ecommerce.application.dto.PaymentStartResponse;
import com.exequt.ecommerce.application.dto.WebhookRequest;
import com.exequt.ecommerce.application.exception.DuplicatePaymentException;
import com.exequt.ecommerce.application.exception.IdempotencyKeyMismatchException;
import com.exequt.ecommerce.application.exception.ResourceNotFoundException;
import com.exequt.ecommerce.domain.gateway.PaymentGateway;
import com.exequt.ecommerce.domain.model.*;
import com.exequt.ecommerce.domain.repository.OrderRepository;
import com.exequt.ecommerce.domain.repository.PaymentRepository;
import com.exequt.ecommerce.infrastructure.persistence.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentMapper paymentMapper;

    public PaymentStartResponse startPayment(String orderId, PaymentStartRequest request) {
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return paymentMapper.toStartResponse(existing.get());
        }

        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getState() != OrderState.CREATED
                && order.getState() != OrderState.PAYMENT_FAILED) {
            throw new InvalidStateTransitionException(order.getState(), OrderState.PENDING_PAYMENT);
        }

        if(paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.PENDING)) {
            throw new DuplicatePaymentException(orderId);
        }

        Payment payment = Payment.create(orderId, order.getTotalAmount(), request.idempotencyKey());
        payment = paymentRepository.save(payment);

        order.transitionTo(OrderState.PENDING_PAYMENT);
        orderRepository.save(order);

        paymentGateway.initiatePayment(payment);

        return paymentMapper.toStartResponse(payment);
    }

    public PaymentResponse handleWebhook(WebhookRequest request) {
        Payment payment = paymentRepository.findById(request.paymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", request.paymentId()));

        if (request.idempotencyKey() != null
                && !request.idempotencyKey().equals(payment.getIdempotencyKey())) {
            throw new IdempotencyKeyMismatchException(request.idempotencyKey(), payment.getIdempotencyKey());
        }

        if ("CONFIRMED".equalsIgnoreCase(request.event())
                && payment.getStatus() == PaymentStatus.CONFIRMED) {
            return paymentMapper.toResponse(payment);
        }
        if ("FAILED".equalsIgnoreCase(request.event())
                && payment.getStatus() == PaymentStatus.FAILED) {
            return paymentMapper.toResponse(payment);
        }

        if (!payment.isPending()) {
            throw new IllegalStateException(
                    "Payment " + request.paymentId() + " is not in PENDING state");
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", payment.getOrderId()));

        if ("CONFIRMED".equalsIgnoreCase(request.event())) {
            payment.confirm();
            order.transitionTo(OrderState.PAID);
        } else if ("FAILED".equalsIgnoreCase(request.event())) {
            payment.fail();
            order.transitionTo(OrderState.PAYMENT_FAILED);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);

        return paymentMapper.toResponse(payment);
    }
}
