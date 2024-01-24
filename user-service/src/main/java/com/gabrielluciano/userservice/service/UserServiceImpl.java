package com.gabrielluciano.userservice.service;

import com.gabrielluciano.userservice.dto.SignupRequest;
import com.gabrielluciano.userservice.dto.UserResponse;
import com.gabrielluciano.userservice.model.Role;
import com.gabrielluciano.userservice.model.User;
import com.gabrielluciano.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse signup(SignupRequest signupRequest) {
        User user = User.builder().name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .roles(Set.of(Role.USER))
                .build();
        userRepository.save(user);
        return UserResponse.fromUser(user);
    }
}
