package com.gabrielluciano.authorizationserver.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class ClientAuthenticationMethodSetConverter implements AttributeConverter<Set<ClientAuthenticationMethod>, String> {

    @Override
    public String convertToDatabaseColumn(Set<ClientAuthenticationMethod> clientAuthenticationMethods) {
        if (clientAuthenticationMethods == null) return "";
        return String.join(",", clientAuthenticationMethods.stream()
                .map(ClientAuthenticationMethod::getValue)
                .collect(Collectors.toSet()));
    }

    @Override
    public Set<ClientAuthenticationMethod> convertToEntityAttribute(String s) {
        if (s == null || s.isEmpty()) return Collections.emptySet();
        return Arrays.stream(s.split(","))
                .map(ClientAuthenticationMethod::new)
                .collect(Collectors.toSet());
    }
}
