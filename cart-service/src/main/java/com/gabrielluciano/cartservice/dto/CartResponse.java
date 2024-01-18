package com.gabrielluciano.cartservice.dto;

import com.gabrielluciano.cartservice.model.Cart;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponse {

    private String id;
    private Long userId;
    private List<CartItemResponse> items;

    public static CartResponse fromCart(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(cart.getItems().stream()
                        .map(CartItemResponse::fromCartItem).toList())
                .build();
    }
}
