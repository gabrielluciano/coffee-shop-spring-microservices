package com.gabrielluciano.orderservice.service;

import com.gabrielluciano.orderservice.dto.OrderCreateRequest;
import com.gabrielluciano.orderservice.dto.OrderCreateRequestItem;
import com.gabrielluciano.orderservice.dto.OrderCreateResponse;
import com.gabrielluciano.orderservice.exception.InvalidOrderProductException;
import com.gabrielluciano.orderservice.exception.ProductNotAvailableException;
import com.gabrielluciano.orderservice.model.Order;
import com.gabrielluciano.orderservice.model.OrderItem;
import com.gabrielluciano.orderservice.model.OrderStatus;
import com.gabrielluciano.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Override
    public OrderCreateResponse createOrder(OrderCreateRequest orderCreateRequest) {
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(orderCreateRequest.getUserId())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        Set<OrderItem> items = createOrderItems(orderCreateRequest, order);
        order.setItems(items);
        order.setTotalAmount(calculateTotalAmount(items));

        Order savedOrder = orderRepository.saveAndFlush(order);

        return OrderCreateResponse.builder()
                .id(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(savedOrder.getStatus())
                .createdAt(savedOrder.getCreatedAt())
                .build();
    }

    private Set<OrderItem> createOrderItems(OrderCreateRequest orderCreateRequest, Order order) {
        Set<OrderItem> items = new HashSet<>();
        for (OrderCreateRequestItem item : orderCreateRequest.getItems()) {
            items.add(new OrderItem(order, item.getProductId(), item.getQuantity()));
        }
        return items;
    }

    private BigDecimal calculateTotalAmount(Set<OrderItem> items) {
        BigDecimal totalAmount = new BigDecimal("0.00");
        for (OrderItem item : items) {
            BigDecimal price = tryGetProductPrice(item.getProductId());
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return totalAmount.setScale(2, RoundingMode.FLOOR);
    }

    private BigDecimal tryGetProductPrice(Long productId) {
        BigDecimal price;
        try {
            price = productService.getProductPrice(productId);
        } catch (ProductNotAvailableException e) {
            String message = String.format("Product with id '%d' is not available or doesn't exist", e.getProductId());
            throw new InvalidOrderProductException(message);
        }
        return price;
    }
}
