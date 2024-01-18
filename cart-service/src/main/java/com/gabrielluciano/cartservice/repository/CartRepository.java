package com.gabrielluciano.cartservice.repository;

import com.gabrielluciano.cartservice.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {

    @Query("{ 'userId': ?0, 'deletedAt': null }")
    Optional<Cart> findByUserIdAndDeletedAtIsNull(Long userId);
}
