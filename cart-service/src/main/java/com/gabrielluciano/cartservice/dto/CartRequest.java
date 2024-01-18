package com.gabrielluciano.cartservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartRequest {

    private Long userId;
    private Long productId;
    private Integer quantity;
}
