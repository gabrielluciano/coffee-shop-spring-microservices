package com.gabrielluciano.productservice.dto;

import com.gabrielluciano.productservice.model.Product;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductCreateRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isAvailable;

    public Product toProduct() {
        return Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .isAvailable(isAvailable)
                .build();
    }
}
