package com.gabrielluciano.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private Long productId;
    private Integer quantity;

    public static CartItem fromProductIdAndQuantity(Long productId, Integer quantity) {
        return new CartItem(productId, quantity);
    }

    public void increaseQuantityBy(Integer quantity) {
        this.quantity += quantity;
    }
}
