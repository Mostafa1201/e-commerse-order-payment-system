package com.exequt.checkout.cart;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartDtos.CartResponse> createCart() {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cartService.createCart());
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDtos.CartResponse> getCart(@PathVariable UUID cartId) {
        return ResponseEntity.ok(cartService.getCart(cartId));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartDtos.CartResponse> addItem(
        @PathVariable UUID cartId,
        @Valid @RequestBody CartDtos.AddItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cartService.addItem(cartId, request));
    }
}
