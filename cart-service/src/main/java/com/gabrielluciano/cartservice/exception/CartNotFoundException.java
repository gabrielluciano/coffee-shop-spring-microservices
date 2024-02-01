package com.gabrielluciano.cartservice.exception;

import java.util.UUID;

public class CartNotFoundException extends ResourceNotFoundException {

    public CartNotFoundException(UUID userId) {
        super(String.format("Cart for user with id '%s' not found", userId));
    }
}
