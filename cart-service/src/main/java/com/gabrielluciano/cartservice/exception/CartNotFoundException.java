package com.gabrielluciano.cartservice.exception;

public class CartNotFoundException extends ResourceNotFoundException {

    public CartNotFoundException(Long userId) {
        super(String.format("Cart for user with id '%s' not found", userId));
    }
}
