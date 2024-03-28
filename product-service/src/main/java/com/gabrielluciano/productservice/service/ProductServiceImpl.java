package com.gabrielluciano.productservice.service;

import com.gabrielluciano.productservice.dto.ProductCreateRequest;
import com.gabrielluciano.productservice.dto.ProductResponse;
import com.gabrielluciano.productservice.exception.ProductNotFoundException;
import com.gabrielluciano.productservice.exception.UniqueConstraintViolationException;
import com.gabrielluciano.productservice.model.Product;
import com.gabrielluciano.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromProduct)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest productCreateRequest) {
        findProductByNameAndThrowExceptionIfFound(productCreateRequest.getName());
        Product product = productRepository.save(productCreateRequest.toProduct());
        log.info("Successfully created product with id '{}' and name '{}'", product.getId(), product.getName());
        return ProductResponse.fromProduct(product);
    }

    private void findProductByNameAndThrowExceptionIfFound(String name) {
        productRepository.findByName(name).ifPresent(p -> {
            throw new UniqueConstraintViolationException("Name already exists");
        });
    }

    @Override
    public ProductResponse getProduct(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::fromProduct)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
