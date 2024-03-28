package com.gabrielluciano.productservice.repository;

import com.gabrielluciano.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("FROM Product p ORDER BY p.id ASC")
    List<Product> findAll();

    @Query("FROM Product p WHERE p.name = :name")
    Optional<Product> findByName(String name);
}
