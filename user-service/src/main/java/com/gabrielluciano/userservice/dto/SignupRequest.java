package com.gabrielluciano.userservice.dto;

import com.gabrielluciano.userservice.util.Messages;
import com.gabrielluciano.userservice.util.RegexPatterns;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupRequest {

    @Size(max = 100)
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotNull
    @Pattern(message = Messages.PASSWORD_VALIDATION_MESSAGE, regexp = RegexPatterns.STRONG_PASSWORD_PATTERN)
    private String password;
}
