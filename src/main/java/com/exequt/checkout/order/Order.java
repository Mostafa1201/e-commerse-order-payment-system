package com.exequt.checkout.order;

import com.exequt.checkout.exception.IllegalStateTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID cartId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // Used for optimistic locking
    @Version
    private Long version;

    public UUID getId() { return id; }
    public UUID getCartId() { return cartId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Static Factory for order creation
    public static Order createFromCart(UUID cartId, BigDecimal totalAmount) {
        Order order = new Order();
        order.cartId = cartId;
        order.totalAmount = totalAmount;
        order.status = OrderStatus.CREATED;
        return order;
    }

    public void startPayment() {
        if (status != OrderStatus.CREATED && status != OrderStatus.PAYMENT_FAILED) {
            throw new IllegalStateTransitionException(
                "Cannot start payment from state [" + status + "]. " +
                    "Allowed: CREATED, PAYMENT_FAILED");
        }
        this.status = OrderStatus.PENDING_PAYMENT;
        this.updatedAt = Instant.now();
    }

    public void markPaid() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateTransitionException(
                "Cannot mark PAID from state [" + status + "]. " +
                    "Allowed: PENDING_PAYMENT");
        }
        this.status = OrderStatus.PAID;
        this.updatedAt = Instant.now();
    }

    public void markFailed() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateTransitionException(
                "Cannot mark PAYMENT_FAILED from state [" + status + "]. " +
                    "Allowed: PENDING_PAYMENT");
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        this.updatedAt = Instant.now();
    }

    public boolean isPaid() { return status == OrderStatus.PAID; }

}
