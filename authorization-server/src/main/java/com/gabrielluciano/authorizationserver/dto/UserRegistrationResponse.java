package com.gabrielluciano.authorizationserver.dto;

import com.gabrielluciano.authorizationserver.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserRegistrationResponse {

    private UUID id;
    private String name;
    private String email;
    private Set<Role> roles;
}
