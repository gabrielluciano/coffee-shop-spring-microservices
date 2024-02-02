package com.gabrielluciano.cartservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    private String id;
    private UUID userId;
    private List<CartItem> items;
    private LocalDateTime deletedAt;

    public void addItem(CartItem item) {
        this.items.add(item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(id, cart.id) && Objects.equals(userId, cart.userId) && Objects.equals(items, cart.items) && Objects.equals(deletedAt, cart.deletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, items, deletedAt);
    }
}
