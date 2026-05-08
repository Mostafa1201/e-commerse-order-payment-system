package com.exequt.checkout;

import com.exequt.checkout.order.Order;
import com.exequt.checkout.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order state machine")
public class OrderStateMachineTest {
    private Order createNewOrder() {
        return Order.createFromCart(UUID.randomUUID(), new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Initial status is CREATED")
    void newOrder_hasCreatedStatus() {
        Order order = createNewOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.isPaid()).isFalse();
    }
}
