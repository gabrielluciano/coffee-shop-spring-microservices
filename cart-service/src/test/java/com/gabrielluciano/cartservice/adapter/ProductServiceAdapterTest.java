package com.gabrielluciano.cartservice.adapter;

import com.gabrielluciano.cartservice.CartServiceApplication;
import com.gabrielluciano.cartservice.exception.ServiceUnavailableException;
import com.gabrielluciano.cartservice.repository.CartRepository;
import com.gabrielluciano.cartservice.util.controller.ProductServiceCompatibleController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = {ProductServiceAdapterTest.TestConfig.class}, webEnvironment = RANDOM_PORT,
        properties = {"eureka.client.enabled=false", "spring.main.allow-bean-definition-overriding=true"})
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class, SecurityAutoConfiguration.class})
class ProductServiceAdapterTest {

    @Autowired
    private ProductServiceAdapter productServiceAdapter;

    // This mock is required because otherwise Spring will try to create the cartRepository bean
    // leading to an error because MongoAutoConfiguration and MongoDataAutoConfiguration are disabled
    @MockBean
    private CartRepository cartRepository;

    @Test
    void shouldReturnTrueWhenProductExists() {
        long productId = ProductServiceCompatibleController.EXISTENT_PRODUCT_ID;

        boolean result = productServiceAdapter.productExists(productId);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenProductDoesNotExist() {
        long productId = ProductServiceCompatibleController.NON_EXISTENT_PRODUCT_ID;

        boolean result = productServiceAdapter.productExists(productId);

        assertFalse(result);
    }

    @Test
    void shouldThrowServiceUnavailableExceptionWhenTimeout() {
        long productId = ProductServiceCompatibleController.SLOW_RESPONSE_PRODUCT_ID;

        assertThrows(ServiceUnavailableException.class, () -> {
            productServiceAdapter.productExists(productId);
        });
    }

    @Test
    void shouldThrowServiceUnavailableExceptionWhenProductServiceIsNotAvailable() {
        long productId = ProductServiceCompatibleController.SERVICE_UNAVAILABLE_PRODUCT_ID;

        assertThrows(ServiceUnavailableException.class, () -> {
            productServiceAdapter.productExists(productId);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @LoadBalancerClients({
            @LoadBalancerClient(name = "product-service", configuration = ProductServiceLoadBalancerConfig.class),
    })
    @Import(CartServiceApplication.class)
    static class TestConfig {

        @Bean
        public ProductServiceCompatibleController productServiceCompatibleController() {
            return new ProductServiceCompatibleController();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable());
            http.authorizeHttpRequests(requests -> requests
                    .anyRequest().permitAll()
            );
            return http.build();
        }
    }

    static class ProductServiceLoadBalancerConfig {

        @LocalServerPort
        private int port;

        @Bean
        public ServiceInstanceListSupplier fixedServiceInstanceListSupplier(Environment env) {
            return ServiceInstanceListSuppliers.from("product-service",
                    new DefaultServiceInstance("product-service-1", "product-service", "localhost", port, false));
        }
    }
}
