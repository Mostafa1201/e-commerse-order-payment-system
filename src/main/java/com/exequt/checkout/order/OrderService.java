package com.exequt.checkout.order;

import com.exequt.checkout.cart.Cart;
import com.exequt.checkout.cart.CartRepository;
import com.exequt.checkout.exception.ConflictException;
import com.exequt.checkout.exception.NotFoundException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public OrderService(CartRepository cartRepository, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    public Order findOrderOrThrow(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    public OrderDtos.OrderResponse getOrder(UUID orderId) {
        return OrderDtos.OrderResponse.from(findOrderOrThrow(orderId));
    }

    @Transactional
    public OrderDtos.OrderResponse checkout(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new NotFoundException("Cart not found: " + cartId));
        if (!cart.isOpen()) {
            throw new ConflictException("Cart " + cartId + " is already checked out");
        }
        if (cart.getItems().isEmpty()) {
            throw new ConflictException("Cannot checkout an empty cart");
        }
        cart.checkout();
        cartRepository.save(cart);
        Order order = Order.createFromCart(cartId, cart.calculateTotal());
        Order saved = orderRepository.save(order);

        log.info("Checkout complete. Cart={} -> Order={} total={}", cartId, saved.getId(),
                saved.getTotalAmount());
        return OrderDtos.OrderResponse.from(saved);
    }
}
