package com.gabrielluciano.authorizationserver.converter;

import com.gabrielluciano.authorizationserver.model.OAuthScope;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class OAuthScopeSetConverter implements AttributeConverter<Set<OAuthScope>, String> {

    @Override
    public String convertToDatabaseColumn(Set<OAuthScope> scopes) {
        return String.join(",", scopes.stream()
                .map(OAuthScope::getValue)
                .collect(Collectors.toSet()));
    }

    @Override
    public Set<OAuthScope> convertToEntityAttribute(String s) {
        return Arrays.stream(s.split(","))
                .map(OAuthScope::new)
                .collect(Collectors.toSet());
    }
}
