package com.gabrielluciano.authorizationserver.repository;

import com.gabrielluciano.authorizationserver.model.Client;
import com.gabrielluciano.authorizationserver.model.OAuthScope;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ClientRepositoryTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ClientRepository clientRepository;

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
    void shouldSaveAndFindClientById() {
        Client client = Client.builder()
                .clientId("some-client")
                .secret("encrypted-secret")
                .scopes(Set.of(new OAuthScope(OidcScopes.OPENID), new OAuthScope(OidcScopes.EMAIL)))
                .authMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .grantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .build();

        clientRepository.save(client);

        Client clientFromDb = clientRepository.findById(client.getId()).orElseThrow();

        assertNotNull(clientFromDb);
        assertEquals(client.getId(), clientFromDb.getId());
        assertEquals(client, clientFromDb);
    }

    @Test
    void shouldFindClientByClientId() {
        Client client = Client.builder()
                .clientId("some-client")
                .secret("encrypted-secret")
                .scopes(Set.of(new OAuthScope(OidcScopes.OPENID), new OAuthScope(OidcScopes.EMAIL)))
                .authMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .grantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .build();

        clientRepository.save(client);

        Client clientFromDb = clientRepository.findByClientId(client.getClientId()).orElseThrow();

        assertNotNull(clientFromDb);
        assertEquals(client.getId(), clientFromDb.getId());
        assertEquals(client, clientFromDb);
    }

    @Test
    void shouldNotAllowDuplicatedClientIds() {
        Client client = Client.builder()
                .clientId("some-client")
                .secret("encrypted-secret")
                .scopes(Set.of(new OAuthScope(OidcScopes.OPENID), new OAuthScope(OidcScopes.EMAIL)))
                .authMethods(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC))
                .grantTypes(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.REFRESH_TOKEN))
                .build();

        clientRepository.save(client);

        Client newClient = Client.builder()
                .clientId(client.getClientId())
                .secret(client.getSecret())
                .scopes(client.getScopes())
                .authMethods(client.getAuthMethods())
                .grantTypes(client.getGrantTypes())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> clientRepository.save(newClient));
    }
}
