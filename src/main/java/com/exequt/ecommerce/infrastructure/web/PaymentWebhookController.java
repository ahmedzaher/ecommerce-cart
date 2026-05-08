package com.exequt.ecommerce.infrastructure.web;

import com.exequt.ecommerce.application.dto.PaymentResponse;
import com.exequt.ecommerce.application.dto.WebhookRequest;
import com.exequt.ecommerce.application.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Webhook", description = "Webhook endpoint — receives payment results from the provider")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Receive payment webhook", description = """
            Called by the payment provider when a payment is CONFIRMED or FAILED.
                                                                
            **Idempotency:** If the payment is already in the target state (CONFIRMED/FAILED),
            the request is a no-op. Duplicate webhooks for the same event are safe.
                                                                
            Events:
            - `CONFIRMED` — Payment succeeded → Order transitions to PAID
            - `FAILED` — Payment failed → Order transitions to PAYMENT_FAILED
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook processed",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payment is not in PENDING state"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/webhook")
    public PaymentResponse handleWebhook(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Webhook payload from payment provider",
                    required = true,
                    content = @Content(schema = @Schema(implementation = WebhookRequest.class)))
            @RequestBody WebhookRequest request) {
        return paymentService.handleWebhook(request);
    }
}
