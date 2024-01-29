package com.gabrielluciano.authorizationserver.service;

import com.gabrielluciano.authorizationserver.dto.UserRegistrationRequest;
import com.gabrielluciano.authorizationserver.dto.UserRegistrationResponse;

public interface UserCredentialsService {
    UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest);
}
