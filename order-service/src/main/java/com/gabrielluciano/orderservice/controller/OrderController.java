package com.gabrielluciano.orderservice.controller;

import com.gabrielluciano.orderservice.dto.OrderCreateRequest;
import com.gabrielluciano.orderservice.dto.OrderCreateResponse;
import com.gabrielluciano.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCreateResponse createOrder(@RequestBody OrderCreateRequest orderCreateRequest) {
        return orderService.createOrder(orderCreateRequest);
    }
}
