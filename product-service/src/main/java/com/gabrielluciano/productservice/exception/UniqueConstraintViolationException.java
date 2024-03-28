package com.gabrielluciano.productservice.exception;

public class UniqueConstraintViolationException extends RuntimeException {

    public UniqueConstraintViolationException(String message) {
        super(message);
    }
}
