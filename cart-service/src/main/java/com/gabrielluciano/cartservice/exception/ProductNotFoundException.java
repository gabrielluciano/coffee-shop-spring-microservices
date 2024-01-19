package com.gabrielluciano.cartservice.exception;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long id) {
        super(String.format("Product with id '%s' not found", id));
    }
}
