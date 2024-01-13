package com.gabrielluciano.productservice.controller;

import com.gabrielluciano.productservice.models.Product;
import com.gabrielluciano.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldGetAllProducts() throws Exception {
        Product product1 = Product.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(BigDecimal.valueOf(2.99))
                .isAvailable(true)
                .build();

        Product product2 = Product.builder()
                .name("Cappuccino")
                .description("Coffee with steamed milk and foam")
                .price(BigDecimal.valueOf(3.49))
                .isAvailable(true)
                .build();

        productRepository.saveAll(List.of(product1, product2));

        mockMvc.perform(get("/api/v1/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(product1.getId()))
                .andExpect(jsonPath("$[0].name").value(product1.getName()))
                .andExpect(jsonPath("$[1].id").value(product2.getId()))
                .andExpect(jsonPath("$[1].name").value(product2.getName()));
    }
}
