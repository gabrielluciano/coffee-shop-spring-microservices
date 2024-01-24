package com.gabrielluciano.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users")
@SequenceGenerator(
        name = User.SEQUENCE_NAME,
        sequenceName = User.SEQUENCE_NAME,
        allocationSize = 1
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {

    public static final String SEQUENCE_NAME = "users_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
