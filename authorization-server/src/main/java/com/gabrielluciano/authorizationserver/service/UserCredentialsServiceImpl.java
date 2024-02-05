package com.gabrielluciano.authorizationserver.service;

import com.gabrielluciano.authorizationserver.dto.UserRegistrationRequest;
import com.gabrielluciano.authorizationserver.dto.UserRegistrationResponse;
import com.gabrielluciano.authorizationserver.event.UserRegisteredEvent;
import com.gabrielluciano.authorizationserver.exception.UserRegistrationException;
import com.gabrielluciano.authorizationserver.model.Role;
import com.gabrielluciano.authorizationserver.model.UserCredentials;
import com.gabrielluciano.authorizationserver.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
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
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Override
    public UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest) {
        UserCredentials userCredentials = createUserCredentials(userRegistrationRequest);
        saveCredentials(userCredentials);
        UserRegisteredEvent userRegisteredEvent = createUserRegisteredEvent(userCredentials.getId(),
                userRegistrationRequest);
        sendUserRegisteredEventOrThrowException(userRegisteredEvent);

        return UserRegistrationResponse.builder()
                .id(userCredentials.getId())
                .name(userRegistrationRequest.getName())
                .email(userCredentials.getEmail())
                .roles(userCredentials.getRoles())
                .build();
    }

    private UserCredentials createUserCredentials(UserRegistrationRequest request) {
        return UserCredentials.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();
    }

    @Transactional
    private void saveCredentials(UserCredentials userCredentials) {
        userCredentialsRepository.save(userCredentials);
    }

    private UserRegisteredEvent createUserRegisteredEvent(UUID userId,
                                                          UserRegistrationRequest userRegistrationRequest) {
        return UserRegisteredEvent.builder()
                .userId(userId)
                .name(userRegistrationRequest.getName())
                .email(userRegistrationRequest.getEmail())
                .build();
    }

    private void sendUserRegisteredEventOrThrowException(UserRegisteredEvent userRegisteredEvent) {
        try {
            sendUserRegisteredEvent(userRegisteredEvent);
        } catch (Exception ex) {
            throw new UserRegistrationException(ex);
        }
    }

    private void sendUserRegisteredEvent(UserRegisteredEvent userRegisteredEvent) throws Exception {
        kafkaTemplate.send("user-registration-events", userRegisteredEvent).get();
    }
}
