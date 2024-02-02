package com.gabrielluciano.authorizationserver.repository;

import com.gabrielluciano.authorizationserver.model.Role;
import com.gabrielluciano.authorizationserver.model.UserCredentials;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserCredentialsRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        userCredentialsRepository.deleteAll();
    }

    @Test
    void shouldSaveUserCredentials() {
        UserCredentials userCredentials = UserCredentials.builder()
                .id(UUID.randomUUID())
                .email("user@mail.com")
                .password("encrypted-password")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();

        userCredentialsRepository.save(userCredentials);
        entityManager.flush();

        UserCredentials userCredentialsFromDb = entityManager.find(UserCredentials.class, userCredentials.getId());

        assertNotNull(userCredentialsFromDb);
        assertEquals(userCredentials.getId(), userCredentialsFromDb.getId());
        assertEquals(userCredentials, userCredentialsFromDb);
    }

    @Test
    void shouldFindUserCredentialsById() {
        UserCredentials userCredentials = UserCredentials.builder()
                .id(UUID.randomUUID())
                .email("user@mail.com")
                .password("encrypted-password")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();
        entityManager.persistAndFlush(userCredentials);

        UserCredentials userCredentialsFromDb = userCredentialsRepository.findById(userCredentials.getId())
                .orElseThrow();

        assertNotNull(userCredentialsFromDb);
        assertEquals(userCredentials.getId(), userCredentialsFromDb.getId());
        assertEquals(userCredentials, userCredentialsFromDb);
    }

    @Test
    void shouldFindUserCredentialsByEmail() {
        UserCredentials userCredentials = UserCredentials.builder()
                .id(UUID.randomUUID())
                .email("user@mail.com")
                .password("encrypted-password")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();
        entityManager.persistAndFlush(userCredentials);

        UserCredentials userCredentialsFromDb = userCredentialsRepository.findByEmail(userCredentials.getEmail())
                .orElseThrow();

        assertNotNull(userCredentialsFromDb);
        assertEquals(userCredentials.getId(), userCredentialsFromDb.getId());
        assertEquals(userCredentials, userCredentialsFromDb);
    }

    @Test
    void shouldNotAllowDuplicatedEmails() {
        UserCredentials userCredentials = UserCredentials.builder()
                .id(UUID.randomUUID())
                .email("user@mail.com")
                .password("encrypted-password")
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();
        entityManager.persistAndFlush(userCredentials);

        UserCredentials newUserCredentials = UserCredentials.builder()
                .id(UUID.randomUUID())
                .email(userCredentials.getEmail())
                .password(userCredentials.getPassword())
                .roles(userCredentials.getRoles())
                .enabled(userCredentials.isEnabled())
                .build();

        assertThrows(Exception.class, () -> {
            userCredentialsRepository.save(newUserCredentials);
            entityManager.flush();
        });
    }
}
