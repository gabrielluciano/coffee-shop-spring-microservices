package com.gabrielluciano.productservice.service;

import com.gabrielluciano.productservice.dto.ProductResponse;
import com.gabrielluciano.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromProduct)
                .toList();
    }
}
