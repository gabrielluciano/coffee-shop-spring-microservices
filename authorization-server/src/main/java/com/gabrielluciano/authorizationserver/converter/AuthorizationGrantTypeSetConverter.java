package com.gabrielluciano.authorizationserver.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class AuthorizationGrantTypeSetConverter implements AttributeConverter<Set<AuthorizationGrantType>, String> {

    @Override
    public String convertToDatabaseColumn(Set<AuthorizationGrantType> authorizationGrantTypes) {
        if (authorizationGrantTypes == null) return "";
        return String.join(",", authorizationGrantTypes.stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.toSet()));
    }

    @Override
    public Set<AuthorizationGrantType> convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty()) return Collections.emptySet();
        return Arrays.stream(s.split(","))
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toSet());
    }
}
