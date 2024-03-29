package com.gabrielluciano.userservice.service;

import com.gabrielluciano.userservice.dto.UserResponse;
import com.gabrielluciano.userservice.event.UserRegisteredEvent;
import com.gabrielluciano.userservice.exception.UserNotFoundException;
import com.gabrielluciano.userservice.model.User;
import com.gabrielluciano.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse getUser(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::fromUser)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional
    public void saveUser(UserRegisteredEvent userRegisteredEvent) {
        User user = User.builder()
                .id(userRegisteredEvent.getUserId())
                .name(userRegisteredEvent.getName())
                .email(userRegisteredEvent.getEmail())
                .build();
        userRepository.save(user);
        log.info("Successfully saved user with id '{}'", user.getId());
    }
}
