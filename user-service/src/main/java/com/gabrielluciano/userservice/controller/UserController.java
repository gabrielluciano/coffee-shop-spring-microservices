package com.gabrielluciano.userservice.controller;

import com.gabrielluciano.userservice.dto.UserResponse;
import com.gabrielluciano.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("{id}")
    public UserResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }
}
