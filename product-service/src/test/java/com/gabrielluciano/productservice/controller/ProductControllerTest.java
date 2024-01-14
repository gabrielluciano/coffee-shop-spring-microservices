package com.gabrielluciano.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielluciano.productservice.dto.ProductCreateRequest;
import com.gabrielluciano.productservice.models.Product;
import com.gabrielluciano.productservice.repository.ProductRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void shouldCreateProduct() throws Exception {
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(BigDecimal.valueOf(2.99))
                .isAvailable(true)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(productCreateRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(productCreateRequest.getName()));
    }

    @Test
    void shouldReturn409StatusOnDuplicateName() throws Exception {
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(BigDecimal.valueOf(2.99))
                .isAvailable(true)
                .build();
        productRepository.save(productCreateRequest.toProduct());

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(productCreateRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.path").value("/api/v1/products"));
    }

    @Test
    void shouldReturn400BadRequestWhenNameLengthIsGreaterThan70() throws Exception {
        int maxLength = 70;
        shouldReturn400BadRequestWhenNameIs("#".repeat(maxLength + 1));
    }

    @Test
    void shouldReturn400BadRequestWhenNameIsBlank() throws Exception {
        shouldReturn400BadRequestWhenNameIs("");
    }

    @Test
    void shouldReturn400BadRequestWhenNameIsNull() throws Exception {
        shouldReturn400BadRequestWhenNameIs(null);
    }

    private void shouldReturn400BadRequestWhenNameIs(String name) throws Exception {
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name(name)
                .price(BigDecimal.valueOf(2.99))
                .isAvailable(true)
                .build();

        performCreateRequestAndExpect400ErrorWithErrorMessageContaining(productCreateRequest, "name");
    }

    @Test
    void shouldReturn400BadRequestWhenDescriptionLengthIsGreaterThan255() throws Exception {
        int maxLength = 255;
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("#".repeat(maxLength + 1))
                .price(BigDecimal.valueOf(2.99))
                .isAvailable(true)
                .build();

        performCreateRequestAndExpect400ErrorWithErrorMessageContaining(productCreateRequest, "description");
    }

    @Test
    void shouldReturn400BadRequestWhenPriceIsNull() throws Exception {
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(null)
                .isAvailable(true)
                .build();

        performCreateRequestAndExpect400ErrorWithErrorMessageContaining(productCreateRequest, "price");
    }

    @Test
    void shouldReturn400BadRequestWhenPriceIsNegative() throws Exception {
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(BigDecimal.valueOf(-3.80))
                .isAvailable(true)
                .build();

        performCreateRequestAndExpect400ErrorWithErrorMessageContaining(productCreateRequest, "price");
    }

    @Test
    void shouldReturn400BadRequestWhenPriceIsNotANumber() throws Exception {
        String price = "12.99";
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(new BigDecimal(price))
                .isAvailable(true)
                .build();

        String jsonString = asJsonString(productCreateRequest);
        String jsonStringWithInvalidPrice = jsonString.replace(price, "invalid");

        performCreateRequestAndExpect400Error(jsonStringWithInvalidPrice);
    }

    @Test
    void shouldReturn400BadRequestWhenIsAvailableIsNull() throws Exception {
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(BigDecimal.valueOf(2.99))
                .isAvailable(null)
                .build();

        performCreateRequestAndExpect400ErrorWithErrorMessageContaining(productCreateRequest, "isAvailable");
    }

    @Test
    void shouldReturn400BadRequestWhenIsAvailableIsNotABoolean() throws Exception {
        ProductCreateRequest productCreateRequest = ProductCreateRequest.builder()
                .name("Espresso")
                .description("Strong and concentrated coffee")
                .price(BigDecimal.valueOf(2.99))
                .isAvailable(true)
                .build();

        String jsonString = asJsonString(productCreateRequest);
        String jsonStringWithInvalidIsAvailable = jsonString.replace("true", "invalid");

        performCreateRequestAndExpect400Error(jsonStringWithInvalidIsAvailable);
    }

    private void performCreateRequestAndExpect400ErrorWithErrorMessageContaining(
            ProductCreateRequest productCreateRequest, String expectedValue) throws Exception {

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(productCreateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.error", containsString(expectedValue)));
    }

    private void performCreateRequestAndExpect400Error(String jsonString) throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.path").value("/api/v1/products"));
    }

    static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }
}
