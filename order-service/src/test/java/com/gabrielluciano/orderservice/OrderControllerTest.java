package com.gabrielluciano.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielluciano.orderservice.dto.OrderCreateRequest;
import com.gabrielluciano.orderservice.dto.OrderCreateRequestItem;
import com.gabrielluciano.orderservice.exception.ProductNotAvailableException;
import com.gabrielluciano.orderservice.model.OrderStatus;
import com.gabrielluciano.orderservice.repository.OrderRepository;
import com.gabrielluciano.orderservice.service.ProductService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateOrder() throws Exception {
        long id1 = 1L;
        long id2 = 2L;
        int quantity1 = 2;
        int quantity2 = 3;
        BigDecimal price1 = new BigDecimal("5.50");
        BigDecimal price2 = new BigDecimal("10.35");

        OrderCreateRequestItem item1 = OrderCreateRequestItem.builder().productId(id1).quantity(quantity1).build();
        OrderCreateRequestItem item2 = OrderCreateRequestItem.builder().productId(id2).quantity(quantity2).build();

        BDDMockito.when(productService.getProductPrice(1L)).thenReturn(price1);
        BDDMockito.when(productService.getProductPrice(2L)).thenReturn(price2);

        BigDecimal expectedTotalAmount = price1.multiply(BigDecimal.valueOf(quantity1))
                .add(price2.multiply(BigDecimal.valueOf(quantity2)))
                .setScale(2, RoundingMode.FLOOR);

        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .userId(UUID.randomUUID())
                .items(Set.of(item1, item2))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(orderCreateRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.userId").value(orderCreateRequest.getUserId().toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.PENDING.name()))
                .andExpect(jsonPath("$.totalAmount").value(expectedTotalAmount.floatValue()))
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    void shouldReturn404NotFoundWhenProductIsNotAvailable() throws Exception {
        long id = 1L;
        int quantity = 2;

        OrderCreateRequestItem item = OrderCreateRequestItem.builder().productId(id).quantity(quantity).build();

        BDDMockito.when(productService.getProductPrice(ArgumentMatchers.anyLong()))
                .thenThrow(new ProductNotAvailableException(id));

        OrderCreateRequest orderCreateRequest = OrderCreateRequest.builder()
                .userId(UUID.randomUUID())
                .items(Set.of(item))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(orderCreateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.path").value("/api/v1/orders"));
    }

    static String asJsonString(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }
}
