package com.gabrielluciano.cartservice.controller;

import com.gabrielluciano.cartservice.dto.CartItemResponse;
import com.gabrielluciano.cartservice.dto.CartRequest;
import com.gabrielluciano.cartservice.dto.CartResponse;
import com.gabrielluciano.cartservice.repository.CartRepository;
import com.gabrielluciano.cartservice.security.WithMockJwt;
import com.gabrielluciano.cartservice.service.CartService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
class CartControllerMethodSecurityTest {

    @Autowired
    private CartController cartController;

    @MockBean
    private CartService cartService;

    // This mock is required because otherwise Spring will try to create the cartRepository bean
    // leading to an error because MongoAutoConfiguration and MongoDataAutoConfiguration are disabled
    @MockBean
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        CartItemResponse cartItemResponse = CartItemResponse.builder()
                .productId(1L)
                .quantity(1)
                .build();

        CartResponse cartResponse = CartResponse.builder()
                .id(UUID.randomUUID().toString())
                .userId(UUID.randomUUID())
                .items(List.of(cartItemResponse))
                .build();

        BDDMockito.when(cartService.addItem(ArgumentMatchers.any()))
                .thenReturn(cartResponse);
        BDDMockito.when(cartService.getCart(ArgumentMatchers.any()))
                .thenReturn(cartResponse);
    }

    @Test
    @WithMockJwt(userId = "5730f4bb-30ea-4cc5-b0a5-9cae8c3da714")
    void addItemAuthorized() {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.fromString("5730f4bb-30ea-4cc5-b0a5-9cae8c3da714"))
                .quantity(1)
                .productId(1L)
                .build();

        CartResponse cartResponse = cartController.addItem(cartRequest);
        assertNotNull(cartResponse);
        assertInstanceOf(CartResponse.class, cartResponse);
    }

    @Test
    void addItemUnauthenticated() {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.fromString("5730f4bb-30ea-4cc5-b0a5-9cae8c3da714"))
                .quantity(1)
                .productId(1L)
                .build();

        Assertions.assertThrows(IllegalArgumentException.class, () ->
                cartController.addItem(cartRequest));
    }

    @Test
    @WithMockJwt(userId = "5730f4bb-30ea-4cc5-b0a5-9cae8c3da714")
    void addItemUnauthorized() {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .quantity(1)
                .productId(1L)
                .build();

        Assertions.assertThrows(AccessDeniedException.class, () ->
                cartController.addItem(cartRequest));
    }

    @Test
    @WithMockJwt(userId = "5730f4bb-30ea-4cc5-b0a5-9cae8c3da714")
    void getCartAuthorized() {
        CartResponse cartResponse = cartController.getCart(UUID.fromString("5730f4bb-30ea-4cc5-b0a5-9cae8c3da714"));
        assertNotNull(cartResponse);
        assertInstanceOf(CartResponse.class, cartResponse);
    }

    @Test
    void getCartUnauthenticated() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                cartController.getCart(UUID.fromString("5730f4bb-30ea-4cc5-b0a5-9cae8c3da714")));
    }

    @Test
    @WithMockJwt(userId = "5730f4bb-30ea-4cc5-b0a5-9cae8c3da714")
    void getCartUnauthorized() {
        Assertions.assertThrows(AccessDeniedException.class, () ->
                cartController.getCart(UUID.randomUUID()));
    }

    @Test
    @WithMockJwt(userId = "5730f4bb-30ea-4cc5-b0a5-9cae8c3da714")
    void clearCartAuthorized() {
        assertDoesNotThrow(() -> {
            cartController.clearCart(UUID.fromString("5730f4bb-30ea-4cc5-b0a5-9cae8c3da714"));
        });
    }

    @Test
    void clearCartUnauthenticated() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                cartController.clearCart(UUID.randomUUID()));
    }

    @Test
    @WithMockJwt(userId = "5730f4bb-30ea-4cc5-b0a5-9cae8c3da714")
    void clearCartUnauthorized() {
        Assertions.assertThrows(AccessDeniedException.class, () ->
                cartController.clearCart(UUID.randomUUID()));
    }
}
