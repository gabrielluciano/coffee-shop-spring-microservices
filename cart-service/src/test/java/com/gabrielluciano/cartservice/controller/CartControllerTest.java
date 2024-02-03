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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
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
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    private MockMvc authenticatedMockMvc;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();

        BDDMockito.when(productService.productExists(ArgumentMatchers.anyLong()))
                .thenReturn(true);

        authenticatedMockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldReturn401WhenAddItemAndNotAuthenticated() throws Exception {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenAddItemAndCartRequestUserIdIsDifferentFromJwtUserIdClaim() throws Exception {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        authenticatedMockMvc.perform(post("/api/v1/cart/add")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAddItem() throws Exception {
        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        authenticatedMockMvc.perform(post("/api/v1/cart/add")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", cartRequest.getUserId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(cartRequest.getUserId().toString()))
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

        authenticatedMockMvc.perform(post("/api/v1/cart/add")
                .with(jwt().jwt(jwt -> jwt.claim("userId", cartRequest.getUserId().toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cartRequest)));

        authenticatedMockMvc.perform(post("/api/v1/cart/add")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", cartRequest.getUserId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(cartRequest.getUserId().toString()))
                .andExpect(jsonPath("$.items[0].productId").value(cartRequest.getProductId()))
                .andExpect(jsonPath("$.items[0].quantity").value(cartRequest.getQuantity() * 2));
    }

    @Test
    void shouldHaveTwoItems() throws Exception {
        UUID userId = UUID.randomUUID();

        CartRequest cartRequest1 = CartRequest.builder()
                .userId(userId)
                .productId(1L)
                .quantity(2)
                .build();

        CartRequest cartRequest2 = CartRequest.builder()
                .userId(userId)
                .productId(2L)
                .quantity(3)
                .build();

        authenticatedMockMvc.perform(post("/api/v1/cart/add")
                .with(jwt().jwt(jwt -> jwt.claim("userId", userId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(cartRequest1)));

        authenticatedMockMvc.perform(post("/api/v1/cart/add")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest2)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(cartRequest1.getUserId().toString()))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void shouldReturn404WhenAddItemAndProductIsNotFound() throws Exception {
        BDDMockito.when(productService.productExists(ArgumentMatchers.anyLong()))
                .thenReturn(false);

        CartRequest cartRequest = CartRequest.builder()
                .userId(UUID.randomUUID())
                .productId(1L)
                .quantity(2)
                .build();

        authenticatedMockMvc.perform(post("/api/v1/cart/add")
                        .with(jwt().jwt(jwt -> jwt.claim("userId", cartRequest.getUserId().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cartRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenGetCartAndNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/cart/" + UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenGetCartAndUserIdIsDifferentFromJwtUserIdClaim() throws Exception {
        authenticatedMockMvc.perform(get("/api/v1/cart/" + UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.claim("userId", UUID.randomUUID().toString()))))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetCart() throws Exception {
        UUID userId = UUID.randomUUID();
        CartItem cartItem = CartItem.fromProductIdAndQuantity(1L, 2);
        Cart cart = Cart.builder()
                .userId(userId)
                .items(List.of(cartItem))
                .build();

        cartRepository.save(cart);

        authenticatedMockMvc.perform(get("/api/v1/cart/" + userId)
                        .with(jwt().jwt(jwt -> jwt.claim("userId", userId.toString()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.items[0].productId").value(cartItem.getProductId()));
    }

    @Test
    void shouldReturn404WhenCartIsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticatedMockMvc.perform(get("/api/v1/cart/" + userId)
                        .with(jwt().jwt(jwt -> jwt.claim("userId", userId.toString()))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenClearCartAndNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/" + UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenClearCartAndUserIdIsDifferentFromJwtUserIdClaim() throws Exception {
        authenticatedMockMvc.perform(delete("/api/v1/cart/" + UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.claim("userId", UUID.randomUUID().toString()))))
                .andDo(print())
                .andExpect(status().isForbidden());
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

        authenticatedMockMvc.perform(delete("/api/v1/cart/" + userId)
                        .with(jwt().jwt(jwt -> jwt.claim("userId", userId.toString()))))
                .andDo(print())
                .andExpect(status().isNoContent());

        Optional<Cart> optionalCart = cartRepository.findByUserIdAndDeletedAtIsNull(userId);
        assertTrue(optionalCart.isEmpty());
    }

    static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }
}
