package com.gabrielluciano.cartservice.service;

import com.gabrielluciano.cartservice.dto.CartRequest;
import com.gabrielluciano.cartservice.dto.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse addItem(CartRequest cartRequest);

    CartResponse getCart(UUID userId);

    void clearCart(UUID userId);
}
