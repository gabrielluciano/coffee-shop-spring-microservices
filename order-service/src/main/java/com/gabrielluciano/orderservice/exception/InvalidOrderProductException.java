package com.gabrielluciano.orderservice.exception;

public class InvalidOrderProductException extends RuntimeException {

    public InvalidOrderProductException(String message) {
        super(message);
    }
}
