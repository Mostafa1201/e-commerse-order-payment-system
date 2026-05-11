package com.exequt.checkout.payment;

public class PaymentInitiationResponse {
    private final String externalPaymentId;
    private final String status;

    public PaymentInitiationResponse(String externalPaymentId, String status) {
        this.externalPaymentId = externalPaymentId;
        this.status = status;
    }

    public String getExternalPaymentId() { return externalPaymentId; }
    public String getStatus() { return status; }
}
