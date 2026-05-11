package com.exequt.checkout.integration;

import com.exequt.checkout.cart.CartDtos;
import com.exequt.checkout.cart.CartService;
import com.exequt.checkout.order.OrderDtos;
import com.exequt.checkout.order.OrderService;
import com.exequt.checkout.order.OrderStatus;
import com.exequt.checkout.payment.PaymentAttemptRepository;
import com.exequt.checkout.payment.PaymentAttemptStatus;
import com.exequt.checkout.payment.PaymentDtos;
import com.exequt.checkout.payment.PaymentService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@DisplayName("Full flow integration tests")
class CheckoutIntegrationTest {
    @Autowired
    CartService cartService;
    @Autowired
    OrderService orderService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentAttemptRepository paymentAttemptRepository;

    @Test
    @DisplayName("Happy path: cart → checkout → pay → PAID")
    void happyPath_orderBecomesPaid() {
        CartDtos.CartResponse cart = cartService.createCart();
        CartDtos.AddItemRequest item = new CartDtos.AddItemRequest();
        item.setProductId("prod-001");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("49.99"));
        cartService.addItem(cart.getId(), item);

        OrderDtos.OrderResponse order = orderService.checkout(cart.getId());
        assertThat(order.getStatus()).isEqualTo("CREATED");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("99.98");

        PaymentDtos.PaymentResponse payment = paymentService.startPayment(order.getId());
        assertThat(payment.getOrderStatus()).isEqualTo("PENDING_PAYMENT");

        PaymentDtos.WebhookPayload webhook = new PaymentDtos.WebhookPayload();
        webhook.setExternalPaymentId(payment.getExternalPaymentId());
        webhook.setResult(PaymentDtos.WebhookResult.CONFIRMED);

        PaymentDtos.PaymentResponse result = paymentService.handleWebhook(webhook);
        assertThat(result.getOrderStatus()).isEqualTo("PAID");
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");

        OrderDtos.OrderResponse finalOrder = orderService.getOrder(order.getId());
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PAID.name());
    }

    @Test
    @DisplayName("Duplicate webhook is ignored — order stays PAID, no error")
    void duplicateWebhook_isIdempotent() {
        CartDtos.CartResponse cart = cartService.createCart();
        CartDtos.AddItemRequest item = new CartDtos.AddItemRequest();
        item.setProductId("prod-002");
        item.setQuantity(1);
        item.setPrice(new BigDecimal("25.00"));
        cartService.addItem(cart.getId(), item);

        OrderDtos.OrderResponse order = orderService.checkout(cart.getId());
        PaymentDtos.PaymentResponse payment = paymentService.startPayment(order.getId());

        PaymentDtos.WebhookPayload webhook = new PaymentDtos.WebhookPayload();
        webhook.setExternalPaymentId(payment.getExternalPaymentId());
        webhook.setResult(PaymentDtos.WebhookResult.CONFIRMED);

        // First webhook
        PaymentDtos.PaymentResponse first = paymentService.handleWebhook(webhook);
        assertThat(first.getOrderStatus()).isEqualTo("PAID");

        // Second (duplicate) webhook must NOT throw, must return same result
        PaymentDtos.PaymentResponse second = paymentService.handleWebhook(webhook);
        assertThat(second.getOrderStatus()).isEqualTo("PAID");
        assertThat(second.getStatus()).isEqualTo("CONFIRMED");

        // Attempt is still CONFIRMED not double-processed
        var attempt = paymentAttemptRepository
                .findByExternalPaymentId(payment.getExternalPaymentId());
        assertThat(attempt).isPresent();
        assertThat(attempt.get().getStatus()).isEqualTo(PaymentAttemptStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Payment failure + retry leads to PAID")
    void failureThenRetry_orderBecomesPaid() {
        CartDtos.CartResponse cart = cartService.createCart();
        CartDtos.AddItemRequest item = new CartDtos.AddItemRequest();
        item.setProductId("prod-003");
        item.setQuantity(3);
        item.setPrice(new BigDecimal("10.00"));
        cartService.addItem(cart.getId(), item);
        OrderDtos.OrderResponse order = orderService.checkout(cart.getId());

        // First payment attempt — fails
        PaymentDtos.PaymentResponse attempt1 = paymentService.startPayment(order.getId());
        PaymentDtos.WebhookPayload failWebhook = new PaymentDtos.WebhookPayload();
        failWebhook.setExternalPaymentId(attempt1.getExternalPaymentId());
        failWebhook.setResult(PaymentDtos.WebhookResult.FAILED);

        PaymentDtos.PaymentResponse failResult = paymentService.handleWebhook(failWebhook);
        assertThat(failResult.getOrderStatus()).isEqualTo("PAYMENT_FAILED");

        // Retry — second payment attempt
        PaymentDtos.PaymentResponse attempt2 = paymentService.startPayment(order.getId());
        assertThat(attempt2.getOrderStatus()).isEqualTo("PENDING_PAYMENT");

        // Second attempt succeeds
        PaymentDtos.WebhookPayload confirmWebhook = new PaymentDtos.WebhookPayload();
        confirmWebhook.setExternalPaymentId(attempt2.getExternalPaymentId());
        confirmWebhook.setResult(PaymentDtos.WebhookResult.CONFIRMED);

        PaymentDtos.PaymentResponse finalResult = paymentService.handleWebhook(confirmWebhook);
        assertThat(finalResult.getOrderStatus()).isEqualTo("PAID");

        OrderDtos.OrderResponse finalOrder = orderService.getOrder(order.getId());
        assertThat(finalOrder.getStatus()).isEqualTo("PAID");
    }
}
