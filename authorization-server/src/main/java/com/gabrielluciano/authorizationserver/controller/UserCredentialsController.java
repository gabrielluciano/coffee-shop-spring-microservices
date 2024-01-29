package com.gabrielluciano.authorizationserver.controller;

import com.gabrielluciano.authorizationserver.dto.UserRegistrationRequest;
import com.gabrielluciano.authorizationserver.dto.UserRegistrationResponse;
import com.gabrielluciano.authorizationserver.service.UserCredentialsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserCredentialsController {

    private final UserCredentialsService userCredentialsService;

    @PostMapping("api/v1/user/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponse registerUser(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {
        return userCredentialsService.registerUser(userRegistrationRequest);
    }
}
