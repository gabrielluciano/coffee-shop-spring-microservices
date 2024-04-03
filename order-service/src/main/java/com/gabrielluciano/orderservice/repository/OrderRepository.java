package com.gabrielluciano.orderservice.repository;

import com.gabrielluciano.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
