package com.gabrielluciano.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwks.uri}")
    private String jwksUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable());
        http.oauth2ResourceServer(server -> server
                .jwt(jwt -> jwt.jwkSetUri(jwksUri))
        );
        http.authorizeExchange(exchange -> exchange
                .pathMatchers("/eureka/**").permitAll()
                .anyExchange().authenticated()
        );
        return http.build();
    }
}
