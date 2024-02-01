package com.gabrielluciano.cartservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielluciano.cartservice.dto.CartRequest;
import com.gabrielluciano.cartservice.model.Cart;
import com.gabrielluciano.cartservice.model.CartItem;
import com.gabrielluciano.cartservice.repository.CartRepository;
import com.gabrielluciano.cartservice.service.ProductService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

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

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();

        BDDMockito.when(productService.productExists(ArgumentMatchers.anyLong()))
                .thenReturn(true);
    }

    @Test
    void shouldAddItemToCart() throws Exception {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(cartRequest.getUserId()))
                .andExpect(jsonPath("$.items[0].productId").value(cartRequest.getProductId()))
                .andExpect(jsonPath("$.items[0].quantity").value(cartRequest.getQuantity()));
    }

    @Test
    void shouldIncreaseItemQuantity() throws Exception {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cartRequest)));

        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(cartRequest.getUserId()))
                .andExpect(jsonPath("$.items[0].productId").value(cartRequest.getProductId()))
                .andExpect(jsonPath("$.items[0].quantity").value(cartRequest.getQuantity() * 2));
    }

    @Test
    void shouldHaveTwoItems() throws Exception {
        CartRequest cartRequest1 = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        CartRequest cartRequest2 = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(2L)
                .quantity(3)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cartRequest1)));

        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(cartRequest1.getUserId()))
                .andExpect(jsonPath("$.items[0].length()").value(2));
    }

    @Test
    void shouldReturn404WhenProductIsNotFound() throws Exception {
        BDDMockito.when(productService.productExists(ArgumentMatchers.anyLong()))
                .thenReturn(false);

        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindCartByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        CartItem cartItem = CartItem.fromProductIdAndQuantity(1L, 2);
        Cart cart = Cart.builder()
                .userId(userId)
                .items(List.of(cartItem))
                .build();

        cartRepository.save(cart);

        mockMvc.perform(get("/api/v1/cart/" + userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.items[0].productId").value(cartItem.getProductId()));
    }

    @Test
    void shouldReturn404WhenCartIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/cart/10"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldClearCart() throws Exception {
        UUID userId = UUID.randomUUID();
        CartItem cartItem = CartItem.fromProductIdAndQuantity(1L, 2);
        Cart cart = Cart.builder()
                .userId(userId)
                .items(List.of(cartItem))
                .build();

        cartRepository.save(cart);

        mockMvc.perform(delete("/api/v1/cart/" + userId))
                .andDo(print())
                .andExpect(status().isNoContent());

        Optional<Cart> optionalCart = cartRepository.findByUserIdAndDeletedAtIsNull(userId);
        assertTrue(optionalCart.isEmpty());
    }

    static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }
}
