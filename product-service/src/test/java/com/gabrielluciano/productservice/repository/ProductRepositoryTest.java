package com.gabrielluciano.productservice.repository;

import com.gabrielluciano.productservice.model.Product;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

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

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldSaveProduct() {
        Product product = Product.builder()
                .name("Some Coffee")
                .description("Description of the product")
                .price(BigDecimal.valueOf(10.99))
                .isAvailable(true)
                .build();

        productRepository.save(product);
        entityManager.flush();

        Product productFromDb = entityManager.find(Product.class, product.getId());

        assertNotNull(productFromDb);
        assertEquals(product.getId(), productFromDb.getId());
        assertEquals(product.getName(), productFromDb.getName());
        assertEquals(product, productFromDb);
    }

    @Test
    void shouldFindProductById() {
        Product product = Product.builder()
                .name("Some Coffee")
                .description("Description of the product")
                .price(BigDecimal.valueOf(10.99))
                .isAvailable(true)
                .build();
        entityManager.persistAndFlush(product);

        Product productFromDb = productRepository.findById(product.getId()).orElseThrow();

        assertNotNull(productFromDb);
        assertEquals(product.getId(), productFromDb.getId());
        assertEquals(product.getName(), productFromDb.getName());
        assertEquals(product, productFromDb);
    }

    @Test
    void shouldFindAllProducts() {
        Product product1 = Product.builder()
                .name("Some Coffee 1")
                .description("Description of the product 1")
                .price(BigDecimal.valueOf(10.99))
                .isAvailable(true)
                .build();

        Product product2 = Product.builder()
                .name("Some Coffee 2")
                .description("Description of the product 2")
                .price(BigDecimal.valueOf(11.99))
                .isAvailable(true)
                .build();

        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.flush();

        List<Product> products = productRepository.findAll();

        assertThat(products).hasSize(2);
        assertThat(products).contains(product1);
        assertThat(products).contains(product2);
    }

    @Test
    void shouldFindProductByName() {
        Product product = Product.builder()
                .name("Some Coffee")
                .description("Description of the product")
                .price(BigDecimal.valueOf(10.99))
                .isAvailable(true)
                .build();
        entityManager.persistAndFlush(product);

        Product productFromDb = productRepository.findByName(product.getName()).orElseThrow();

        assertNotNull(productFromDb);
        assertEquals(product.getId(), productFromDb.getId());
        assertEquals(product.getName(), productFromDb.getName());
        assertEquals(product, productFromDb);
    }

    @Test
    void shouldDeleteProductById() {
        Product product = Product.builder()
                .name("Some Coffee")
                .description("Description of the product")
                .price(BigDecimal.valueOf(10.99))
                .isAvailable(true)
                .build();
        entityManager.persistAndFlush(product);

        productRepository.deleteById(product.getId());

        Product productFromDb = entityManager.find(Product.class, product.getId());

        assertNull(productFromDb);
    }
}
