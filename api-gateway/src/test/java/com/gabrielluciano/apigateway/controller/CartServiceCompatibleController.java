package com.gabrielluciano.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartServiceCompatibleController {

    @PostMapping("/add")
    public Boolean addItem() {
        return true;
    }

    @GetMapping("/{userId}")
    public Long getCart(@PathVariable Long userId) {
        return userId;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable Long userId) {
    }
}
