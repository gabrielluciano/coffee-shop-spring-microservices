package com.gabrielluciano.authorizationserver.converter;

import com.gabrielluciano.authorizationserver.model.OAuthScope;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class OAuthScopeSetConverter implements AttributeConverter<Set<OAuthScope>, String> {

    @Override
    public String convertToDatabaseColumn(Set<OAuthScope> scopes) {
        if (scopes == null) return "";
        return String.join(",", scopes.stream()
                .map(OAuthScope::getValue)
                .collect(Collectors.toSet()));
    }

    @Override
    public Set<OAuthScope> convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty()) return Collections.emptySet();
        return Arrays.stream(s.split(","))
                .map(OAuthScope::new)
                .collect(Collectors.toSet());
    }
}
