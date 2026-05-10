package com.exequt.checkout.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public class PaymentDtos {

    public static class PaymentResponse {

        private UUID paymentAttemptId;
        private UUID orderId;
        private String externalPaymentId;
        private String status;
        private String orderStatus;
        private Instant createdAt;

        public UUID getPaymentAttemptId() { return paymentAttemptId; }

        public UUID getOrderId() { return orderId; }

        public String getExternalPaymentId() { return externalPaymentId; }

        public String getStatus() { return status; }

        public String getOrderStatus() { return orderStatus; }

        public Instant getCreatedAt() { return createdAt; }

        public static PaymentResponse from(PaymentAttempt attempt, String orderStatus) {
            PaymentResponse r = new PaymentResponse();
            r.paymentAttemptId = attempt.getId();
            r.orderId = attempt.getOrderId();
            r.externalPaymentId = attempt.getExternalPaymentId();
            r.status = attempt.getStatus().name();
            r.orderStatus = orderStatus;
            r.createdAt = attempt.getCreatedAt();
            return r;
        }
    }

    public static class WebhookPayload {

        @NotBlank(message = "externalPaymentId is required")
        private String externalPaymentId;

        @NotNull(message = "result is required")
        private WebhookResult result;

        public String getExternalPaymentId() { return externalPaymentId; }

        public WebhookResult getResult() { return result; }

        public void setExternalPaymentId(
                String externalPaymentId) { this.externalPaymentId = externalPaymentId; }

        public void setResult(WebhookResult result) { this.result = result; }
    }

    public enum WebhookResult {
        CONFIRMED,
        FAILED
    }
}
