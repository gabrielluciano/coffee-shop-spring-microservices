package com.gabrielluciano.authorizationserver.service;

import com.gabrielluciano.authorizationserver.dto.UserRegistrationRequest;
import com.gabrielluciano.authorizationserver.dto.UserRegistrationResponse;
import com.gabrielluciano.authorizationserver.model.Role;
import com.gabrielluciano.authorizationserver.model.UserCredentials;
import com.gabrielluciano.authorizationserver.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCredentialsServiceImpl implements UserCredentialsService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest) {
        UserCredentials userCredentials = UserCredentials.builder()
                .id(UUID.randomUUID())
                .email(userRegistrationRequest.getEmail())
                .password(passwordEncoder.encode(userRegistrationRequest.getPassword()))
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();

        userCredentialsRepository.save(userCredentials);

        return UserRegistrationResponse.builder()
                .id(userCredentials.getId())
                .name(userRegistrationRequest.getName())
                .email(userCredentials.getEmail())
                .roles(userCredentials.getRoles())
                .build();
    }
}
