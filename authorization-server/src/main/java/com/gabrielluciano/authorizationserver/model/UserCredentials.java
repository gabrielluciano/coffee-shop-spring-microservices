package com.gabrielluciano.authorizationserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_credentials")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserCredentials {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @Column(nullable = false)
    private Boolean enabled;

    public Boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UserCredentials that = (UserCredentials) object;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.equals(roles, that.roles) && Objects.equals(enabled, that.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, roles, enabled);
    }
}
