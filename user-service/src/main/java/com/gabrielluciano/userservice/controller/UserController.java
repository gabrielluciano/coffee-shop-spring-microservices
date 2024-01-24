package com.gabrielluciano.userservice.controller;

import com.gabrielluciano.userservice.dto.SignupRequest;
import com.gabrielluciano.userservice.dto.UserResponse;
import com.gabrielluciano.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@RequestBody SignupRequest signupRequest) {
        return userService.signup(signupRequest);
    }
}
