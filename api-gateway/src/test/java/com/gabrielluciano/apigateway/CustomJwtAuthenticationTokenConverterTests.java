package com.gabrielluciano.apigateway;

import com.gabrielluciano.apigateway.config.CustomJwtAuthenticationTokenConverter;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomJwtAuthenticationTokenConverterTests {

    @Test
    void convertRolesClaimToGrantedAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                // jwt must have at least one header
                .header("alg", "RS256")
                .claim("roles", List.of("USER", "EMPLOYEE", "ADMIN"))
                .build();

        CustomJwtAuthenticationTokenConverter converter = new CustomJwtAuthenticationTokenConverter();
        AbstractAuthenticationToken token = converter.convert(jwt).block();
        var authorities = token.getAuthorities();

        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_USER"));
        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        assertThat(authorities).contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void emptyRolesClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                .claim("roles", Collections.emptyList())
                .header("alg", "RS256")
                .build();

        CustomJwtAuthenticationTokenConverter converter = new CustomJwtAuthenticationTokenConverter();
        AbstractAuthenticationToken token = converter.convert(jwt).block();
        var authorities = token.getAuthorities();

        assertThat(authorities).isEmpty();
    }

    @Test
    void noRolesClaim() {
        Jwt jwt = Jwt.withTokenValue("token")
                // jwt must have at least one claim
                .claim("some", "claim")
                .header("alg", "RS256")
                .build();

        CustomJwtAuthenticationTokenConverter converter = new CustomJwtAuthenticationTokenConverter();
        AbstractAuthenticationToken token = converter.convert(jwt).block();
        var authorities = token.getAuthorities();

        assertThat(authorities).isEmpty();
    }
}
