package com.exequt.ecommerce.infrastructure.web;

import com.exequt.ecommerce.application.dto.CartItemRequest;
import com.exequt.ecommerce.application.dto.CartResponse;
import com.exequt.ecommerce.application.dto.OrderResponse;
import com.exequt.ecommerce.application.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@Tag(name = "Cart", description = "Cart management endpoints — create, add items, checkout")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Create a new cart", description = "Creates an empty, unlocked shopping cart with a unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cart created",
                    content = @Content(schema = @Schema(implementation = CartResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse createCart() {
        return cartService.createCart();
    }

    @Operation(summary = "Get cart details", description = "Retrieves a cart by its ID, including items and total amount.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart found"),
            @ApiResponse(responseCode = "404", description = "Cart not found",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{cartId}")
    public CartResponse getCart(
            @Parameter(description = "Cart ID", required = true, example = "a1b2c3d4")
            @PathVariable String cartId) {
        return cartService.getCart(cartId);
    }

    @Operation(summary = "Add item to cart", description = "Adds a product item to the cart. Cart must not be locked.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added"),
            @ApiResponse(responseCode = "400", description = "Validation error / Cart locked"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @PostMapping("/{cartId}/items")
    public CartResponse addItem(
            @Parameter(description = "Cart ID", required = true, example = "a1b2c3d4")
            @PathVariable String cartId,
            @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(cartId, request);
    }

    @Operation(summary = "Checkout cart", description = "Locks the cart, creates an Order in CREATED state, and returns the order.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cart is empty"),
            @ApiResponse(responseCode = "409", description = "Cart already locked"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @PostMapping("/{cartId}/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(
            @Parameter(description = "Cart ID", required = true, example = "a1b2c3d4")
            @PathVariable String cartId) {
        return cartService.checkout(cartId);
    }
}
