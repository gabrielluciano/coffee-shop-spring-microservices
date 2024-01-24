package com.gabrielluciano.userservice.dto;

import com.gabrielluciano.userservice.model.Role;
import com.gabrielluciano.userservice.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {

    private String name;
    private String email;
    private Set<Role> roles;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
