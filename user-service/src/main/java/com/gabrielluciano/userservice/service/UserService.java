package com.gabrielluciano.userservice.service;

import com.gabrielluciano.userservice.dto.SignupRequest;
import com.gabrielluciano.userservice.dto.UserResponse;

public interface UserService {

    UserResponse signup(SignupRequest signupRequest);
}
