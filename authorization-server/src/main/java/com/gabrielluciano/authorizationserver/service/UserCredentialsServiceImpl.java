package com.gabrielluciano.authorizationserver.service;

import com.gabrielluciano.authorizationserver.dto.UserRegistrationRequest;
import com.gabrielluciano.authorizationserver.dto.UserRegistrationResponse;
import com.gabrielluciano.authorizationserver.event.UserRegisteredEvent;
import com.gabrielluciano.authorizationserver.exception.UniqueConstraintViolationException;
import com.gabrielluciano.authorizationserver.exception.UserRegistrationException;
import com.gabrielluciano.authorizationserver.model.Role;
import com.gabrielluciano.authorizationserver.model.UserCredentials;
import com.gabrielluciano.authorizationserver.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserCredentialsServiceImpl implements UserCredentialsService {

    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Override
    @Transactional
    public UserRegistrationResponse registerUser(UserRegistrationRequest userRegistrationRequest) {
        findUserByEmailAndThrowExceptionIfFound(userRegistrationRequest.getEmail());
        UserCredentials userCredentials = createUserCredentials(userRegistrationRequest);
        userCredentialsRepository.save(userCredentials);
        UserRegisteredEvent userRegisteredEvent = createUserRegisteredEvent(userCredentials.getId(),
                userRegistrationRequest);
        sendUserRegisteredEventOrThrowException(userRegisteredEvent);

        log.info("Successfully saved user credentials with id '{}'", userCredentials.getId());
        return UserRegistrationResponse.builder()
                .id(userCredentials.getId())
                .name(userRegistrationRequest.getName())
                .email(userCredentials.getEmail())
                .roles(userCredentials.getRoles())
                .build();
    }

    private void findUserByEmailAndThrowExceptionIfFound(String email) {
        userCredentialsRepository.findByEmail(email)
                .ifPresent(u -> {
                    throw new UniqueConstraintViolationException("Email already registered");
                });
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
            log.error("Error sending UserRegisteredEvent for user with id '{}'", userRegisteredEvent.getUserId());
            throw new UserRegistrationException(ex);
        }
    }

    private void sendUserRegisteredEvent(UserRegisteredEvent userRegisteredEvent) throws Exception {
        kafkaTemplate.send("user-registration-events", userRegisteredEvent).get();
        log.info("Successfully sent UserRegisteredEvent for user with id '{}'", userRegisteredEvent.getUserId());
    }
}
