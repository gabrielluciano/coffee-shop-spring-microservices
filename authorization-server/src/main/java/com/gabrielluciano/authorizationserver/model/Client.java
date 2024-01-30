package com.gabrielluciano.authorizationserver.model;

import com.gabrielluciano.authorizationserver.converter.AuthorizationGrantTypeSetConverter;
import com.gabrielluciano.authorizationserver.converter.ClientAuthenticationMethodSetConverter;
import com.gabrielluciano.authorizationserver.converter.OAuthScopeSetConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "clients")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Client {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String secret;

    @ElementCollection
    @CollectionTable(name = "clients_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri", length = 1000)
    private Set<String> redirectUris;

    @Column(nullable = false)
    @Convert(converter = ClientAuthenticationMethodSetConverter.class)
    private Set<ClientAuthenticationMethod> authMethods;

    @Column(nullable = false)
    @Convert(converter = AuthorizationGrantTypeSetConverter.class)
    private Set<AuthorizationGrantType> grantTypes;

    @Column(nullable = false)
    @Convert(converter = OAuthScopeSetConverter.class)
    private Set<OAuthScope> scopes;

    public static RegisteredClient toRegisteredClient(Client client) {
        return RegisteredClient.withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientSecret(client.getSecret())
                .redirectUris(uris -> uris.addAll(client.getRedirectUris()))
                .clientAuthenticationMethods(methods -> methods.addAll(client.getAuthMethods()))
                .authorizationGrantTypes(types -> types.addAll(client.getGrantTypes()))
                .scopes(scopes -> scopes.addAll(client.getScopes().stream().map(OAuthScope::getValue)
                        .collect(Collectors.toSet())))
                .build();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Client client = (Client) object;
        return Objects.equals(id, client.id) && Objects.equals(clientId, client.clientId) && Objects.equals(secret, client.secret) && Objects.equals(authMethods, client.authMethods) && Objects.equals(grantTypes, client.grantTypes) && Objects.equals(scopes, client.scopes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clientId, secret, authMethods, grantTypes, scopes);
    }
}
