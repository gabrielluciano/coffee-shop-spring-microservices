package com.gabrielluciano.cartservice.service;

import com.gabrielluciano.cartservice.dto.CartRequest;
import com.gabrielluciano.cartservice.dto.CartResponse;

public interface CartService {

    CartResponse addItem(CartRequest cartRequest);

    CartResponse getCart(Long userId);

    void clearCart(Long userId);
}
