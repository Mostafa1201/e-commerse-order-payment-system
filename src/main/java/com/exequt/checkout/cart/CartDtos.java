package com.exequt.checkout.cart;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartDtos {

    public static class CartItemResponse {
        private UUID id;
        private String productId;
        private int quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;

        public UUID getId() { return id; }
        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
        public BigDecimal getTotalPrice() { return totalPrice; }

        public static CartItemResponse from(CartItem item) {
            CartItemResponse r = new CartItemResponse();
            r.id = item.getId();
            r.productId = item.getProductId();
            r.quantity = item.getQuantity();
            r.price = item.getPrice();
            r.totalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return r;
        }
    }

    public static class CartResponse {
        private UUID id;
        private String status;
        private List<CartItemResponse> items;
        private BigDecimal total;

        public UUID getId() { return id; }
        public String getStatus() { return status; }
        public List<CartItemResponse> getItems() { return items; }
        public BigDecimal getTotal() { return total; }

        public static CartResponse from(Cart cart) {
            CartResponse r = new CartResponse();
            r.id = cart.getId();
            r.status = cart.getStatus().name();
            r.items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
            r.total = cart.calculateTotal();
            return r;
        }
    }

    public static class AddItemRequest {
        @NotBlank(message = "productId is required")
        private String productId;

        @Min(value = 1, message = "quantity must be at least 1")
        private int quantity;

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.01", message = "price must be positive")
        private BigDecimal price;

        public String getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public BigDecimal getPrice() { return price; }
        public void setProductId(String productId) { this.productId = productId; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
}
