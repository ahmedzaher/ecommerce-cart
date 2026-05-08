package com.exequt.ecommerce.integration;

import com.exequt.ecommerce.application.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.webhook.base-url=http://localhost:${local.server.port}")
class WebhookDuplicateIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void shouldHandleDuplicateWebhookIdempotently() {
        // Create cart
        CartResponse cart = rest.postForObject("/carts", null, CartResponse.class);
        assertThat(cart).isNotNull();

        // Add item
        CartItemRequest item = new CartItemRequest("prod-002", 1);
        rest.postForObject("/carts/{id}/items", item, CartResponse.class, cart.id());

        // Checkout
        OrderResponse order = rest.postForObject("/carts/{id}/checkout", null, OrderResponse.class, cart.id());
        assertThat(order).isNotNull();

        // Start payment with client-provided idempotency key
        PaymentStartRequest payReq = new PaymentStartRequest(UUID.randomUUID().toString());
        PaymentStartResponse payment = rest.postForObject(
                "/orders/{id}/payment/start", payReq, PaymentStartResponse.class, order.id());
        assertThat(payment).isNotNull();

        // First confirm → success
        ResponseEntity<PaymentResponse> first = rest.postForEntity(
                "/mock-payment/{id}/confirm", null, PaymentResponse.class, payment.paymentId());
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody().status()).isEqualTo("CONFIRMED");

        // Duplicate confirm → idempotent
        ResponseEntity<PaymentResponse> second = rest.postForEntity(
                "/mock-payment/{id}/confirm", null, PaymentResponse.class, payment.paymentId());
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(second.getBody().status()).isEqualTo("CONFIRMED");
    }

    @Test
    void shouldHandlePaymentFailureAndRetry() {
        // Create cart
        CartResponse cart = rest.postForObject("/carts", null, CartResponse.class);
        assertThat(cart).isNotNull();

        // Add item
        CartItemRequest item = new CartItemRequest("prod-003", 3);
        rest.postForObject("/carts/{id}/items", item, CartResponse.class, cart.id());

        // Checkout
        OrderResponse order = rest.postForObject("/carts/{id}/checkout", null, OrderResponse.class, cart.id());
        assertThat(order).isNotNull();

        // First payment → fail
        PaymentStartRequest payReq1 = new PaymentStartRequest(UUID.randomUUID().toString());
        PaymentStartResponse payment1 = rest.postForObject(
                "/orders/{id}/payment/start", payReq1, PaymentStartResponse.class, order.id());
        assertThat(payment1).isNotNull();

        ResponseEntity<PaymentResponse> failResp = rest.postForEntity(
                "/mock-payment/{id}/fail", null, PaymentResponse.class, payment1.paymentId());
        assertThat(failResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(failResp.getBody().status()).isEqualTo("FAILED");

        // Retry with NEW idempotency key (new payment attempt)
        PaymentStartRequest payReq2 = new PaymentStartRequest(UUID.randomUUID().toString());
        PaymentStartResponse payment2 = rest.postForObject(
                "/orders/{id}/payment/start", payReq2, PaymentStartResponse.class, order.id());
        assertThat(payment2).isNotNull();

        ResponseEntity<PaymentResponse> confirmResp = rest.postForEntity(
                "/mock-payment/{id}/confirm", null, PaymentResponse.class, payment2.paymentId());
        assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResp.getBody().status()).isEqualTo("CONFIRMED");

        // Demonstrate safe retry — sending same key returns cached response
        PaymentStartResponse cachedResponse = rest.postForObject(
                "/orders/{id}/payment/start", payReq2, PaymentStartResponse.class, order.id());
        assertThat(cachedResponse).isNotNull();
        assertThat(cachedResponse.paymentId()).isEqualTo(payment2.paymentId());
        assertThat(cachedResponse.idempotencyKey()).isEqualTo(payReq2.idempotencyKey());
    }
}
