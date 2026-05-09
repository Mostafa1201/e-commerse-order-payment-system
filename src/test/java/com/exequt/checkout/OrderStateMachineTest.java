package com.exequt.checkout;

import com.exequt.checkout.exception.IllegalStateTransitionException;
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

    @Test
    @DisplayName("CREATED → PENDING_PAYMENT on startPayment()")
    void startPayment_fromCreated_transitions() {
        Order order = createNewOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        order.startPayment();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("PENDING_PAYMENT → PAID on markPaid()")
    void markPaid_fromPendingPayment_transitions() {
        Order order = createNewOrder();
        order.startPayment();
        order.markPaid();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.isPaid()).isTrue();
    }

    @Test
    @DisplayName("PENDING_PAYMENT → PAYMENT_FAILED on markFailed()")
    void markFailed_fromPendingPayment_transitions() {
        Order order = createNewOrder();
        order.startPayment();
        order.markFailed();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("PAYMENT_FAILED → PENDING_PAYMENT on startPayment() — retry flow")
    void startPayment_fromPaymentFailed_allowsRetry() {
        Order order = createNewOrder();
        order.startPayment();
        order.markFailed();
        order.startPayment();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("PAID → startPayment() throws")
    void startPayment_fromPaid_throws() {
        Order order = createNewOrder();
        order.startPayment();
        order.markPaid();

        assertThatThrownBy(order::startPayment)
            .isInstanceOf(IllegalStateTransitionException.class)
            .hasMessageContaining("PAID");
    }

    @Test
    @DisplayName("PAID → markFailed() throws")
    void markFailed_fromPaid_throws() {
        Order order = createNewOrder();
        order.startPayment();
        order.markPaid();

        assertThatThrownBy(order::markFailed)
            .isInstanceOf(IllegalStateTransitionException.class);
    }

    @Test
    @DisplayName("CREATED → markPaid() throws — must go through PENDING_PAYMENT first")
    void markPaid_fromCreated_throws() {
        Order order = createNewOrder();

        assertThatThrownBy(order::markPaid)
            .isInstanceOf(IllegalStateTransitionException.class)
            .hasMessageContaining("CREATED");
    }

    @Test
    @DisplayName("PAID → markPaid() throws — no double-paid")
    void markPaid_fromPaid_throws() {
        Order order = createNewOrder();
        order.startPayment();
        order.markPaid();

        assertThatThrownBy(order::markPaid)
            .isInstanceOf(IllegalStateTransitionException.class)
            .hasMessageContaining("PAID");
    }
}
