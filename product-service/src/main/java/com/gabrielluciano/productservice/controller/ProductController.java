package com.gabrielluciano.productservice.controller;

import com.gabrielluciano.productservice.dto.ProductCreateRequest;
import com.gabrielluciano.productservice.dto.ProductResponse;
import com.gabrielluciano.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody @Valid ProductCreateRequest productCreateRequest) {
        return productService.createProduct(productCreateRequest);
    }
}
