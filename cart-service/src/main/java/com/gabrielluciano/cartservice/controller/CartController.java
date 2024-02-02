package com.gabrielluciano.cartservice.controller;

import com.gabrielluciano.cartservice.dto.CartRequest;
import com.gabrielluciano.cartservice.dto.CartResponse;
import com.gabrielluciano.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("#cartRequest.userId.toString() == principal.claims['userId']")
    public CartResponse addItem(@RequestBody CartRequest cartRequest) {
        return cartService.addItem(cartRequest);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("#userId.toString() == principal.claims['userId']")
    public CartResponse getCart(@PathVariable UUID userId) {
        return cartService.getCart(userId);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId.toString() == principal.claims['userId']")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable UUID userId) {
        cartService.clearCart(userId);
    }
}
