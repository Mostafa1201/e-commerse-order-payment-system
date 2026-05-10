package com.exequt.checkout.payment;

import com.exequt.checkout.exception.ConflictException;
import com.exequt.checkout.exception.NotFoundException;
import com.exequt.checkout.order.Order;
import com.exequt.checkout.order.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final OrderRepository orderRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;

    public PaymentService(OrderRepository orderRepository, PaymentAttemptRepository paymentAttemptRepository) {
        this.orderRepository = orderRepository;
        this.paymentAttemptRepository = paymentAttemptRepository;
    }

    private Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    @Transactional
    public PaymentDtos.PaymentResponse startPayment(UUID orderId) {
        Order order = findOrderOrThrow(orderId);
        boolean activePaymentExists = paymentAttemptRepository.existsByOrderIdAndStatusIn(
            orderId, List.of(PaymentAttemptStatus.CONFIRMED, PaymentAttemptStatus.FAILED));
        if (activePaymentExists) {
            throw new ConflictException("An active payment already exists for order: " + orderId);
        }
        order.startPayment();
        orderRepository.save(order);

        String externalId = "ext-" + UUID.randomUUID();
        PaymentAttempt paymentAttempt = PaymentAttempt.create(orderId, externalId);
        PaymentAttempt saved = paymentAttemptRepository.save(paymentAttempt);

        log.info("Payment started. Order={} ExternalId={}", orderId, externalId);
        return PaymentDtos.PaymentResponse.from(saved, order.getStatus().name());
    }
}
