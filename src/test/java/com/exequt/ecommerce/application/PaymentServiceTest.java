package com.exequt.ecommerce.application;

import com.exequt.ecommerce.application.dto.PaymentResponse;
import com.exequt.ecommerce.application.dto.PaymentStartRequest;
import com.exequt.ecommerce.application.dto.PaymentStartResponse;
import com.exequt.ecommerce.application.dto.WebhookRequest;
import com.exequt.ecommerce.application.exception.DuplicatePaymentException;
import com.exequt.ecommerce.application.exception.IdempotencyKeyMismatchException;
import com.exequt.ecommerce.application.exception.ResourceNotFoundException;
import com.exequt.ecommerce.application.service.PaymentService;
import com.exequt.ecommerce.domain.gateway.PaymentGateway;
import com.exequt.ecommerce.domain.model.*;
import com.exequt.ecommerce.domain.repository.OrderRepository;
import com.exequt.ecommerce.domain.repository.PaymentRepository;
import com.exequt.ecommerce.infrastructure.persistence.PaymentMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private PaymentService paymentService;

    private Order order;
    private Payment payment;
    private PaymentStartRequest request;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, orderRepository, paymentGateway, new PaymentMapperImpl());
        Cart cart = Cart.create();
        cart.addItem(new CartItem("prod-1", 2, BigDecimal.valueOf(50)));
        order = Order.createFromCart(cart);

        payment = Payment.create(order.getId(), order.getTotalAmount(), UUID.randomUUID().toString());
        request = new PaymentStartRequest(payment.getIdempotencyKey());
    }

    @Test
    void shouldStartPayment() {
        when(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).thenReturn(Optional.empty());
        when(orderRepository.findByIdWithLock(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING))
                .thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentStartResponse response = paymentService.startPayment(order.getId(), request);

        assertThat(response.orderId()).isEqualTo(order.getId());
        verify(paymentGateway).initiatePayment(any(Payment.class));
    }

    @Test
    void shouldReturnCachedPaymentOnRetry() {
        when(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).thenReturn(Optional.of(payment));

        PaymentStartResponse response = paymentService.startPayment(order.getId(), request);

        assertThat(response.paymentId()).isEqualTo(payment.getId());
        assertThat(response.idempotencyKey()).isEqualTo(payment.getIdempotencyKey());
        verify(orderRepository, never()).findByIdWithLock(any());
        verify(paymentGateway, never()).initiatePayment(any());
    }

    @Test
    void shouldPreventDuplicateActivePayments() {
        payment = Payment.create(order.getId(), order.getTotalAmount(), "other-key");
        PaymentStartRequest otherRequest = new PaymentStartRequest("other-key");
        when(paymentRepository.findByIdempotencyKey("other-key")).thenReturn(Optional.empty());
        when(orderRepository.findByIdWithLock(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> paymentService.startPayment(order.getId(), otherRequest))
                .isInstanceOf(DuplicatePaymentException.class);
    }

    @Test
    void shouldThrowWhenOrderAlreadyPaid() {
        order.transitionTo(OrderState.PENDING_PAYMENT);
        order.transitionTo(OrderState.PAID);
        when(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).thenReturn(Optional.empty());
        when(orderRepository.findByIdWithLock(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.startPayment(order.getId(), request))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void shouldThrowWhenOrderAlreadyCancelled() {
        order.transitionTo(OrderState.PENDING_PAYMENT);
        order.transitionTo(OrderState.PAYMENT_FAILED);
        order.transitionTo(OrderState.CANCELLED);
        when(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).thenReturn(Optional.empty());
        when(orderRepository.findByIdWithLock(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.startPayment(order.getId(), request))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void shouldHandleConfirmWebhook() {
        order.transitionTo(OrderState.PENDING_PAYMENT);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        WebhookRequest webhook = new WebhookRequest(payment.getId(), "CONFIRMED", payment.getIdempotencyKey());

        paymentService.handleWebhook(webhook);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CONFIRMED);
        assertThat(order.getState()).isEqualTo(OrderState.PAID);
    }

    @Test
    void shouldHandleFailWebhook() {
        order.transitionTo(OrderState.PENDING_PAYMENT);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        WebhookRequest webhook = new WebhookRequest(payment.getId(), "FAILED", payment.getIdempotencyKey());

        paymentService.handleWebhook(webhook);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getState()).isEqualTo(OrderState.PAYMENT_FAILED);
    }

    @Test
    void shouldHandleDuplicateWebhookIdempotently() {
        payment.confirm();
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        WebhookRequest webhook = new WebhookRequest(payment.getId(), "CONFIRMED", payment.getIdempotencyKey());

        PaymentResponse response = paymentService.handleWebhook(webhook);

        assertThat(response.status()).isEqualTo("CONFIRMED");
        verify(orderRepository, never()).findById(any());
    }

    @Test
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById("nonexistent")).thenReturn(Optional.empty());

        WebhookRequest webhook = new WebhookRequest("nonexistent", "CONFIRMED", null);

        assertThatThrownBy(() -> paymentService.handleWebhook(webhook))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenIdempotencyKeyMismatch() {
        order.transitionTo(OrderState.PENDING_PAYMENT);
        payment = Payment.create(order.getId(), order.getTotalAmount(), "correct-key");
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        WebhookRequest webhook = new WebhookRequest(payment.getId(), "CONFIRMED", "wrong-key");

        assertThatThrownBy(() -> paymentService.handleWebhook(webhook))
                .isInstanceOf(IdempotencyKeyMismatchException.class);
    }

    @Test
    void shouldSkipIdempotencyCheckWhenKeyIsNull() {
        order.transitionTo(OrderState.PENDING_PAYMENT);
        payment = Payment.create(order.getId(), order.getTotalAmount(), UUID.randomUUID().toString());
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        WebhookRequest webhook = new WebhookRequest(payment.getId(), "CONFIRMED", null);

        PaymentResponse response = paymentService.handleWebhook(webhook);

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(paymentRepository.findByIdempotencyKey(request.idempotencyKey())).thenReturn(Optional.empty());
        when(orderRepository.findByIdWithLock(order.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.startPayment(order.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
