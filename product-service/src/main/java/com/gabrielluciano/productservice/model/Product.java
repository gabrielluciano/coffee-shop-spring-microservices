package com.gabrielluciano.productservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "products")
@SequenceGenerator(
        name = Product.SEQUENCE_NAME,
        sequenceName = Product.SEQUENCE_NAME,
        allocationSize = 1
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Product {

    public static final String SEQUENCE_NAME = "products_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Column(length = 70, unique = true, nullable = false)
    @NotNull
    @Size(min = 1, max = 70)
    private String name;

    @Size(max = 255)
    private String description;

    @Column(precision = 10, scale = 2)
    @Positive
    @NotNull
    private BigDecimal price;

    @NotNull
    private Boolean isAvailable;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Product product = (Product) object;
        return Objects.equals(id, product.id) && Objects.equals(name, product.name) && Objects.equals(description, product.description) && Objects.equals(price, product.price) && Objects.equals(isAvailable, product.isAvailable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, isAvailable);
    }
}
