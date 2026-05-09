package com.exequt.checkout.cart;

import com.exequt.checkout.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart findCartOrThrow(UUID cartId) {
        return cartRepository.findById(cartId)
            .orElseThrow(() -> new NotFoundException("Cart not found: " + cartId));
    }

    @Transactional
    public CartDtos.CartResponse createCart() {
        Cart cart = new Cart();
        Cart saved = cartRepository.save(cart);
        logger.info("Cart created: {}", saved.getId());
        return CartDtos.CartResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CartDtos.CartResponse getCart(UUID cartId) {
        return CartDtos.CartResponse.from(findCartOrThrow(cartId));
    }

    @Transactional
    public CartDtos.CartResponse addItem(UUID cartId, CartDtos.AddItemRequest request) {
        Cart cart = findCartOrThrow(cartId);
        cart.addItem(request.getProductId(), request.getQuantity(), request.getPrice());
        Cart saved = cartRepository.save(cart);
        logger.info("Item added to cart {}: product={} qty={}", cartId, request.getProductId(), request.getQuantity());
        return CartDtos.CartResponse.from(saved);
    }
}
