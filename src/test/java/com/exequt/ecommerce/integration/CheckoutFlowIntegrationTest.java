package com.exequt.ecommerce.integration;

import com.exequt.ecommerce.application.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.webhook.base-url=http://localhost:${local.server.port}")
class CheckoutFlowIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void shouldCompleteHappyPathFromCartToPaid() {
        // Create cart
        ResponseEntity<CartResponse> createResp = rest.postForEntity("/carts", null, CartResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CartResponse cart = createResp.getBody();
        assertThat(cart).isNotNull();
        assertThat(cart.locked()).isFalse();

        // Add item (server looks up price from product catalog)
        CartItemRequest item = new CartItemRequest("prod-001", 2);
        ResponseEntity<CartResponse> addResp = rest.postForEntity(
                "/carts/{cartId}/items", item, CartResponse.class, cart.id());
        assertThat(addResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(addResp.getBody().items()).hasSize(1);
        assertThat(addResp.getBody().totalAmount()).isEqualByComparingTo(new BigDecimal("59.98"));

        // Checkout
        ResponseEntity<OrderResponse> checkoutResp = rest.postForEntity(
                "/carts/{cartId}/checkout", null, OrderResponse.class, cart.id());
        assertThat(checkoutResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OrderResponse order = checkoutResp.getBody();
        assertThat(order).isNotNull();
        assertThat(order.state()).isEqualTo("CREATED");

        // Start payment — client provides idempotency key
        PaymentStartRequest payReq = new PaymentStartRequest(UUID.randomUUID().toString());
        ResponseEntity<PaymentStartResponse> paymentResp = rest.postForEntity(
                "/orders/{orderId}/payment/start", payReq, PaymentStartResponse.class, order.id());
        assertThat(paymentResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        PaymentStartResponse payment = paymentResp.getBody();
        assertThat(payment).isNotNull();
        assertThat(payment.paymentId()).isNotNull();
        assertThat(payment.status()).isEqualTo("PENDING");
        assertThat(payment.idempotencyKey()).isEqualTo(payReq.idempotencyKey());

        // Confirm payment via mock → HTTP POST /payments/webhook
        ResponseEntity<PaymentResponse> confirmResp = rest.postForEntity(
                "/mock-payment/{paymentId}/confirm", null, PaymentResponse.class, payment.paymentId());
        assertThat(confirmResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmResp.getBody().status()).isEqualTo("CONFIRMED");
    }
}
