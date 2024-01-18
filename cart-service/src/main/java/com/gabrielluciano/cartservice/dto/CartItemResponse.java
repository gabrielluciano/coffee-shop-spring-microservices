package com.gabrielluciano.cartservice.dto;

import com.gabrielluciano.cartservice.model.CartItem;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {

    private Long productId;
    private Integer quantity;

    public static CartItemResponse fromCartItem(CartItem cartItem) {
        return CartItemResponse.builder()
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
