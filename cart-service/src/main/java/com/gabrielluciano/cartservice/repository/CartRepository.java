package com.gabrielluciano.cartservice.repository;

import com.gabrielluciano.cartservice.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends MongoRepository<Cart, String> {

    @Query("{ 'userId': ?0, 'deletedAt': null }")
    Optional<Cart> findByUserIdAndDeletedAtIsNull(UUID userId);
}
