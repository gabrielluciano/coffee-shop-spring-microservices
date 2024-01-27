package com.gabrielluciano.authorizationserver.model;

import com.gabrielluciano.authorizationserver.converter.AuthorizationGrantTypeSetConverter;
import com.gabrielluciano.authorizationserver.converter.ClientAuthenticationMethodSetConverter;
import com.gabrielluciano.authorizationserver.converter.OAuthScopeSetConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "clients")
@SequenceGenerator(
        name = Client.SEQUENCE_NAME,
        sequenceName = Client.SEQUENCE_NAME,
        allocationSize = 1
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Client {

    public static final String SEQUENCE_NAME = "clients_id_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Long id;

    @Column(unique = true, nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String secret;

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
        return RegisteredClient.withId(String.valueOf(client.getId()))
                .clientId(client.getClientId())
                .clientSecret(client.getSecret())
                .clientAuthenticationMethods(methods -> methods.addAll(client.getAuthMethods()))
                .authorizationGrantTypes(types -> types.addAll(client.getGrantTypes()))
                .scopes(scopes -> scopes.addAll(client.getScopes().stream().map(OAuthScope::getValue)
                        .collect(Collectors.toSet())))
                .build();
    }
}
