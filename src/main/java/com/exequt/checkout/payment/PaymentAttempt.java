package com.exequt.checkout.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "payment_attempts",
        indexes = @Index(name = "idx_external_payment_id", columnList = "externalPaymentId"),
        uniqueConstraints = @UniqueConstraint(name = "uk_external_payment_id",
                columnNames = "externalPaymentId"))
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, unique = true)
    private String externalPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentAttemptStatus status = PaymentAttemptStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant processedAt;

    public UUID getId() { return id; }

    public UUID getOrderId() { return orderId; }

    public String getExternalPaymentId() { return externalPaymentId; }

    public PaymentAttemptStatus getStatus() { return status; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getProcessedAt() { return processedAt; }

    public static PaymentAttempt create(UUID orderId, String externalPaymentId) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.orderId = orderId;
        attempt.externalPaymentId = externalPaymentId;
        attempt.status = PaymentAttemptStatus.PENDING;
        return attempt;
    }

    public void markConfirmed() {
        this.status = PaymentAttemptStatus.CONFIRMED;
        this.processedAt = Instant.now();
    }

    public void markFailed() {
        this.status = PaymentAttemptStatus.FAILED;
        this.processedAt = Instant.now();
    }

    public boolean isAlreadyProcessed() {
        return status == PaymentAttemptStatus.CONFIRMED
                || status == PaymentAttemptStatus.FAILED;
    }
}
