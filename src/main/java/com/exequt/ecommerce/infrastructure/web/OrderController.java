package com.exequt.ecommerce.infrastructure.web;

import com.exequt.ecommerce.application.dto.PaymentStartRequest;
import com.exequt.ecommerce.application.dto.PaymentStartResponse;
import com.exequt.ecommerce.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order / Payment", description = "Order payment initiation")
public class OrderController {

    private final PaymentService paymentService;

    public OrderController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Start payment", description = """
            Initiates a payment for the order. The client must provide an idempotency key.
            Sending the same key again returns the cached response (safe retry).

            - Order state transitions to PENDING_PAYMENT
            - Only one pending payment allowed per order at a time
            - Mock provider is notified asynchronously
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment initiated",
                    content = @Content(schema = @Schema(implementation = PaymentStartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate active payment / Invalid state transition")
    })
    @PostMapping("/{orderId}/payment/start")
    public PaymentStartResponse startPayment(
            @Parameter(description = "Order ID", required = true, example = "e5f6g7h8")
            @PathVariable String orderId,
            @Valid @RequestBody PaymentStartRequest request) {
        return paymentService.startPayment(orderId, request);
    }
}
