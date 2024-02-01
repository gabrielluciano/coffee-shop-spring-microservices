package com.gabrielluciano.apigateway.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationTokenConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = extractAuthoritiesFromJwt(jwt);
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);
        return Mono.just(token);
    }

    private List<GrantedAuthority> extractAuthoritiesFromJwt(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null)
            return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

        return Collections.emptyList();
    }
}
