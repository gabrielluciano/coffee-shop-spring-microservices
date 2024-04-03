package com.gabrielluciano.orderservice.service;

import com.gabrielluciano.orderservice.dto.OrderCreateRequest;
import com.gabrielluciano.orderservice.dto.OrderCreateResponse;

public interface OrderService {
    OrderCreateResponse createOrder(OrderCreateRequest orderCreateRequest);
}
