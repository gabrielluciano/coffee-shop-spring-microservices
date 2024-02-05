package com.gabrielluciano.authorizationserver.event;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private final String eventType = "UserRegisteredEvent";
    private UUID userId;
    private String name;
    private String email;
}
