package com.exequt.checkout.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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
}
