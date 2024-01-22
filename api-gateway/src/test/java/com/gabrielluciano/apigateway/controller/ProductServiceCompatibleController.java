package com.gabrielluciano.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductServiceCompatibleController {

    @GetMapping
    public Boolean getAllProducts() {
        return true;
    }

    @GetMapping("/{id}")
    public Long getProduct(@PathVariable Long id) {
        return id;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Boolean createProduct() {
        return true;
    }
}
