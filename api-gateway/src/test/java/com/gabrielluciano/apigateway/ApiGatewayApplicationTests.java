package com.gabrielluciano.apigateway;

import com.gabrielluciano.apigateway.controller.CartServiceCompatibleController;
import com.gabrielluciano.apigateway.controller.DiscoveryServerCompatibleController;
import com.gabrielluciano.apigateway.controller.ProductServiceCompatibleController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest(classes = {ApiGatewayApplicationTests.TestConfig.class}, webEnvironment = RANDOM_PORT,
        properties = {"eureka.client.enabled=false"})
@ContextConfiguration
class ApiGatewayApplicationTests {

    @Autowired
    ApplicationContext context;

    @LocalServerPort
    private int port;

    private WebTestClient webClient;

    @BeforeEach
    void setUp() {
        String baseUri = "http://localhost:" + port;
        this.webClient = WebTestClient.bindToApplicationContext(context)
                .apply(springSecurity())
                .configureClient()
                .responseTimeout(Duration.ofSeconds(10))
                .baseUrl(baseUri)
                .build();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void productServiceGetAllProducts() {
        webClient.get().uri("/api/v1/products").exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .consumeWith(result -> assertEquals(true, result.getResponseBody()));
    }

    @Test
    void productServiceGetProduct() {
        long productId = 1L;
        webClient.get().uri("/api/v1/products/" + productId).exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .consumeWith(result -> assertEquals(productId, result.getResponseBody()));
    }

    @Test
    void productServiceCreateProductAuthorized() {
        webClient.mutateWith(mockJwt().authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .post().uri("/api/v1/products").exchange()
                .expectStatus().isCreated()
                .expectBody(Boolean.class)
                .consumeWith(result -> assertEquals(true, result.getResponseBody()));
    }

    @Test
    void productServiceCreateProductUnauthorized() {
        webClient.post().uri("/api/v1/products").exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void productServiceCreateProductForbidden() {
        webClient.mutateWith(mockJwt().authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_EMPLOYEE"))))
                .post().uri("/api/v1/products").exchange()
                .expectStatus().isForbidden();
    }

    // TODO: Investigate and fix 431 Error
//    @Test
//    void productServiceNotFound() {
//        webClient.get().uri("/api/v1/products/ab/cd").exchange()
//                .expectStatus().isNotFound();
//    }


    @Test
    void cartServiceGetCartAuthorized() {
        long userId = 1L;
        webClient.mutateWith(mockJwt().authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"))))
                .get().uri("/api/v1/cart/" + userId).exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .consumeWith(result -> assertEquals(userId, result.getResponseBody()));
    }

    @Test
    void cartServiceGetCartUnauthorized() {
        long userId = 1L;
        webClient.get().uri("/api/v1/cart/" + userId).exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void cartServiceGetCartForbidden() {
        long userId = 1L;
        webClient.mutateWith(mockJwt())
                .get().uri("/api/v1/cart/" + userId).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void cartServiceAddItemAuthorized() {
        webClient.mutateWith(mockJwt().authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"))))
                .post().uri("/api/v1/cart/add").exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .consumeWith(result -> assertEquals(true, result.getResponseBody()));
    }

    @Test
    void cartServiceAddItemUnauthorized() {
        webClient.post().uri("/api/v1/cart/add").exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void cartServiceAddItemForbidden() {
        webClient.mutateWith(mockJwt())
                .post().uri("/api/v1/cart/add").exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void cartServiceClearCartAuthorized() {
        webClient.mutateWith(mockJwt().authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"))))
                .delete().uri("/api/v1/cart/" + 1L).exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void cartServiceClearCartUnauthorized() {
        webClient.delete().uri("/api/v1/cart/" + 1L).exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void cartServiceClearCartForbidden() {
        webClient.mutateWith(mockJwt())
                .delete().uri("/api/v1/cart/" + 1L).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void discoveryServerHome() {
        webClient.get().uri("/eureka").exchange()
                .expectStatus().isOk();
    }

    @Test
    void discoveryServerStaticResources() {
        webClient.get().uri("/eureka/css").exchange()
                .expectStatus().isOk();
        webClient.get().uri("/eureka/js/file.js").exchange()
                .expectStatus().isOk();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @LoadBalancerClients({
            @LoadBalancerClient(name = "product-service", configuration = ProductServiceLoadBalancerConfig.class),
            @LoadBalancerClient(name = "cart-service", configuration = CartServiceLoadBalancerConfig.class),
            @LoadBalancerClient(name = "discovery-server", configuration = DiscoveryServerLoadBalancerConfig.class)
    })
    @Import(ApiGatewayApplication.class)
    static class TestConfig {

        @Bean
        public ProductServiceCompatibleController productServiceCompatibleController() {
            return new ProductServiceCompatibleController();
        }

        @Bean
        public CartServiceCompatibleController cartServiceCompatibleController() {
            return new CartServiceCompatibleController();
        }

        @Bean
        public DiscoveryServerCompatibleController discoveryServerCompatibleController() {
            return new DiscoveryServerCompatibleController();
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

    static class CartServiceLoadBalancerConfig {

        @LocalServerPort
        private int port;

        @Bean
        public ServiceInstanceListSupplier fixedServiceInstanceListSupplier(Environment env) {
            return ServiceInstanceListSuppliers.from("cart-service",
                    new DefaultServiceInstance("cart-service-1", "cart-service", "localhost", port, false));
        }
    }

    static class DiscoveryServerLoadBalancerConfig {

        @Bean
        public ServiceInstanceListSupplier fixedServiceInstanceListSupplier(Environment env) {
            return ServiceInstanceListSuppliers.from("discovery-server",
                    new DefaultServiceInstance("discovery-server-1", "discovery-server", "localhost", 8761, false));
        }
    }
}
