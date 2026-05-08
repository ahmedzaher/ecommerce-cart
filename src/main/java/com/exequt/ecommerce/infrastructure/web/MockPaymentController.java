package com.exequt.ecommerce.infrastructure.web;

import com.exequt.ecommerce.application.dto.PaymentResponse;
import com.exequt.ecommerce.application.dto.WebhookRequest;
import com.exequt.ecommerce.application.exception.ResourceNotFoundException;
import com.exequt.ecommerce.domain.model.Payment;
import com.exequt.ecommerce.domain.repository.PaymentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/mock-payment")
@Tag(name = "Mock Payment Provider", description = "Mock provider — trigger payment results for testing")
public class MockPaymentController {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final Environment environment;

    public MockPaymentController(PaymentRepository paymentRepository, RestTemplate restTemplate,
                                 Environment environment) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    @Operation(summary = "Mock: confirm payment", description = "Simulates a CONFIRMED webhook by calling POST /payments/webhook via HTTP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment confirmed",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class)))
    })
    @PostMapping("/{paymentId}/confirm")
    public PaymentResponse confirmPayment(
            @Parameter(description = "Payment ID to confirm", required = true, example = "p123")
            @PathVariable String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        return sendWebhook(payment, "CONFIRMED");
    }

    @Operation(summary = "Mock: fail payment", description = "Simulates a FAILED webhook by calling POST /payments/webhook via HTTP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment failed",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class)))
    })
    @PostMapping("/{paymentId}/fail")
    public PaymentResponse failPayment(
            @Parameter(description = "Payment ID to fail", required = true, example = "p123")
            @PathVariable String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        return sendWebhook(payment, "FAILED");
    }

    private PaymentResponse sendWebhook(Payment payment, String event) {
        WebhookRequest webhook = new WebhookRequest(payment.getId(), event, payment.getIdempotencyKey());
        String baseUrl = environment.resolveRequiredPlaceholders("${app.webhook.base-url}");
        String url = baseUrl + "/payments/webhook";
        ResponseEntity<PaymentResponse> response = restTemplate.postForEntity(url, webhook, PaymentResponse.class);
        return response.getBody();
    }

    @Operation(summary = "Mock: check payment status", description = "Returns a simulated response as if the payment is being processed.")
    @GetMapping("/{paymentId}")
    public Map<String, String> simulateProviderResponse(
            @Parameter(description = "Payment ID", required = true, example = "p123")
            @PathVariable String paymentId) {
        return Map.of(
                "paymentId", paymentId,
                "message", "Payment is being processed by mock provider"
        );
    }
}
