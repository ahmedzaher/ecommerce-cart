package com.exequt.ecommerce.domain;

import com.exequt.ecommerce.domain.model.InvalidStateTransitionException;
import com.exequt.ecommerce.domain.model.OrderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateTransitionTest {

    @ParameterizedTest
    @MethodSource("validTransitions")
    void shouldAllowValidTransition(OrderState from, OrderState to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void shouldRejectInvalidTransition(OrderState from, OrderState to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenTransitionIsInvalid() {
        var order = com.exequt.ecommerce.domain.model.Order.createFromCart(
                createCartWithItems());

        assertThat(order.getState()).isEqualTo(OrderState.CREATED);

        assertThatThrownBy(() -> order.transitionTo(OrderState.PAID))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("CREATED -> PAID");
    }

    @Test
    void shouldTransitionThroughHappyPath() {
        var order = com.exequt.ecommerce.domain.model.Order.createFromCart(
                createCartWithItems());

        assertThat(order.getState()).isEqualTo(OrderState.CREATED);

        order.transitionTo(OrderState.PENDING_PAYMENT);
        assertThat(order.getState()).isEqualTo(OrderState.PENDING_PAYMENT);

        order.transitionTo(OrderState.PAID);
        assertThat(order.getState()).isEqualTo(OrderState.PAID);
    }

    @Test
    void shouldSupportPaymentFailedRetry() {
        var order = com.exequt.ecommerce.domain.model.Order.createFromCart(
                createCartWithItems());

        order.transitionTo(OrderState.PENDING_PAYMENT);
        order.transitionTo(OrderState.PAYMENT_FAILED);
        assertThat(order.getState()).isEqualTo(OrderState.PAYMENT_FAILED);

        order.transitionTo(OrderState.PENDING_PAYMENT);
        order.transitionTo(OrderState.PAID);
        assertThat(order.getState()).isEqualTo(OrderState.PAID);
    }

    @Test
    void shouldSupportCancelFromCreated() {
        var order = com.exequt.ecommerce.domain.model.Order.createFromCart(
                createCartWithItems());

        order.transitionTo(OrderState.CANCELLED);
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
    }

    @Test
    void shouldSupportCancelFromPaymentFailed() {
        var order = com.exequt.ecommerce.domain.model.Order.createFromCart(
                createCartWithItems());

        order.transitionTo(OrderState.PENDING_PAYMENT);
        order.transitionTo(OrderState.PAYMENT_FAILED);
        order.transitionTo(OrderState.CANCELLED);
        assertThat(order.getState()).isEqualTo(OrderState.CANCELLED);
    }

    @Test
    void shouldNotAllowTransitionFromPaid() {
        var order = com.exequt.ecommerce.domain.model.Order.createFromCart(
                createCartWithItems());

        order.transitionTo(OrderState.PENDING_PAYMENT);
        order.transitionTo(OrderState.PAID);

        for (OrderState state : OrderState.values()) {
            assertThat(order.getState().canTransitionTo(state))
                    .as("PAID -> " + state)
                    .isFalse();
        }
    }

    @Test
    void shouldNotAllowTransitionFromCancelled() {
        var order = com.exequt.ecommerce.domain.model.Order.createFromCart(
                createCartWithItems());

        order.transitionTo(OrderState.CANCELLED);

        for (OrderState state : OrderState.values()) {
            assertThat(order.getState().canTransitionTo(state))
                    .as("CANCELLED -> " + state)
                    .isFalse();
        }
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                Arguments.of(OrderState.CREATED, OrderState.PENDING_PAYMENT),
                Arguments.of(OrderState.CREATED, OrderState.CANCELLED),
                Arguments.of(OrderState.PENDING_PAYMENT, OrderState.PAID),
                Arguments.of(OrderState.PENDING_PAYMENT, OrderState.PAYMENT_FAILED),
                Arguments.of(OrderState.PAYMENT_FAILED, OrderState.PENDING_PAYMENT),
                Arguments.of(OrderState.PAYMENT_FAILED, OrderState.CANCELLED)
        );
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                Arguments.of(OrderState.CREATED, OrderState.CREATED),
                Arguments.of(OrderState.CREATED, OrderState.PAID),
                Arguments.of(OrderState.CREATED, OrderState.PAYMENT_FAILED),
                Arguments.of(OrderState.PENDING_PAYMENT, OrderState.CREATED),
                Arguments.of(OrderState.PENDING_PAYMENT, OrderState.CANCELLED),
                Arguments.of(OrderState.PAID, OrderState.CREATED),
                Arguments.of(OrderState.PAID, OrderState.PENDING_PAYMENT),
                Arguments.of(OrderState.PAID, OrderState.PAYMENT_FAILED),
                Arguments.of(OrderState.PAID, OrderState.CANCELLED),
                Arguments.of(OrderState.CANCELLED, OrderState.CREATED),
                Arguments.of(OrderState.CANCELLED, OrderState.PENDING_PAYMENT),
                Arguments.of(OrderState.CANCELLED, OrderState.PAYMENT_FAILED),
                Arguments.of(OrderState.CANCELLED, OrderState.PAID),
                Arguments.of(OrderState.PAYMENT_FAILED, OrderState.CREATED),
                Arguments.of(OrderState.PAYMENT_FAILED, OrderState.PAID)
        );
    }

    private com.exequt.ecommerce.domain.model.Cart createCartWithItems() {
        var cart = com.exequt.ecommerce.domain.model.Cart.create();
        cart.addItem(new com.exequt.ecommerce.domain.model.CartItem("prod-1", 2,
                java.math.BigDecimal.valueOf(100)));
        return cart;
    }
}
