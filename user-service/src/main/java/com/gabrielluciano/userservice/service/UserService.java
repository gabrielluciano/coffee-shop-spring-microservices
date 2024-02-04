package com.gabrielluciano.userservice.service;

import com.gabrielluciano.userservice.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getUser(UUID id);
}
