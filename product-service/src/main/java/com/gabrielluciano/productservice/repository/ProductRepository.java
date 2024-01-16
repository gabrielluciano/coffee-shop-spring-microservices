package com.gabrielluciano.productservice.repository;

import com.gabrielluciano.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("FROM Product p ORDER BY p.id ASC")
    List<Product> findAll();
}
