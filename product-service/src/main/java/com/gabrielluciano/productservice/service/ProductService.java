package com.gabrielluciano.productservice.service;

import com.gabrielluciano.productservice.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    List<ProductResponse> getAllProducts();
}
