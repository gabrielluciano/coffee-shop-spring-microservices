package com.gabrielluciano.productservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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
    private String name;
    private String description;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    private boolean isAvailable;
}
