package com.exequt.checkout.payment;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/{orderId}/start")
    public ResponseEntity<PaymentDtos.PaymentResponse> startPayment(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.startPayment(orderId));
    }

    @PostMapping("/payments/webhook")
    public ResponseEntity<PaymentDtos.PaymentResponse> webhook(
            @Valid @RequestBody PaymentDtos.WebhookPayload payload) {
        return ResponseEntity.ok(paymentService.handleWebhook(payload));
    }
}
