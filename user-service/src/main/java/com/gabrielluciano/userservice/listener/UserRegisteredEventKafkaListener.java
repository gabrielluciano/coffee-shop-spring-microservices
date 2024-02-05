package com.gabrielluciano.userservice.listener;

import com.gabrielluciano.userservice.event.UserRegisteredEvent;
import com.gabrielluciano.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisteredEventKafkaListener {

    private final UserService userService;

    @KafkaListener(id = "group", topics = "user-registration-events")
    public void listen(UserRegisteredEvent userRegisteredEvent) {
        userService.saveUser(userRegisteredEvent);
    }
}
