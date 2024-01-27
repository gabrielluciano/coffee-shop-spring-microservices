package com.gabrielluciano.authorizationserver.service;

import com.gabrielluciano.authorizationserver.model.Client;
import com.gabrielluciano.authorizationserver.model.OAuthScope;
import com.gabrielluciano.authorizationserver.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomClientService implements RegisteredClientRepository {

    private final ClientRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void save(RegisteredClient registeredClient) {
        Client client = Client.builder()
                .clientId(registeredClient.getClientId())
                .secret(passwordEncoder.encode(registeredClient.getClientSecret()))
                .authMethods(registeredClient.getClientAuthenticationMethods())
                .grantTypes(registeredClient.getAuthorizationGrantTypes())
                .scopes(registeredClient.getScopes().stream().map(OAuthScope::new)
                        .collect(Collectors.toSet()))
                .build();

        repository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        Client client = repository.findById(Long.parseLong(id)).orElseThrow();
        return Client.toRegisteredClient(client);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Client client = repository.findByClientId(clientId).orElseThrow();
        return Client.toRegisteredClient(client);
    }
}
