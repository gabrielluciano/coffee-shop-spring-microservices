package com.gabrielluciano.cartservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CartRequest {

    private UUID userId;
    private Long productId;
    private Integer quantity;
}
