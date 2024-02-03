package com.gabrielluciano.cartservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockJwtSecurityContextFactory implements WithSecurityContextFactory<WithMockJwt> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwt customJwt) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("some", "header")
                .claim("userId", customJwt.userId())
                .claim("roles", customJwt.roles())
                .build();

        Authentication authentication = new JwtAuthenticationToken(jwt);
        context.setAuthentication(authentication);
        return context;
    }
}
