package com.gabrielluciano.orderservice.exception;

import lombok.Getter;

@Getter
public class ProductNotAvailableException extends Exception {

    private final Long productId;

    public ProductNotAvailableException(Long id) {
        super(String.format("Product with id '%s' is not available", id));
        this.productId = id;
    }
}
