package com.gabrielluciano.apigateway.config;

import com.gabrielluciano.apigateway.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .jwt(jwt -> jwt
                        .jwkSetUri(jwksUri)
                        .jwtAuthenticationConverter(new CustomJwtAuthenticationTokenConverter())
                )
        );
        http.authorizeExchange(exchange -> exchange
                .pathMatchers("/eureka/**").permitAll()
                .pathMatchers("/api/v1/cart/**").hasRole(Role.USER.name())
                .pathMatchers(HttpMethod.GET, "/api/v1/products", "/api/v1/products/").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/products/{id}", "/api/v1/products/{id}/").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/products", "/api/v1/products/").hasRole(Role.ADMIN.name())
                .anyExchange().authenticated()
        );
        return http.build();
    }
}
