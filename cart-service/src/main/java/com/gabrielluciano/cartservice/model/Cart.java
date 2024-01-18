package com.gabrielluciano.cartservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    private String id;
    private Long userId;
    private List<CartItem> items;
    private LocalDateTime deletedAt;

    public void addItem(CartItem item) {
        this.items.add(item);
    }
}
