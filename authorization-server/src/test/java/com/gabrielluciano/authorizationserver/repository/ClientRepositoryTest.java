package com.gabrielluciano.authorizationserver.repository;

import com.gabrielluciano.authorizationserver.model.Client;
import com.gabrielluciano.authorizationserver.model.OAuthScope;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClientRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ClientRepository clientRepository;

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
        clientRepository.deleteAll();
    }

    @Test
    void shouldSaveClient() {
        Client client = Client.builder()
                .id(UUID.randomUUID())
                .clientId("some-client")
                .secret("encrypted-secret")
                .scopes(Set.of(new OAuthScope(OidcScopes.OPENID), new OAuthScope(OidcScopes.EMAIL)))
                .authMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .grantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .build();

        clientRepository.save(client);
        entityManager.flush();

        Client clientFromDb = entityManager.find(Client.class, client.getId());

        assertNotNull(clientFromDb);
        assertEquals(client.getId(), clientFromDb.getId());
        assertEquals(client, clientFromDb);
    }

    @Test
    void shouldFindClientById() {
        Client client = Client.builder()
                .id(UUID.randomUUID())
                .clientId("some-client")
                .secret("encrypted-secret")
                .scopes(Set.of(new OAuthScope(OidcScopes.OPENID), new OAuthScope(OidcScopes.EMAIL)))
                .authMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .grantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .build();
        entityManager.persistAndFlush(client);

        Client clientFromDb = clientRepository.findById(client.getId()).orElseThrow();

        assertNotNull(clientFromDb);
        assertEquals(client.getId(), clientFromDb.getId());
        assertEquals(client, clientFromDb);
    }

    @Test
    void shouldFindClientByClientId() {
        Client client = Client.builder()
                .id(UUID.randomUUID())
                .clientId("some-client")
                .secret("encrypted-secret")
                .scopes(Set.of(new OAuthScope(OidcScopes.OPENID), new OAuthScope(OidcScopes.EMAIL)))
                .authMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .grantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .build();
        entityManager.persistAndFlush(client);

        Client clientFromDb = clientRepository.findByClientId(client.getClientId()).orElseThrow();

        assertNotNull(clientFromDb);
        assertEquals(client.getId(), clientFromDb.getId());
        assertEquals(client, clientFromDb);
    }

    @Test
    void shouldNotAllowDuplicatedClientIds() {
        Client client = Client.builder()
                .id(UUID.randomUUID())
                .clientId("some-client")
                .secret("encrypted-secret")
                .scopes(Set.of(new OAuthScope(OidcScopes.OPENID), new OAuthScope(OidcScopes.EMAIL)))
                .authMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .grantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .build();
        entityManager.persistAndFlush(client);

        Client newClient = Client.builder()
                .id(UUID.randomUUID())
                .clientId(client.getClientId())
                .secret(client.getSecret())
                .scopes(client.getScopes())
                .authMethods(client.getAuthMethods())
                .grantTypes(client.getGrantTypes())
                .build();

        assertThrows(Exception.class, () -> {
            clientRepository.save(newClient);
            entityManager.flush();
        });
    }
}
