package com.exequt.checkout.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrderDtos {

    public static class OrderResponse {
        private UUID id;
        private UUID cartId;
        private String status;
        private BigDecimal totalAmount;
        private Instant createdAt;
        private Instant updatedAt;

        public UUID getId() { return id; }
        public UUID getCartId() { return cartId; }
        public String getStatus() { return status; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getUpdatedAt() { return updatedAt; }

        public static OrderResponse from(Order order) {
            OrderResponse r = new OrderResponse();
            r.id = order.getId();
            r.cartId = order.getCartId();
            r.status = order.getStatus().name();
            r.totalAmount = order.getTotalAmount();
            r.createdAt = order.getCreatedAt();
            r.updatedAt = order.getUpdatedAt();
            return r;
        }
    }
}
