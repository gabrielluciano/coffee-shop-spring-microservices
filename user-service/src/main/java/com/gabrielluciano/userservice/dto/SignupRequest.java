package com.gabrielluciano.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupRequest {

    private String name;
    private String email;
    private String password;
}
