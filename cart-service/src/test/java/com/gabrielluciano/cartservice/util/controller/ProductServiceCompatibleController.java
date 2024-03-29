package com.gabrielluciano.cartservice.util.controller;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@RestController
@RequestMapping("/api/v1/products")
public class ProductServiceCompatibleController {

    public static final Long EXISTENT_PRODUCT_ID = 1L;
    public static final Long NON_EXISTENT_PRODUCT_ID = 2L;
    public static final Long SLOW_RESPONSE_PRODUCT_ID = 3L;
    public static final Long SERVICE_UNAVAILABLE_PRODUCT_ID = 4L;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) throws InterruptedException {
        ProductResponse productResponse = ProductResponse.builder()
                .name("Coffee")
                .description("Some description")
                .price(BigDecimal.valueOf(12.99))
                .isAvailable(true)
                .build();

        if (EXISTENT_PRODUCT_ID.equals(id)) {
            productResponse.setId(EXISTENT_PRODUCT_ID);
            return ResponseEntity.ok(productResponse);
        }

        if (NON_EXISTENT_PRODUCT_ID.equals(id)) {
            return ResponseEntity.notFound().build();
        }

        if (SLOW_RESPONSE_PRODUCT_ID.equals(id)) {
            Thread.sleep(15 * 1000);
            productResponse.setId(SLOW_RESPONSE_PRODUCT_ID);
            return ResponseEntity.ok(productResponse);
        }

        if (SERVICE_UNAVAILABLE_PRODUCT_ID.equals(id)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        return ResponseEntity.notFound().build();
    }

    @Data
    @Builder
    public static class ProductResponse {

        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Boolean isAvailable;
    }
}
