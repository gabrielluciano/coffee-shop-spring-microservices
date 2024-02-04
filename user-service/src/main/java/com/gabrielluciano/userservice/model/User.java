package com.gabrielluciano.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

    @Id
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;
}
