package com.gabrielluciano.productservice.service;

import com.gabrielluciano.productservice.dto.ProductCreateRequest;
import com.gabrielluciano.productservice.dto.ProductResponse;
import com.gabrielluciano.productservice.exception.ProductNotFoundException;
import com.gabrielluciano.productservice.model.Product;
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

    @Override
    public ProductResponse createProduct(ProductCreateRequest productCreateRequest) {
        Product product = productRepository.save(productCreateRequest.toProduct());
        return ProductResponse.fromProduct(product);
    }

    @Override
    public ProductResponse getProduct(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromProduct)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
