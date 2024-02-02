package com.gabrielluciano.cartservice.repository;

import com.gabrielluciano.cartservice.model.Cart;
import com.gabrielluciano.cartservice.model.CartItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
class CartRepositoryTest {

    static final MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0.5");

    @BeforeAll
    static void beforeAll() {
        mongodb.start();
    }

    @AfterAll
    static void afterAll() {
        mongodb.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongodb.getReplicaSetUrl("cartservice"));
    }

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
    }

    @Test
    void shouldFindByUserIdAndDeletedAtIsNull() {
        Cart cart = Cart.builder()
                .userId(UUID.randomUUID())
                .items(List.of(CartItem.fromProductIdAndQuantity(1L, 2)))
                .build();

        Cart deletedCart = Cart.builder()
                .userId(UUID.randomUUID())
                .items(List.of(CartItem.fromProductIdAndQuantity(1L, 2)))
                .deletedAt(LocalDateTime.now())
                .build();

        cartRepository.save(cart);
        cartRepository.save(deletedCart);

        Cart cartFromDb = cartRepository.findByUserIdAndDeletedAtIsNull(cart.getUserId()).orElseThrow();

        assertEquals(cart.getId(), cartFromDb.getId());
        assertEquals(cart.getUserId(), cartFromDb.getUserId());
        assertEquals(cart, cartFromDb);
    }

    @Test
    void shouldNotFindByUserIdAndDeletedAtIsNull() {
        Cart deletedCart = Cart.builder()
                .userId(UUID.randomUUID())
                .items(List.of(CartItem.fromProductIdAndQuantity(1L, 2)))
                .deletedAt(LocalDateTime.now())
                .build();

        cartRepository.save(deletedCart);

        Optional<Cart> optionalCartFromDb = cartRepository.findByUserIdAndDeletedAtIsNull(deletedCart.getUserId());

        assertThat(optionalCartFromDb).isEmpty();
    }
}
