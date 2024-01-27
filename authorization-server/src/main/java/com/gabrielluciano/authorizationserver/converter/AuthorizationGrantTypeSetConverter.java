package com.gabrielluciano.authorizationserver.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class AuthorizationGrantTypeSetConverter implements AttributeConverter<Set<AuthorizationGrantType>, String> {

    @Override
    public String convertToDatabaseColumn(Set<AuthorizationGrantType> authorizationGrantTypes) {
        return String.join(",", authorizationGrantTypes.stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.toSet()));
    }

    @Override
    public Set<AuthorizationGrantType> convertToEntityAttribute(String s) {
        return Arrays.stream(s.split(","))
                .map(AuthorizationGrantType::new)
                .collect(Collectors.toSet());
    }
}
