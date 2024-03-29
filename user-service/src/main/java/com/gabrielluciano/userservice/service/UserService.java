package com.gabrielluciano.userservice.service;

import com.gabrielluciano.userservice.dto.UserResponse;
import com.gabrielluciano.userservice.event.UserRegisteredEvent;

import java.util.UUID;

public interface UserService {

    UserResponse getUser(UUID id);

    void saveUser(UserRegisteredEvent userRegisteredEvent);
}
