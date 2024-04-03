package com.gabrielluciano.orderservice.service;

import com.gabrielluciano.orderservice.exception.ProductNotAvailableException;

import java.math.BigDecimal;

public interface ProductService {

    BigDecimal getProductPrice(Long id) throws ProductNotAvailableException;
}
