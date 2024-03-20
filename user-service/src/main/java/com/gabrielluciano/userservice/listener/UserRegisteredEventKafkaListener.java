package com.gabrielluciano.userservice.listener;

import com.gabrielluciano.userservice.event.UserRegisteredEvent;
import com.gabrielluciano.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserRegisteredEventKafkaListener {

    private final UserService userService;

    @KafkaListener(id = "group", topics = "user-registration-events")
    public void listen(UserRegisteredEvent userRegisteredEvent) {
        log.info("Received UserRegisteredEvent for user with id '{}'", userRegisteredEvent.getUserId());
        userService.saveUser(userRegisteredEvent);
    }
}
