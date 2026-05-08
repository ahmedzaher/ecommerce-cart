package com.exequt.ecommerce.application;

import com.exequt.ecommerce.application.dto.CartItemRequest;
import com.exequt.ecommerce.application.dto.CartResponse;
import com.exequt.ecommerce.application.dto.OrderResponse;
import com.exequt.ecommerce.application.exception.CartEmptyException;
import com.exequt.ecommerce.application.exception.CartLockedException;
import com.exequt.ecommerce.application.exception.ProductNotFoundException;
import com.exequt.ecommerce.application.exception.ProductUnavailableException;
import com.exequt.ecommerce.application.exception.ResourceNotFoundException;
import com.exequt.ecommerce.application.service.CartService;
import com.exequt.ecommerce.domain.model.Cart;
import com.exequt.ecommerce.domain.model.CartItem;
import com.exequt.ecommerce.domain.model.Order;
import com.exequt.ecommerce.domain.model.OrderState;
import com.exequt.ecommerce.domain.model.Product;
import com.exequt.ecommerce.domain.repository.CartRepository;
import com.exequt.ecommerce.domain.repository.OrderRepository;
import com.exequt.ecommerce.domain.repository.ProductRepository;
import com.exequt.ecommerce.infrastructure.persistence.CartMapperImpl;
import com.exequt.ecommerce.infrastructure.persistence.OrderMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    private CartService cartService;

    private Cart cart;
    private Product availableProduct;
    private Product unavailableProduct;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartRepository, orderRepository, productRepository,
                new CartMapperImpl(), new OrderMapperImpl());
        cart = Cart.create();
        availableProduct = Product.create("prod-1", "Wireless Mouse", new BigDecimal("29.99"), true);
        unavailableProduct = Product.create("prod-2", "Broken Item", new BigDecimal("9.99"), false);
    }

    @Test
    void shouldCreateCart() {
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.createCart();

        assertThat(response.id()).isEqualTo(cart.getId());
        assertThat(response.locked()).isFalse();
        assertThat(response.items()).isEmpty();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void shouldAddItemToCart() {
        cart.addItem(new CartItem("prod-0", 2, BigDecimal.valueOf(50)));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(availableProduct));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartItemRequest request = new CartItemRequest("prod-1", 3);
        CartResponse response = cartService.addItem(cart.getId(), request);

        assertThat(response.items()).hasSize(2);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById("unknown")).thenReturn(Optional.empty());

        CartItemRequest request = new CartItemRequest("unknown", 1);

        assertThatThrownBy(() -> cartService.addItem(cart.getId(), request))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldThrowWhenProductUnavailable() {
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById("prod-2")).thenReturn(Optional.of(unavailableProduct));

        CartItemRequest request = new CartItemRequest("prod-2", 1);

        assertThatThrownBy(() -> cartService.addItem(cart.getId(), request))
                .isInstanceOf(ProductUnavailableException.class);
    }

    @Test
    void shouldThrowWhenCartNotFound() {
        when(cartRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenCartIsLocked() {
        cart.lock();
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.checkout(cart.getId()))
                .isInstanceOf(CartLockedException.class);
    }

    @Test
    void shouldThrowWhenCartIsEmpty() {
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.checkout(cart.getId()))
                .isInstanceOf(CartEmptyException.class);
    }

    @Test
    void shouldCheckoutSuccessfully() {
        cart.addItem(new CartItem("prod-1", 2, BigDecimal.valueOf(100)));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = cartService.checkout(cart.getId());

        assertThat(response.state()).isEqualTo(OrderState.CREATED.name());
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(cart.isLocked()).isTrue();
    }

    @Test
    void shouldLockCartOnCheckout() {
        cart.addItem(new CartItem("prod-1", 1, BigDecimal.TEN));
        when(cartRepository.findById(cart.getId())).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.checkout(cart.getId());

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository, atLeastOnce()).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue().isLocked()).isTrue();
    }
}
