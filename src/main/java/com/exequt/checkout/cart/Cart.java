package com.exequt.checkout.cart;

import com.exequt.checkout.exception.IllegalStateTransitionException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status = CartStatus.OPEN;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public UUID getId() { return id; }
    public CartStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<CartItem> getItems() { return items; }

    public void addItem(String productId, int quantity, BigDecimal price) {
        if (status != CartStatus.OPEN) {
            throw new IllegalStateTransitionException(
                "Cannot add items to a cart that is " + status);
        }
        CartItem item = new CartItem();
        item.setCart(this);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);
        items.add(item);
    }

    public BigDecimal calculateTotal() {
        return items.stream()
            .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isOpen() { return status == CartStatus.OPEN; }

    public void checkout() {
        if (status != CartStatus.OPEN) {
            throw new IllegalStateTransitionException("Cart is already " + status);
        }
        this.status = CartStatus.CHECKED_OUT;
    }
}
