package com.gabrielluciano.cartservice.exception;

public class CartNotFoundException extends ResourceNotFoundException {

    public CartNotFoundException(Long userId) {
        super(String.format("Product with userId '%s' not found", userId));
    }
}
