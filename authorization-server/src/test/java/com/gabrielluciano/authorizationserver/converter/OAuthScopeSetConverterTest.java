package com.gabrielluciano.authorizationserver.converter;

import com.gabrielluciano.authorizationserver.model.OAuthScope;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OAuthScopeSetConverterTest {

    private final OAuthScopeSetConverter converter = new OAuthScopeSetConverter();

    @Test
    void toDatabaseColumnNull() {
        String emptyString = "";
        String result = converter.convertToDatabaseColumn(null);
        assertEquals(emptyString, result);
    }

    @Test
    void toDatabaseColumnEmptySet() {
        String emptyString = "";
        String result = converter.convertToDatabaseColumn(Collections.emptySet());
        assertEquals(emptyString, result);
    }

    @Test
    void toDatabaseColumnSingleElement() {
        String result = converter.convertToDatabaseColumn(Set.of(OAuthScope.EMAIL));
        assertEquals(OAuthScope.EMAIL.getValue(), result);
    }

    @Test
    void toDatabaseColumnMultipleElements() {
        Set<OAuthScope> scopes = Set.of(OAuthScope.EMAIL, OAuthScope.PROFILE);
        String result = converter.convertToDatabaseColumn(scopes);

        assertThat(result.split(",")).contains(OAuthScope.EMAIL.getValue());
        assertThat(result.split(",")).contains(OAuthScope.PROFILE.getValue());
    }

    @Test
    void toEntityAttributeNull() {
        Set<OAuthScope> scopes = converter.convertToEntityAttribute(null);
        assertThat(scopes).isEmpty();
    }

    @Test
    void toEntityAttributeEmptyString() {
        Set<OAuthScope> scopes = converter.convertToEntityAttribute("");
        assertThat(scopes).isEmpty();
    }

    @Test
    void toEntityAttributeSingleElement() {
        Set<OAuthScope> scopes = converter.convertToEntityAttribute(OAuthScope.EMAIL.getValue());
        assertThat(scopes).contains(OAuthScope.EMAIL);
    }

    @Test
    void toEntityAttributeMultipleElements() {
        String s = OAuthScope.EMAIL.getValue() + "," + OAuthScope.PROFILE.getValue();
        Set<OAuthScope> scopes = converter.convertToEntityAttribute(s);
        assertThat(scopes).contains(OAuthScope.EMAIL);
        assertThat(scopes).contains(OAuthScope.PROFILE);
    }
}
