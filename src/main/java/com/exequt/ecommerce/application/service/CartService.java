package com.exequt.ecommerce.application.service;

import com.exequt.ecommerce.application.dto.CartItemRequest;
import com.exequt.ecommerce.application.dto.CartResponse;
import com.exequt.ecommerce.application.dto.OrderResponse;
import com.exequt.ecommerce.application.exception.CartEmptyException;
import com.exequt.ecommerce.application.exception.CartLockedException;
import com.exequt.ecommerce.application.exception.ProductNotFoundException;
import com.exequt.ecommerce.application.exception.ProductUnavailableException;
import com.exequt.ecommerce.application.exception.ResourceNotFoundException;
import com.exequt.ecommerce.domain.model.Cart;
import com.exequt.ecommerce.domain.model.CartItem;
import com.exequt.ecommerce.domain.model.Order;
import com.exequt.ecommerce.domain.model.Product;
import com.exequt.ecommerce.domain.repository.CartRepository;
import com.exequt.ecommerce.domain.repository.OrderRepository;
import com.exequt.ecommerce.domain.repository.ProductRepository;
import com.exequt.ecommerce.infrastructure.persistence.CartMapper;
import com.exequt.ecommerce.infrastructure.persistence.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;

    public CartResponse createCart() {
        Cart cart = Cart.create();
        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    public CartResponse addItem(String cartId, CartItemRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        if (!product.isAvailable()) {
            throw new ProductUnavailableException(request.productId());
        }

        CartItem item = new CartItem(product.getId(), request.quantity(), product.getPrice());
        cart.addItem(item);
        cart = cartRepository.save(cart);
        return cartMapper.toResponse(cart);
    }

    public CartResponse getCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));
        return cartMapper.toResponse(cart);
    }

    public OrderResponse checkout(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));

        if (cart.isLocked()) {
            throw new CartLockedException(cartId);
        }
        if (cart.isEmpty()) {
            throw new CartEmptyException(cartId);
        }

        cart.lock();
        cartRepository.save(cart);

        Order order = Order.createFromCart(cart);
        order = orderRepository.save(order);

        return orderMapper.toResponse(order);
    }
}
